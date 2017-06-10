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

import static io.vertx.core.http.HttpMethod.GET;
import static org.trimou.trimness.util.Strings.HEADER_CONTENT_TYPE;
import static org.trimou.trimness.util.Strings.TEXT_HTML;

import javax.inject.Inject;

import org.jboss.weld.vertx.web.WebRoute;
import org.trimou.trimness.util.RouteHandlers;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

/**
 *
 * @author Martin Kouba
 */
@WebRoute(value = "/test", methods = GET)
public class TestFormHandler implements Handler<RoutingContext> {

    @Inject
    Vertx vertx;

    @Override
    public void handle(RoutingContext ctx) {
        vertx.eventBus().send(RenderObserver.ADDR_RENDER, "{ \"templateId\" : \"trimness:test.html\" }", (result) -> {
            if (result.succeeded()) {
                RouteHandlers.ok(ctx).putHeader(HEADER_CONTENT_TYPE, TEXT_HTML).end(result.result().body().toString());
            } else {
                RouteHandlers.internalServerError(ctx, "Unable to render test form" + result.cause());
            }
        });
    }

}
