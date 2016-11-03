package org.trimou.basis;

import static org.trimou.basis.Strings.APP_JSON;
import static org.trimou.basis.Strings.CODE;
import static org.trimou.basis.Strings.CONTENT_TYPE;
import static org.trimou.basis.Strings.FAILURE;
import static org.trimou.basis.Strings.HEADER_CONTENT_TYPE;
import static org.trimou.basis.Strings.MSG;
import static org.trimou.basis.Strings.RESULT;
import static org.trimou.basis.Strings.RESULT_ID;
import static org.trimou.basis.Strings.SUCCESS;
import static org.trimou.basis.Strings.TEMPLATE_ID;
import static org.trimou.basis.Strings.TIME;

import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

/**
 *
 * @author Martin Kouba
 */
class Resources {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(Resources.class.getName());

    static String metadataResult(Template template, String result) {
        JsonObject metadata = success();
        metadata.addProperty(RESULT, result);
        if (template != null) {
            metadata.addProperty(TEMPLATE_ID, template.getId());
            if (template.getContentType() != null) {
                metadata.addProperty(CONTENT_TYPE, template.getContentType());
            }
        }
        return metadata.toString();
    }

    static String metadataResult(Result result, Gson gson) {
        JsonObject metadata = success();
        metadata.add(RESULT, gson.toJsonTree(result));
        return metadata.toString();
    }

    static JsonObject getBodyAsJsonObject(RoutingContext ctx) {
        JsonElement input = getBodyAsJson(ctx);
        if (input != null && input.isJsonObject()) {
            return input.getAsJsonObject();
        }
        return null;
    }

    static JsonElement getBodyAsJson(RoutingContext ctx) {
        try {
            return new JsonParser().parse(ctx.getBodyAsString());
        } catch (JsonSyntaxException e) {
            LOGGER.warn("Malformed JSON input", e);
            return null;
        }
    }

    static JsonObject success() {
        return success(null);
    }

    static JsonObject success(String msg, Object... params) {
        JsonObject success = response(msg, params);
        success.addProperty(CODE, SUCCESS);
        return success;
    }

    static JsonObject failure(String msg, Object... params) {
        JsonObject failure = response(msg, params);
        failure.addProperty(CODE, FAILURE);
        return failure;
    }

    static JsonObject response(String msg, Object... params) {
        JsonObject response = new JsonObject();
        // Server time
        response.addProperty(TIME, LocalDateTime.now().toString());
        if (msg != null) {
            response.addProperty(MSG,
                    params.length > 0 ? String.format(msg, params) : msg);
        }
        return response;
    }

    static String asyncResult(Long resultId) {
        JsonObject metadata = success();
        metadata.addProperty(RESULT_ID, resultId);
        return metadata.toString();
    }

    static void ok(RoutingContext ctx, String chunk) {
        ctx.response().setStatusCode(200).end(chunk);
    }

    static HttpServerResponse ok(RoutingContext ctx) {
        return ctx.response().setStatusCode(200);
    }

    static void badRequest(RoutingContext ctx, String chunk) {
        ctx.response().setStatusCode(400)
                .putHeader(HEADER_CONTENT_TYPE, APP_JSON).end(chunk);
    }

    static void templateNotFound(RoutingContext ctx, String id) {
        notFound(ctx, failure("Template not found for id: %s", id).toString());
    }

    static void notFound(RoutingContext ctx, String chunk) {
        ctx.response().setStatusCode(404)
                .putHeader(HEADER_CONTENT_TYPE, APP_JSON).end(chunk);
    }

    private static void internalServerError(RoutingContext ctx, String chunk) {
        ctx.response().setStatusCode(500)
                .putHeader(HEADER_CONTENT_TYPE, APP_JSON).end(chunk);
    }

    static void renderingError(RoutingContext ctx, String id) {
        internalServerError(ctx,
                failure("Error rendering template with id: %s", id).toString());
    }

    enum ResultType {

        RAW, METADATA;

        static ResultType of(JsonElement element) {
            return element != null ? of(element.getAsString()) : RAW;
        }

        static ResultType of(String value) {
            if (RAW.toString().toLowerCase().equals(value)) {
                return RAW;
            } else if (METADATA.toString().toLowerCase().equals(value)) {
                return METADATA;
            }
            // The default value
            return RAW;
        }

    }

}
