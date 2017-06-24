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

import static io.vertx.core.http.HttpMethod.DELETE;
import static io.vertx.core.http.HttpMethod.GET;
import static org.jboss.weld.vertx.web.WebRoute.HandlerType.BLOCKING;
import static org.trimou.trimness.util.Strings.HEADER_CONTENT_TYPE;
import static org.trimou.trimness.util.Strings.ID;
import static org.trimou.trimness.util.Strings.RESULT_TYPE;

import javax.inject.Inject;

import org.jboss.weld.vertx.web.WebRoute;
import org.trimou.trimness.util.Jsons;
import org.trimou.trimness.util.RouteHandlers;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

/**
 *
 * @author Martin Kouba
 */
public class ResultHandlers {

    @WebRoute(value = "/result/:id", type = BLOCKING, methods = GET)
    public static class GetHandler implements Handler<RoutingContext> {

        @Inject
        private ResultLogic resultLogic;

        @Override
        public void handle(RoutingContext ctx) {

            String resultTypeParam = ctx.request().getParam(RESULT_TYPE);
            if (resultTypeParam == null) {
                resultTypeParam = ctx.get(RESULT_TYPE);
            }
            resultLogic.get(ctx.request().getParam(ID), resultTypeParam, (result, contentType) -> {
                HttpServerResponse response = RouteHandlers.ok(ctx);
                if (contentType != null) {
                    response.putHeader(HEADER_CONTENT_TYPE, contentType);
                }
                response.end(result);
            }, (code, message) -> {
                switch (code) {
                case Codes.CODE_ID_NOT_SET:
                case Codes.CODE_INVALID_RESULT_TYPE:
                    RouteHandlers.badRequest(ctx, Jsons.message(message).build().toString());
                    break;
                case Codes.CODE_NOT_FOUND:
                    RouteHandlers.notFound(ctx, message);
                    break;
                default:
                    RouteHandlers.internalServerError(ctx, message);
                }
            });
        }

    }

    @WebRoute(value = "/result/:id", type = BLOCKING, methods = DELETE)
    public static class RemoveHandler implements Handler<RoutingContext> {

        @Inject
        private ResultLogic resultLogic;

        @Override
        public void handle(RoutingContext ctx) {
            resultLogic.remove(ctx.request().getParam(ID), (result, contentType) -> {
                HttpServerResponse response = RouteHandlers.ok(ctx);
                if (contentType != null) {
                    response.putHeader(HEADER_CONTENT_TYPE, contentType);
                }
                response.end(result);
            }, (code, message) -> {
                switch (code) {
                case Codes.CODE_ID_NOT_SET:
                    RouteHandlers.badRequest(ctx, Jsons.message(message).build().toString());
                    break;
                case Codes.CODE_NOT_FOUND:
                    RouteHandlers.notFound(ctx, message);
                    break;
                default:
                    RouteHandlers.internalServerError(ctx, message);
                }
            });
        }

    }

    @WebRoute(value = "/result/link/:id", type = BLOCKING, methods = GET)
    public static class GetLinkHandler implements Handler<RoutingContext> {

        private static final Logger LOGGER = LoggerFactory.getLogger(GetLinkHandler.class);

        @Inject
        private ResultLogic resultLogic;

        @Override
        public void handle(RoutingContext ctx) {

            resultLogic.getLink(ctx.request().getParam(ID), (link) -> {
                String path = "/result/" + link.getResultId();
                LOGGER.info("Result link {0} found reroute to {1}", link, path);
                ctx.put(RESULT_TYPE, ctx.request().getParam(RESULT_TYPE));
                ctx.reroute(path);
            }, (code, message) -> {
                switch (code) {
                case Codes.CODE_ID_NOT_SET:
                    RouteHandlers.badRequest(ctx, Jsons.message(message).build().toString());
                    break;
                case Codes.CODE_NOT_FOUND:
                    RouteHandlers.notFound(ctx, message);
                    break;
                default:
                    RouteHandlers.internalServerError(ctx, message);
                }
            });
        }

    }

}
