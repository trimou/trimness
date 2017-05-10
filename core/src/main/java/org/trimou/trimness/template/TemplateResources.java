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
package org.trimou.trimness.template;

import static org.trimou.trimness.util.Requests.badRequest;
import static org.trimou.trimness.util.Strings.APP_JSON;
import static org.trimou.trimness.util.Strings.HEADER_CONTENT_TYPE;
import static org.trimou.trimness.util.Strings.ID;

import javax.inject.Inject;
import javax.json.JsonObjectBuilder;

import org.jboss.weld.vertx.web.WebRoute;
import org.jboss.weld.vertx.web.WebRoute.HandlerType;
import org.trimou.trimness.util.Requests;
import org.trimou.trimness.util.Strings;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

/**
 *
 * @author Martin Kouba
 */
public class TemplateResources {

    @WebRoute(value = "/template/:id", methods = HttpMethod.GET, type = HandlerType.BLOCKING, produces = APP_JSON)
    public static class TemplateHandler implements Handler<RoutingContext> {

        @Inject
        private TemplateCache templateCache;

        @Override
        public void handle(RoutingContext ctx) {

            String id = ctx.request().getParam(ID);

            if (id == null) {
                badRequest(ctx, "Template id must be set");
                return;
            }

            Template template = templateCache.get(id);

            if (template == null) {
                Requests.templateNotFound(ctx, id);
                return;
            }

            JsonObjectBuilder response = Requests.success();
            response.add(ID, id);
            response.add(Strings.CONTENT, template.getContent());
            if (template.getContentType() != null) {
                response.add(Strings.CONTENT_TYPE, template.getContentType());
            }
            ctx.response().putHeader(HEADER_CONTENT_TYPE, APP_JSON).setStatusCode(200).end(response.build().toString());
        }

    }

}
