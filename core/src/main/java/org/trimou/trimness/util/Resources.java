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
package org.trimou.trimness.util;

import static org.trimou.trimness.util.Strings.APP_JSON;
import static org.trimou.trimness.util.Strings.CODE;
import static org.trimou.trimness.util.Strings.CONTENT_TYPE;
import static org.trimou.trimness.util.Strings.FAILURE;
import static org.trimou.trimness.util.Strings.HEADER_CONTENT_TYPE;
import static org.trimou.trimness.util.Strings.MSG;
import static org.trimou.trimness.util.Strings.RESULT;
import static org.trimou.trimness.util.Strings.RESULT_ID;
import static org.trimou.trimness.util.Strings.SUCCESS;
import static org.trimou.trimness.util.Strings.TEMPLATE_ID;
import static org.trimou.trimness.util.Strings.TIME;

import java.time.LocalDateTime;

import org.trimou.trimness.render.Result;
import org.trimou.trimness.template.Template;

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
public class Resources {

    private static final Logger LOGGER = LoggerFactory.getLogger(Resources.class.getName());

    public static String metadataResult(Template template, String result) {
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

    public static String metadataResult(Result result, Gson gson) {
        JsonObject metadata = success();
        metadata.add(RESULT, gson.toJsonTree(result));
        return metadata.toString();
    }

    public static JsonObject getBodyAsJsonObject(RoutingContext ctx) {
        JsonElement input = getBodyAsJson(ctx);
        if (input != null && input.isJsonObject()) {
            return input.getAsJsonObject();
        }
        return null;
    }

    public static JsonElement getBodyAsJson(RoutingContext ctx) {
        try {
            return new JsonParser().parse(ctx.getBodyAsString());
        } catch (JsonSyntaxException e) {
            LOGGER.warn("Malformed JSON input", e);
            return null;
        }
    }

    public static JsonObject success() {
        return success(null);
    }

    public static JsonObject success(String msg, Object... params) {
        JsonObject success = response(msg, params);
        success.addProperty(CODE, SUCCESS);
        return success;
    }

    public static JsonObject failure(String msg, Object... params) {
        JsonObject failure = response(msg, params);
        failure.addProperty(CODE, FAILURE);
        return failure;
    }

    public static JsonObject response(String msg, Object... params) {
        JsonObject response = new JsonObject();
        // Server time
        response.addProperty(TIME, LocalDateTime.now().toString());
        if (msg != null) {
            response.addProperty(MSG, params.length > 0 ? String.format(msg, params) : msg);
        }
        return response;
    }

    public static String asyncResult(String resultId) {
        JsonObject metadata = success();
        metadata.addProperty(RESULT_ID, resultId);
        return metadata.toString();
    }

    public static void ok(RoutingContext ctx, String chunk) {
        ctx.response().setStatusCode(200).end(chunk);
    }

    public static HttpServerResponse ok(RoutingContext ctx) {
        return ctx.response().setStatusCode(200);
    }

    public static void badRequest(RoutingContext ctx, String chunk) {
        ctx.response().setStatusCode(400).putHeader(HEADER_CONTENT_TYPE, APP_JSON).end(chunk);
    }

    public static void templateNotFound(RoutingContext ctx, String id) {
        notFound(ctx, failure("Template not found for id: %s", id).toString());
    }

    public static void notFound(RoutingContext ctx, String chunk) {
        ctx.response().setStatusCode(404).putHeader(HEADER_CONTENT_TYPE, APP_JSON).end(chunk);
    }

    private static void internalServerError(RoutingContext ctx, String chunk) {
        ctx.response().setStatusCode(500).putHeader(HEADER_CONTENT_TYPE, APP_JSON).end(chunk);
    }

    public static void renderingError(RoutingContext ctx, String id) {
        internalServerError(ctx, failure("Error rendering template with id: %s", id).toString());
    }

    public enum ResultType {

        RAW, METADATA;

        public static ResultType of(JsonElement element) {
            return element != null ? of(element.getAsString()) : RAW;
        }

        public static ResultType of(String value) {
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
