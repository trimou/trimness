/*
 * Copyright 2017 Trimness team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trimou.trimness.render;

import static io.vertx.core.http.HttpMethod.POST;
import static org.jboss.weld.vertx.web.WebRoute.HandlerType.BLOCKING;
import static org.trimou.trimness.config.TrimnessKey.RESULT_TIMEOUT;
import static org.trimou.trimness.util.RouteHandlers.badRequest;
import static org.trimou.trimness.util.RouteHandlers.message;
import static org.trimou.trimness.util.RouteHandlers.notFound;
import static org.trimou.trimness.util.RouteHandlers.ok;
import static org.trimou.trimness.util.RouteHandlers.renderingError;
import static org.trimou.trimness.util.RouteHandlers.templateNotFound;
import static org.trimou.trimness.util.Strings.APP_JSON;
import static org.trimou.trimness.util.Strings.ASYNC;
import static org.trimou.trimness.util.Strings.CONTENT_TYPE;
import static org.trimou.trimness.util.Strings.HEADER_CONTENT_TYPE;
import static org.trimou.trimness.util.Strings.LINK_ID;
import static org.trimou.trimness.util.Strings.MODEL;
import static org.trimou.trimness.util.Strings.RESULT_TYPE;
import static org.trimou.trimness.util.Strings.TEMPLATE_CONTENT;
import static org.trimou.trimness.util.Strings.TEMPLATE_ID;
import static org.trimou.trimness.util.Strings.TIMEOUT;

import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonParsingException;

import org.jboss.weld.vertx.web.WebRoute;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.exception.MustacheException;
import org.trimou.trimness.config.Configuration;
import org.trimou.trimness.model.ModelInitializer;
import org.trimou.trimness.template.ImmutableTemplate;
import org.trimou.trimness.template.Template;
import org.trimou.trimness.template.TemplateCache;
import org.trimou.trimness.util.AsyncHandlers;
import org.trimou.trimness.util.RouteHandlers;
import org.trimou.trimness.util.RouteHandlers.ResultType;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

/**
 * Handles route for rendering templates.
 */
