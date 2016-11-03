package org.trimou.basis;

import static io.vertx.core.http.HttpMethod.POST;
import static org.jboss.weld.vertx.web.WebRoute.HandlerType.BLOCKING;
import static org.trimou.basis.BasisConfigurationKey.DEFAULT_RESULT_TIMEOUT;
import static org.trimou.basis.Resources.asyncResult;
import static org.trimou.basis.Resources.badRequest;
import static org.trimou.basis.Resources.failure;
import static org.trimou.basis.Resources.notFound;
import static org.trimou.basis.Resources.ok;
import static org.trimou.basis.Resources.renderingError;
import static org.trimou.basis.Resources.templateNotFound;
import static org.trimou.basis.Strings.APP_JSON;
import static org.trimou.basis.Strings.ASYNC;
import static org.trimou.basis.Strings.CONTENT;
import static org.trimou.basis.Strings.CONTENT_TYPE;
import static org.trimou.basis.Strings.DATA;
import static org.trimou.basis.Strings.HEADER_CONTENT_TYPE;
import static org.trimou.basis.Strings.ID;
import static org.trimou.basis.Strings.RESULT_TYPE;
import static org.trimou.basis.Strings.TIMEOUT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.weld.vertx.web.WebRoute;
import org.trimou.Mustache;
import org.trimou.basis.DataItemProvider.DataItem;
import org.trimou.basis.Resources.ResultType;
import org.trimou.basis.Result.Code;
import org.trimou.engine.MustacheEngine;
import org.trimou.exception.MustacheException;

import com.google.gson.JsonObject;

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

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RenderHandler.class);

    private final AtomicLong idGenerator = new AtomicLong(0);

    @Inject
    private CompositeTemplateRepository templateRepository;

    @Inject
    private ResultRepository resultRepository;

    @Inject
    private Instance<DataItemProvider> dataProviderInstance;

    @Inject
    private MustacheEngine engine;

    @Inject
    private BasisConfiguration configuration;

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
        return input.has(ASYNC)
                ? Boolean.valueOf(input.get(ASYNC).getAsString()) : false;
    }

    private void execute(RoutingContext ctx, JsonObject input,
            ResultType resultType) {

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
                notFound(ctx, failure("No such template found in engine: %s",
                        templateId).toString());
                return;
            }
            if (template.getContentType() != null) {
                response.putHeader(HEADER_CONTENT_TYPE,
                        template.getContentType());
            }
        } else {
            // Onetime rendering
            templateId = getOnetimeId();
            mustache = engine.compileMustache(templateId,
                    input.get(CONTENT).getAsString());
            if (input.has(CONTENT_TYPE)) {
                response.putHeader(HEADER_CONTENT_TYPE,
                        input.get(CONTENT_TYPE).getAsString());
            }
        }

        try {
            String result = mustache
                    .render(initData(templateId, input.get(DATA)));
            switch (resultType) {
            case RAW:
                ok(ctx, result);
                break;
            case METADATA:
                ok(ctx).putHeader(HEADER_CONTENT_TYPE, APP_JSON)
                        .end(Resources.metadataResult(template, result));
                break;
            default:
                throw new IllegalStateException(
                        "Unsupported result type: " + resultType);
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
            contentType = input.has(CONTENT_TYPE)
                    ? input.get(CONTENT_TYPE).getAsString() : null;
            templateContent = input.get(CONTENT).getAsString();
        }

        Result result = resultRepository.next(templateId, contentType);

        // Schedule one-shot timer
        vertx.setTimer(1,
                new AsyncRenderHandler(result, templateId, templateContent,
                        resultRepository, engine,
                        (id) -> this.initData(id, input.get(DATA))));
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

        ok(ctx).putHeader(HEADER_CONTENT_TYPE, APP_JSON)
                .end(asyncResult(result.getId()));

    }

    private Map<String, Object> initData(String templateId,
            Object requestData) {
        Map<String, Object> data = new HashMap<>();
        data.put(DATA, requestData);
        for (DataItemProvider provider : dataProviderInstance) {
            List<DataItem> dataItems = provider.getData(templateId);
            LOGGER.debug("Data provider \"{0}\" found {1} items",
                    provider.getNamespace(), dataItems.size());
            if (!dataItems.isEmpty()) {
                Map<String, Object> items = new HashMap<>();
                for (DataItem dataItem : dataItems) {
                    items.put(dataItem.getName(), dataItem.getValue());
                }
                data.put(provider.getNamespace(), items);
            }
        }
        return data;
    }

    private String getOnetimeId() {
        return "onetime_" + idGenerator.incrementAndGet();
    }

    static class AsyncRenderHandler implements Handler<Long> {

        private final Result result;

        private final String templateId;

        private final String templateContent;

        private final ResultRepository resultRepository;

        private final MustacheEngine engine;

        private final Function<String, Map<String, Object>> dataFunction;

        AsyncRenderHandler(Result result, String templateId,
                String templateContent, ResultRepository resultRepository,
                MustacheEngine engine,
                Function<String, Map<String, Object>> dataFunction) {
            this.result = result;
            this.templateId = templateId;
            this.templateContent = templateContent;
            this.resultRepository = resultRepository;
            this.engine = engine;
            this.dataFunction = dataFunction;
        }

        @Override
        public void handle(Long event) {
            Mustache mustache = templateContent != null
                    ? engine.compileMustache(templateId, templateContent)
                    : engine.getMustache(templateId);
            String output = null;
            String errorMessage = null;

            if (mustache != null) {
                try {
                    output = mustache.render(dataFunction.apply(templateId));
                } catch (MustacheException e) {
                    resultRepository.complete(result.getId(), Code.FAILURE,
                            e.getMessage(), null);
                }
            } else {
                errorMessage = "No such template found in engine";
                resultRepository.complete(result.getId(), Code.FAILURE,
                        errorMessage, null);
            }
            resultRepository.complete(result.getId(), Code.SUCESS, null,
                    output);
            // TODO log error if save unsuccessful
        }
    }

}