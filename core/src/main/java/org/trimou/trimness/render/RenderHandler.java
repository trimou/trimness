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
import static org.trimou.trimness.util.Strings.APP_JSON;
import static org.trimou.trimness.util.Strings.HEADER_CONTENT_TYPE;

import javax.inject.Inject;

import org.jboss.weld.vertx.web.WebRoute;
import org.trimou.trimness.util.RouteHandlers;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * Handles route for rendering templates.
 *
 * @author Martin Kouba
 */
@WebRoute(value = "/render", methods = POST, type = BLOCKING, consumes = APP_JSON)
public class RenderHandler implements Handler<RoutingContext> {

    @Inject
    private RenderLogic renderLogic;

    @Override
    public void handle(RoutingContext ctx) {
        renderLogic.render(ctx.getBodyAsString(), (result, contentType) -> {
            HttpServerResponse response = RouteHandlers.ok(ctx);
            if (contentType != null) {
                response.putHeader(HEADER_CONTENT_TYPE, contentType);
            }
            response.end(result);
        }, (code, message) -> {
            switch (code) {
            case Codes.CODE_TEMPLATE_NOT_FOUND:
                RouteHandlers.notFound(ctx, message);
                break;
            case Codes.CODE_INVALID_INPUT:
                RouteHandlers.badRequest(ctx, message);
                break;
            case Codes.CODE_COMPILATION_ERROR:
            case Codes.CODE_RENDER_ERROR:
            default:
                RouteHandlers.internalServerError(ctx, message);
            }
        });
    }

}