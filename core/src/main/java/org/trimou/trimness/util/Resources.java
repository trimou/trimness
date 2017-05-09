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
import static org.trimou.trimness.util.Strings.PARAMS;
import static org.trimou.trimness.util.Strings.RESULT;
import static org.trimou.trimness.util.Strings.RESULT_ID;
import static org.trimou.trimness.util.Strings.SUCCESS;
import static org.trimou.trimness.util.Strings.TEMPLATE_ID;
import static org.trimou.trimness.util.Strings.TIME;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonParsingException;

import org.trimou.trimness.render.Result;
import org.trimou.trimness.template.Template;
import org.trimou.util.ImmutableMap;
import org.trimou.util.ImmutableMap.ImmutableMapBuilder;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 *
 * @author Martin Kouba
 */
public final class Resources {

    private Resources() {
    }

    public static String metadataResult(Template template, String result) {
        JsonObjectBuilder metadata = success();
        metadata.add(RESULT, result);
        if (template != null) {
            metadata.add(TEMPLATE_ID, template.getId());
            if (template.getContentType() != null) {
                metadata.add(CONTENT_TYPE, template.getContentType());
            }
        }
        return metadata.build().toString();
    }

    public static String metadataResult(Result result) {
        JsonObjectBuilder metadata = success();
        metadata.add(RESULT,
                Json.createObjectBuilder().add(Strings.ID, result.getId())
                        .add(Strings.CONTENT_TYPE, result.getContentType()).add(Strings.ERROR, result.getError())
                        .add(Strings.OUTPUT, result.getOutput()).add(Strings.TEMPLATE_ID, result.getTemplateId())
                        .add(Strings.CODE, result.getCode().toString()));
        return metadata.build().toString();
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
        return Json.createReader(new StringReader(body)).read();
    }

    public static JsonObjectBuilder success() {
        return success(null);
    }

    public static JsonObjectBuilder success(String msg, Object... params) {
        JsonObjectBuilder success = response(msg, params);
        success.add(CODE, SUCCESS);
        return success;
    }

    public static JsonObjectBuilder failure(String msg, Object... params) {
        JsonObjectBuilder failure = response(msg, params);
        failure.add(CODE, FAILURE);
        return failure;
    }

    public static JsonObjectBuilder response(String msg, Object... params) {
        JsonObjectBuilder response = Json.createObjectBuilder();
        // Server time
        response.add(TIME, LocalDateTime.now().toString());
        if (msg != null) {
            response.add(MSG, params.length > 0 ? String.format(msg, params) : msg);
        }
        return response;
    }

    public static String asyncResult(String resultId) {
        JsonObjectBuilder metadata = success();
        metadata.add(RESULT_ID, resultId);
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
        notFound(ctx, failure("Template not found for id: %s", id).build().toString());
    }

    public static void notFound(RoutingContext ctx, String chunk) {
        ctx.response().setStatusCode(404).putHeader(HEADER_CONTENT_TYPE, APP_JSON).end(chunk);
    }

    public static void internalServerError(RoutingContext ctx, String chunk) {
        ctx.response().setStatusCode(500).putHeader(HEADER_CONTENT_TYPE, APP_JSON).end(chunk);
    }

    public static void renderingError(RoutingContext ctx, String id) {
        internalServerError(ctx, failure("Error rendering template with id: %s", id).toString());
    }

    public static Map<String, Object> initParams(JsonObject input) {
        JsonValue params = input.get(PARAMS);
        if (params == null || !ValueType.OBJECT.equals(params.getValueType())) {
            return Collections.emptyMap();
        }
        ImmutableMapBuilder<String, Object> builder = ImmutableMap.builder();
        for (Map.Entry<String, JsonValue> param : ((JsonObject) params).entrySet()) {
            builder.put(param.getKey(), param.getValue());
        }
        return builder.build();
    }

    public enum ResultType {

        RAW, METADATA;

        public static ResultType of(JsonValue element) {
            return element != null && element instanceof JsonString ? of(((JsonString) element).getString()) : RAW;
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