@WebRoute(value = "/render", methods = POST, type = BLOCKING, consumes = APP_JSON)
public class RenderHandler implements Handler<RoutingContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RenderHandler.class.getName());

    @Inject
    private TemplateCache templateCache;

    @Inject
    private DelegateResultRepository resultRepository;

    @Inject
    private ModelInitializer modelInitializer;

    @Inject
    private MustacheEngine engine;

    @Inject
    private Configuration configuration;

    @Inject
    private Vertx vertx;

    @Inject
    private IdGenerator idGenerator;

    @Override
    public void handle(RoutingContext ctx) {
        JsonObject input;
        try {
            input = RouteHandlers.getBodyAsJsonObject(ctx.getBodyAsString());
        } catch (JsonParsingException e) {
            badRequest(ctx, "Malformed JSON input:" + e.getMessage());
            return;
        }
        if (input == null) {
            badRequest(ctx, "Input must be JSON object");
            return;
        }
        if (!input.containsKey(TEMPLATE_ID) && !input.containsKey(TEMPLATE_CONTENT)) {
            badRequest(ctx, "Template id or content must be set");
            return;
        }
        if (isAsync(input)) {
            schedule(ctx, input);
        } else {
            execute(ctx, input, ResultType.of(input.get(RESULT_TYPE)));
        }
    }

    private boolean isAsync(JsonObject input) {
        return input.containsKey(ASYNC)
                ? input.getBoolean(ASYNC, false) || Boolean.valueOf(input.getString(ASYNC, "false")) : false;
    }

    private void execute(RoutingContext ctx, JsonObject input, ResultType resultType) {

        Mustache mustache;
        Template template = null;
        HttpServerResponse response = ctx.response();

        if (input.containsKey(TEMPLATE_ID)) {

            String templateId = input.getString(TEMPLATE_ID, null);
            if (templateId != null) {
                template = templateCache.get(templateId);
            }

            if (template == null) {
                templateNotFound(ctx, templateId);
                return;
            }
            mustache = engine.getMustache(template.getId());
            if (mustache == null) {
                notFound(ctx, message("No such template found in engine: %s", templateId).toString());
                return;
            }
        } else {
            // Onetime rendering - we can be sure the content is set
            String content = input.getString(TEMPLATE_CONTENT, "");
            template = ImmutableTemplate.of(idGenerator.nextOneoffTemplateId(), content,
                    input.getString(CONTENT_TYPE, null));
            mustache = engine.compileMustache(template.getId(), content);

        }
        if (template.getContentType() != null) {
            response.putHeader(HEADER_CONTENT_TYPE, template.getContentType());
        }

        try {
            RenderRequest renderRequest = new SimpleRenderRequest(template, RouteHandlers.initParams(input));
            String result = mustache.render(modelInitializer.initModel(renderRequest, input.get(MODEL)));
            switch (resultType) {
            case RAW:
                ok(ctx, result);
                break;
            case METADATA:
                ok(ctx).putHeader(HEADER_CONTENT_TYPE, APP_JSON).end(RouteHandlers.metadataResult(template, result));
                break;
            default:
                throw new IllegalStateException("Unsupported result type: " + resultType);
            }
        } catch (MustacheException e) {
            renderingError(ctx, mustache.getName());
        }
    }

    private void schedule(RoutingContext ctx, JsonObject input) {

        String templateId = null;
        Template template;

        if (input.containsKey(TEMPLATE_ID)) {
            templateId = input.getString(TEMPLATE_ID, null);
            template = templateId != null ? templateCache.get(templateId) : null;
            if (template == null) {
                templateNotFound(ctx, templateId);
                return;
            }
        } else {
            template = ImmutableTemplate.of(idGenerator.nextOneoffTemplateId(), input.getString(TEMPLATE_CONTENT, ""),
                    input.getString(CONTENT_TYPE, null));
        }

        RenderRequest renderRequest = new SimpleRenderRequest(template, initTimeout(input),
                input.getString(LINK_ID, null), RouteHandlers.initParams(input));
        Result result = resultRepository.init(renderRequest);

        // Schedule one-shot timer
        vertx.setTimer(1, new AsyncRenderHandler(vertx, result, template, engine,
                () -> modelInitializer.initModel(renderRequest, input.get(MODEL))));

        RouteHandlers.ok(ctx).putHeader(HEADER_CONTENT_TYPE, APP_JSON)
                .end(RouteHandlers.asyncResult(result.getId(), renderRequest));
    }

    private long initTimeout(JsonObject input) {
        JsonValue timeout = input.get(TIMEOUT);
        if (timeout != null && ValueType.NUMBER.equals(timeout.getValueType())) {
            return ((JsonNumber) timeout).longValue();
        }
        return configuration.getLongValue(RESULT_TIMEOUT);
    }

    static class AsyncRenderHandler implements Handler<Long> {

        private final Vertx vertx;

        private final Result result;

        private final Template template;

        private final MustacheEngine engine;

        private final Supplier<Map<String, Object>> modelSupplier;

        AsyncRenderHandler(Vertx vertx, Result result, Template template, MustacheEngine engine,
                Supplier<Map<String, Object>> modelSupplier) {
            this.vertx = vertx;
            this.result = result;
            this.template = template;
            this.engine = engine;
            this.modelSupplier = modelSupplier;
        }

        @Override
        public void handle(Long event) {
            vertx.executeBlocking(future -> {
                try {
                    LOGGER.debug("Async rendering of {0} started on thread {1}", template.getId(),
                            Thread.currentThread().getName());
                    Mustache mustache = template.getProviderId() == null
                            ? engine.compileMustache(template.getId(), template.getContent())
                            : engine.getMustache(template.getId());
                    if (mustache != null) {
                        result.complete(mustache.render(modelSupplier.get()));
                    } else {
                        result.fail("No such template found in engine: " + template.getId());
                    }
                    future.complete();
                } catch (Exception e) {
                    result.fail(e.getMessage());
                    future.fail(e);
                }
            }, AsyncHandlers.NOOP_HANDLER);
        }
    }

}