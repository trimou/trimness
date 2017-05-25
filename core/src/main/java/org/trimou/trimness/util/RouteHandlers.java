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
import static org.trimou.trimness.util.Strings.CONTENT_TYPE;
import static org.trimou.trimness.util.Strings.HEADER_CONTENT_TYPE;
import static org.trimou.trimness.util.Strings.ID;
import static org.trimou.trimness.util.Strings.MSG;
import static org.trimou.trimness.util.Strings.OUTPUT;
import static org.trimou.trimness.util.Strings.PARAMS;
import static org.trimou.trimness.util.Strings.RESULT;
import static org.trimou.trimness.util.Strings.RESULT_ID;
import static org.trimou.trimness.util.Strings.STATUS;
import static org.trimou.trimness.util.Strings.TEMPLATE_ID;
import static org.trimou.trimness.util.Strings.TIME;
import static org.trimou.trimness.util.Strings.TIMEOUT;

import java.io.StringReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonParsingException;

import org.trimou.trimness.render.RenderRequest;
import org.trimou.trimness.render.Result;
import org.trimou.trimness.template.Template;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 *
 * @author Martin Kouba
 */
public final class RouteHandlers {

    private RouteHandlers() {
    }

    public static String metadataResult(Template template, String output) {
        JsonObjectBuilder metadata = empty();
        metadata.add(OUTPUT, output);
        metadata.add(TEMPLATE_ID, template.getId());
        if (template.getContentType() != null) {
            metadata.add(CONTENT_TYPE, template.getContentType());
        }
        return metadata.build().toString();
    }

    public static String metadataResult(Result result) {
        JsonObjectBuilder builder = Jsons.objectBuilder();
        builder.add(Strings.TEMPLATE_ID, result.getTemplateId());
        builder.add(ID, result.getId());
        builder.add(STATUS, result.getStatus().toString());
        if (result.isFailure()) {
            builder.add(Strings.ERROR, result.getValue());
        } else {
            builder.add(Strings.OUTPUT, result.getValue());
            if (result.getContentType() != null) {
                builder.add(Strings.CONTENT_TYPE, result.getContentType());
            }
        }
        return empty().add(RESULT, builder).build().toString();
    }

    public static JsonObject getBodyAsJsonObject(String body) {
        JsonStructure input = getBodyAsJson(body);
        if (input != null && ValueType.OBJECT.equals(input.getValueType())) {
            return (JsonObject) input;
        }
        return null;
    }

    /**
     *
     * @param body
     * @return the parsed JSON
     * @throws JsonParsingException
     */
    public static JsonStructure getBodyAsJson(String body) {
        return Jsons.reader(new StringReader(body)).read();
    }

    public static JsonObjectBuilder empty() {
        return message(null);
    }

    public static JsonObjectBuilder message(String msg, Object... params) {
        JsonObjectBuilder response = Jsons.objectBuilder();
        // Server time
        response.add(TIME, LocalDateTime.now().toString());
        if (msg != null) {
            response.add(MSG, params.length > 0 ? String.format(msg, params) : msg);
        }
        return response;
    }

    public static String asyncResult(String resultId, RenderRequest renderRequest) {
        JsonObjectBuilder metadata = empty();
        metadata.add(RESULT_ID, resultId);
        metadata.add(TIMEOUT,
                LocalDateTime.ofInstant(Instant.ofEpochMilli(renderRequest.getTime() + renderRequest.getTimeout()),
                        ZoneId.systemDefault()).toString());
        return metadata.build().toString();
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
        notFound(ctx, message("Template not found for id: %s", id).build().toString());
    }

    public static void notFound(RoutingContext ctx, String chunk) {
        ctx.response().setStatusCode(404).putHeader(HEADER_CONTENT_TYPE, APP_JSON).end(chunk);
    }

    public static void internalServerError(RoutingContext ctx, String chunk) {
        ctx.response().setStatusCode(500).putHeader(HEADER_CONTENT_TYPE, APP_JSON).end(chunk);
    }

    public static void renderingError(RoutingContext ctx, String id) {
        internalServerError(ctx, message("Error rendering template with id: %s", id).toString());
    }

    public static JsonObject initParams(JsonObject input) {
        JsonValue params = input.get(PARAMS);
        if (params != null && ValueType.OBJECT.equals(params.getValueType())) {
            return (JsonObject) params;
        }
        return Jsons.EMPTY_OBJECT;
    }

    public enum ResultType {

        RAW, METADATA;

        public static ResultType of(JsonValue element) {
            return element != null && element instanceof JsonString ? of(((JsonString) element).getString()) : RAW;
        }

        public static ResultType of(String value) {
            value = value.toUpperCase();
            if (RAW.toString().equals(value)) {
                return RAW;
            } else if (METADATA.toString().equals(value)) {
                return METADATA;
            }
            // The default value
            return RAW;
        }

    }

}
