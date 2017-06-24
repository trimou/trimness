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

import static org.trimou.trimness.config.TrimnessKey.RESULT_TIMEOUT;
import static org.trimou.trimness.util.Strings.APP_JSON;
import static org.trimou.trimness.util.Strings.APP_JSON_UTF8;
import static org.trimou.trimness.util.Strings.ASYNC;
import static org.trimou.trimness.util.Strings.CONTENT_TYPE;
import static org.trimou.trimness.util.Strings.LINK_ID;
import static org.trimou.trimness.util.Strings.MODEL;
import static org.trimou.trimness.util.Strings.PARAMS;
import static org.trimou.trimness.util.Strings.RESULT_TYPE;
import static org.trimou.trimness.util.Strings.TEMPLATE_CONTENT;
import static org.trimou.trimness.util.Strings.TEMPLATE_ID;
import static org.trimou.trimness.util.Strings.TIMEOUT;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonParsingException;

import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.exception.MustacheException;
import org.trimou.trimness.config.Configuration;
import org.trimou.trimness.model.ModelInitializer;
import org.trimou.trimness.template.ImmutableTemplate;
import org.trimou.trimness.template.Template;
import org.trimou.trimness.template.TemplateCache;
import org.trimou.trimness.util.AsyncHandlers;
import org.trimou.trimness.util.Jsons;
import org.trimou.trimness.util.Strings;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class RenderLogic {

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

    /**
     *
     * @param inputJson
     * @param resultConsumer
     *            Used to accept the result output and optional content type
     * @param errorConsumer
     *            Used to accept the error code and message
     */
    void render(String inputJson, BiConsumer<String, String> resultConsumer,
            BiConsumer<Integer, String> errorConsumer) {
        JsonObject input = null;
        try {
            input = Jsons.asJsonObject(inputJson);
        } catch (JsonParsingException e) {
            errorConsumer.accept(Codes.CODE_INVALID_INPUT, "Malformed JSON input:" + e.getMessage());
            return;
        }
        if (input == null) {
            errorConsumer.accept(Codes.CODE_INVALID_INPUT, "Input must be JSON object");
            return;
        }
        if (!input.containsKey(TEMPLATE_ID) && !input.containsKey(TEMPLATE_CONTENT)) {
            errorConsumer.accept(Codes.CODE_INVALID_INPUT, "Template id or content must be set");
            return;
        }
        if (isAsync(input)) {
            schedule(input, resultConsumer, errorConsumer);
        } else {
            execute(input, resultConsumer, errorConsumer);
        }
    }

    private void execute(JsonObject input, BiConsumer<String, String> resultConsumer,
            BiConsumer<Integer, String> errorConsumer) {

        Mustache mustache;
        Template template = null;

        if (input.containsKey(TEMPLATE_ID)) {

            String templateId = input.getString(TEMPLATE_ID, null);
            if (templateId != null) {
                template = templateCache.get(templateId);
            }

            if (template == null) {
                errorConsumer.accept(Codes.CODE_TEMPLATE_NOT_FOUND, "Template not found: " + templateId);
                return;
            }
            mustache = engine.getMustache(template.getId());
            if (mustache == null) {
                errorConsumer.accept(Codes.CODE_TEMPLATE_NOT_FOUND, "Template not found in engine: " + templateId);
                return;
            }
        } else {
            // Onetime rendering - we can be sure the content is set
            String content = input.getString(TEMPLATE_CONTENT, "");
            template = ImmutableTemplate.of(idGenerator.nextOneoffTemplateId(), content,
                    input.getString(CONTENT_TYPE, null));
            try {
                mustache = engine.compileMustache(template.getId(), content);
            } catch (MustacheException e) {
                // Handle possible compilation problems
                errorConsumer.accept(Codes.CODE_COMPILATION_ERROR, "Template compilation failed: " + e.getMessage());
                return;
            }
        }

        Result result = null;
        try {
            RenderRequest renderRequest;

            if (input.containsKey(LINK_ID) || input.containsKey(TIMEOUT)) {
                // We need to store the result of sync rendering
                String linkId = input.getString(LINK_ID, null);
                if (linkId != null && !Strings.matchesLinkPattern(linkId)) {
                    errorConsumer.accept(Codes.CODE_INVALID_INPUT, "Link id does not match " + Strings.LINK_PATTERN);
                    return;
                }
                renderRequest = new SimpleRenderRequest(template, initTimeout(input), linkId, initParams(input));
                result = resultRepository.init(renderRequest);
            } else {
                renderRequest = new SimpleRenderRequest(template, initParams(input));
            }

            String resultOutput = mustache.render(modelInitializer.initModel(renderRequest, input.get(MODEL)));
            if (result != null) {
                result.complete(resultOutput);
            }
            ResultType resultType = ResultType.of(input.get(RESULT_TYPE));
            // Consume result
            switch (resultType) {
            case RAW:
                resultConsumer.accept(resultOutput, template.getContentType());
                break;
            case METADATA:
                resultConsumer.accept(
                        result != null ? Jsons.metadataResult(result) : Jsons.metadataResult(template, resultOutput),
                        APP_JSON_UTF8);
                break;
            default:
                throw new IllegalStateException("Unsupported result type: " + resultType);
            }

        } catch (Exception e) {
            if (result != null) {
                result.fail(e.getMessage());
            }
            String msg = "Error rendering template " + template.getId() + ": ";
            LOGGER.error(msg, e);
            errorConsumer.accept(Codes.CODE_RENDER_ERROR, msg + e.getMessage());
        }
    }

    private void schedule(JsonObject input, BiConsumer<String, String> resultConsumer,
            BiConsumer<Integer, String> errorConsumer) {

        String templateId = null;
        Template template;

        if (input.containsKey(TEMPLATE_ID)) {
            templateId = input.getString(TEMPLATE_ID, null);
            template = templateId != null ? templateCache.get(templateId) : null;
            if (template == null) {
                errorConsumer.accept(Codes.CODE_TEMPLATE_NOT_FOUND, "Template not found: " + templateId);
                return;
            }
        } else {
            template = ImmutableTemplate.of(idGenerator.nextOneoffTemplateId(), input.getString(TEMPLATE_CONTENT, ""),
                    input.getString(CONTENT_TYPE, null));
        }

        String linkId = input.getString(LINK_ID, null);
        if (linkId != null && !Strings.matchesLinkPattern(linkId)) {
            errorConsumer.accept(Codes.CODE_INVALID_INPUT, "Link id does not match " + Strings.LINK_PATTERN);
            return;
        }

        RenderRequest renderRequest = new SimpleRenderRequest(template, initTimeout(input), linkId, initParams(input));
        Result result = resultRepository.init(renderRequest);

        // Schedule one-shot timer
        vertx.setTimer(1, new AsyncRenderHandler(vertx, result, template, engine,
                () -> modelInitializer.initModel(renderRequest, input.get(MODEL))));

        // Consume result
        resultConsumer.accept(Jsons.asyncResult(result.getId(), renderRequest), APP_JSON);
    }

    private long initTimeout(JsonObject input) {
        JsonValue timeout = input.get(TIMEOUT);
        if (timeout != null && ValueType.NUMBER.equals(timeout.getValueType())) {
            return ((JsonNumber) timeout).longValue();
        }
        return configuration.getLongValue(RESULT_TIMEOUT);
    }

    private JsonObject initParams(JsonObject input) {
        JsonValue params = input.get(PARAMS);
        if (params != null && ValueType.OBJECT.equals(params.getValueType())) {
            return (JsonObject) params;
        }
        return Jsons.EMPTY_OBJECT;
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

    private boolean isAsync(JsonObject input) {
        return input.containsKey(ASYNC)
                ? input.getBoolean(ASYNC, false) || Boolean.valueOf(input.getString(ASYNC, "false")) : false;
    }

    public enum ResultType {

        RAW, METADATA;

        public static ResultType of(JsonValue element) {
            return element != null && element instanceof JsonString ? of(((JsonString) element).getString()) : RAW;
        }

        public static ResultType of(String value) {
            if (value != null) {
                value = value.toUpperCase();
                if (RAW.toString().equals(value)) {
                    return RAW;
                } else if (METADATA.toString().equals(value)) {
                    return METADATA;
                }
            }
            // The default value
            return RAW;
        }

    }

}
