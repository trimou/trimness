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
import static org.trimou.trimness.util.Resources.badRequest;
import static org.trimou.trimness.util.Resources.failure;
import static org.trimou.trimness.util.Resources.notFound;
import static org.trimou.trimness.util.Resources.ok;
import static org.trimou.trimness.util.Resources.renderingError;
import static org.trimou.trimness.util.Resources.templateNotFound;
import static org.trimou.trimness.util.Strings.APP_JSON;
import static org.trimou.trimness.util.Strings.ASYNC;
import static org.trimou.trimness.util.Strings.CONTENT;
import static org.trimou.trimness.util.Strings.CONTENT_TYPE;
import static org.trimou.trimness.util.Strings.HEADER_CONTENT_TYPE;
import static org.trimou.trimness.util.Strings.ID;
import static org.trimou.trimness.util.Strings.MODEL;
import static org.trimou.trimness.util.Strings.PARAMS;
import static org.trimou.trimness.util.Strings.RESULT_TYPE;
import static org.trimou.trimness.util.Strings.TIMEOUT;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.jboss.weld.vertx.web.WebRoute;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.exception.MustacheException;
import org.trimou.trimness.config.Configuration;
import org.trimou.trimness.model.ModelInitializer;
import org.trimou.trimness.template.CompositeTemplateRepository;
import org.trimou.trimness.template.ImmutableTemplate;
import org.trimou.trimness.template.Template;
import org.trimou.trimness.util.Resources;
import org.trimou.trimness.util.Resources.ResultType;
import org.trimou.util.ImmutableMap;
import org.trimou.util.ImmutableMap.ImmutableMapBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * Handles route for rendering templates.
 */
@WebRoute(value = "/render", methods = POST, type = BLOCKING, consumes = APP_JSON)
public class RenderHandler implements Handler<RoutingContext> {

    private final AtomicLong idGenerator = new AtomicLong(System.currentTimeMillis());

    @Inject
    private CompositeTemplateRepository templateRepository;

    @Inject
    private ResultRepository resultRepository;

    @Inject
    private ModelInitializer modelInitializer;

    @Inject
    private MustacheEngine engine;

    @Inject
    private Configuration configuration;

    @Inject
    private Vertx vertx;

    @Override
    public void handle(RoutingContext ctx) {

        JsonObject input = Resources.getBodyAsJsonObject(ctx);

        if (input == null) {
            badRequest(ctx, "Invalid request body format");
            return;
        }
        if (!input.has(ID) && !input.has(CONTENT)) {
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
        return input.has(ASYNC) ? Boolean.valueOf(input.get(ASYNC).getAsString()) : false;
    }

    private void execute(RoutingContext ctx, JsonObject input, ResultType resultType) {

        Mustache mustache;
        Template template = null;
        HttpServerResponse response = ctx.response();

        if (input.has(ID)) {

            String templateId = input.get(ID).getAsString();
            template = templateRepository.get(input.get(ID).getAsString());

            if (template == null) {
                templateNotFound(ctx, templateId);
                return;
            }
            mustache = engine.getMustache(template.getId());
            if (mustache == null) {
                notFound(ctx, failure("No such template found in engine: %s", templateId).toString());
                return;
            }
        } else {
            // Onetime rendering - we can be sure the content is set
            template = ImmutableTemplate.of(getOnetimeId(), input.get(CONTENT).getAsString(),
                    input.has(CONTENT_TYPE) ? input.get(CONTENT_TYPE).getAsString() : null);
            mustache = engine.compileMustache(template.getId(), template.getContent());

        }
        if (template.hasContentType()) {
            response.putHeader(HEADER_CONTENT_TYPE, template.getContentType());
        }

        try {
            String result = mustache.render(modelInitializer.initModel(template, input.get(MODEL), initParams(input)));
            switch (resultType) {
            case RAW:
                ok(ctx, result);
                break;
            case METADATA:
                ok(ctx).putHeader(HEADER_CONTENT_TYPE, APP_JSON).end(Resources.metadataResult(template, result));
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

        if (input.has(ID)) {

            templateId = input.get(ID).getAsString();
            template = templateRepository.get(templateId);

            if (template == null) {
                templateNotFound(ctx, templateId);
                return;
            }
        } else {
            template = ImmutableTemplate.of(getOnetimeId(), input.get(CONTENT).getAsString(),
                    input.has(CONTENT_TYPE) ? input.get(CONTENT_TYPE).getAsString() : null);
        }

        Result result = resultRepository.init(template, initTimeout(input));

        // Schedule one-shot timer
        vertx.setTimer(1, new AsyncRenderHandler(templateId == null, result, template, engine,
                () -> modelInitializer.initModel(template, input.get(MODEL), initParams(input))));

        Resources.ok(ctx).putHeader(HEADER_CONTENT_TYPE, APP_JSON).end(Resources.asyncResult(result.getId()));
    }

    private long initTimeout(JsonObject input) {
        if (input.has(TIMEOUT)) {
            try {
                return input.get(TIMEOUT).getAsLong();
            } catch (Exception ignored) {
            }
        }
        return configuration.getLongValue(RESULT_TIMEOUT);
    }

    private Map<String, Object> initParams(JsonObject input) {
        JsonElement paramsElement = input.get(PARAMS);
        if (paramsElement == null || !paramsElement.isJsonObject()) {
            return Collections.emptyMap();
        }
        ImmutableMapBuilder<String, Object> builder = ImmutableMap.builder();
        for (Map.Entry<String, JsonElement> param : paramsElement.getAsJsonObject().entrySet()) {
            builder.put(param.getKey(), param.getValue());
        }
        return builder.build();
    }

    private String getOnetimeId() {
        return "onetime_" + idGenerator.incrementAndGet();
    }

    static class AsyncRenderHandler implements Handler<Long> {

        private final boolean oneOff;

        private final Result result;

        private final Template template;

        private final MustacheEngine engine;

        private final Supplier<Map<String, Object>> modelSupplier;

        AsyncRenderHandler(boolean isOneoff, Result result, Template template, MustacheEngine engine,
                Supplier<Map<String, Object>> modelSupplier) {
            this.oneOff = isOneoff;
            this.result = result;
            this.template = template;
            this.engine = engine;
            this.modelSupplier = modelSupplier;
        }

        @Override
        public void handle(Long event) {
            try {
                Mustache mustache = oneOff ? engine.compileMustache(template.getId(), template.getContent())
                        : engine.getMustache(template.getId());
                if (mustache != null) {
                    result.complete(mustache.render(modelSupplier.get()));
                } else {
                    result.fail("No such template found in engine: " + template.getId());
                }
            } catch (MustacheException e) {
                result.fail(e.getMessage());
            }
        }
    }

}