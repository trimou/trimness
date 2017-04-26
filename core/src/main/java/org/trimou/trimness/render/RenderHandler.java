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
import static org.trimou.trimness.config.TrimnessConfigurationKey.DEFAULT_RESULT_TIMEOUT;
import static org.trimou.trimness.util.Resources.asyncResult;
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
import static org.trimou.trimness.util.Strings.RESULT_TYPE;
import static org.trimou.trimness.util.Strings.TIMEOUT;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.weld.vertx.web.WebRoute;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.exception.MustacheException;
import org.trimou.trimness.config.Configuration;
import org.trimou.trimness.model.ModelProvider;
import org.trimou.trimness.template.CompositeTemplateRepository;
import org.trimou.trimness.template.Template;
import org.trimou.trimness.util.Resources;
import org.trimou.trimness.util.Resources.ResultType;

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

    private final AtomicLong idGenerator = new AtomicLong(0);

    @Inject
    private CompositeTemplateRepository templateRepository;

    @Inject
    private ResultRepository resultRepository;

    @Inject
    private Instance<ModelProvider> modelProviders;

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

        String templateId;
        Mustache mustache;
        Template template = null;
        HttpServerResponse response = ctx.response();

        if (input.has(ID)) {

            templateId = input.get(ID).getAsString();
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
            if (template.getContentType() != null) {
                response.putHeader(HEADER_CONTENT_TYPE, template.getContentType());
            }
        } else {
            // Onetime rendering
            templateId = getOnetimeId();
            mustache = engine.compileMustache(templateId, input.get(CONTENT).getAsString());
            if (input.has(CONTENT_TYPE)) {
                response.putHeader(HEADER_CONTENT_TYPE, input.get(CONTENT_TYPE).getAsString());
            }
        }

        try {
            String result = mustache.render(initModel(templateId, input.get(MODEL)));
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

        String templateId;
        String templateContent = null;
        String contentType;

        if (input.has(ID)) {

            templateId = input.get(ID).getAsString();
            Template template = templateRepository.get(templateId);

            if (template == null) {
                templateNotFound(ctx, templateId);
                return;
            }
            contentType = template.getContentType();
        } else {
            templateId = getOnetimeId();
            contentType = input.has(CONTENT_TYPE) ? input.get(CONTENT_TYPE).getAsString() : null;
            templateContent = input.get(CONTENT).getAsString();
        }

        Result result = resultRepository.init(templateId, contentType);

        // Schedule one-shot timer
        vertx.setTimer(1, new AsyncRenderHandler(result, templateId, templateContent, engine,
                (id) -> this.initModel(id, input.get(MODEL))));
        // Schedule result removal
        long delay = configuration.getLongValue(DEFAULT_RESULT_TIMEOUT);
        if (input.has(TIMEOUT)) {
            try {
                delay = input.get(TIMEOUT).getAsLong();
            } catch (Exception ignored) {
            }
        }
        vertx.setTimer(delay, (id) -> {
            resultRepository.remove(result.getId());
        });

        ok(ctx).putHeader(HEADER_CONTENT_TYPE, APP_JSON).end(asyncResult(result.getId()));
    }

    private Map<String, Object> initModel(String templateId, Object requestModel) {
        Map<String, Object> model = new HashMap<>();
        model.put(MODEL, requestModel);
        for (ModelProvider provider : modelProviders) {
            Map<String, Object> result = provider.getModel(templateId);
            if (result != null) {
                model.put(provider.getNamespace(), result);
            }
        }
        return model;
    }

    private String getOnetimeId() {
        return "onetime_" + idGenerator.incrementAndGet();
    }

    static class AsyncRenderHandler implements Handler<Long> {

        private final Result result;

        private final String templateId;

        private final String templateContent;

        private final MustacheEngine engine;

        private final Function<String, Map<String, Object>> dataFunction;

        AsyncRenderHandler(Result result, String templateId, String templateContent, MustacheEngine engine,
                Function<String, Map<String, Object>> dataFunction) {
            this.result = result;
            this.templateId = templateId;
            this.templateContent = templateContent;
            this.engine = engine;
            this.dataFunction = dataFunction;
        }

        @Override
        public void handle(Long event) {
            Mustache mustache = templateContent != null ? engine.compileMustache(templateId, templateContent)
                    : engine.getMustache(templateId);
            String output = null;
            if (mustache != null) {
                try {
                    output = mustache.render(dataFunction.apply(templateId));
                } catch (MustacheException e) {
                    result.failure(e.getMessage());
                }
            } else {
                result.failure("No such template found in engine");
            }
            result.success(output);
        }
    }

}