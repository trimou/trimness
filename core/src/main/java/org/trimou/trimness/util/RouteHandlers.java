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
import static org.trimou.trimness.util.Strings.HEADER_CONTENT_TYPE;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 *
 * @author Martin Kouba
 */
public final class RouteHandlers {

    private RouteHandlers() {
    }

    public static void ok(RoutingContext ctx, String chunk) {
        ok(ctx).end(chunk);
    }

    public static HttpServerResponse ok(RoutingContext ctx) {
        return ctx.response().setStatusCode(200);
    }

    public static void badRequest(RoutingContext ctx, String chunk) {
        ctx.response().setStatusCode(400).putHeader(HEADER_CONTENT_TYPE, APP_JSON).end(chunk);
    }

    public static void templateNotFound(RoutingContext ctx, String id) {
        notFound(ctx, Jsons.message("Template not found for id: %s", id).build().toString());
    }

    public static void notFound(RoutingContext ctx, String chunk) {
        ctx.response().setStatusCode(404).putHeader(HEADER_CONTENT_TYPE, APP_JSON).end(chunk);
    }

    public static void internalServerError(RoutingContext ctx, String chunk) {
        ctx.response().setStatusCode(500).putHeader(HEADER_CONTENT_TYPE, APP_JSON).end(chunk);
    }

}
