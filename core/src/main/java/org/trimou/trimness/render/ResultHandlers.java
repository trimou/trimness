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
import org.trimou.trimness.util.Requests;
import org.trimou.trimness.util.Requests.ResultType;

import io.vertx.core.Handler;
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
        private DelegateResultRepository resultRepository;

        @Override
        public void handle(RoutingContext ctx) {

            String id = ctx.request().getParam(ID);
            if (id == null) {
                Requests.badRequest(ctx, Requests.failure("Result id must be set").build().toString());
                return;
            }

            Result result = resultRepository.get(id);

            if (result == null) {
                Requests.notFound(ctx, Requests.failure("Result not found for id: %s", id).build().toString());
            } else if (!result.isComplete()) {
                Requests.ok(ctx, Requests.success("Result %s not complete yet", id).build().toString());
            } else if (result.isFailure()) {
                Requests.internalServerError(ctx, Requests.failure(result.getError()).build().toString());
            } else {
                if (result.getContentType() != null) {
                    ctx.response().putHeader(HEADER_CONTENT_TYPE, result.getContentType());
                }

                ResultType resultType = ResultType.of(ctx.request().getParam(RESULT_TYPE));

                switch (resultType) {
                case RAW:
                    Requests.ok(ctx, result.getOutput());
                    break;
                case METADATA:
                    Requests.ok(ctx, Requests.metadataResult(result));
                default:
                    throw new IllegalStateException("Unsupported result type: " + resultType);
                }
            }
        }

    }

    @WebRoute(value = "/result/:id", type = BLOCKING, methods = DELETE)
    public static class RemoveHandler implements Handler<RoutingContext> {

        @Inject
        private DelegateResultRepository resultRepository;

        @Override
        public void handle(RoutingContext ctx) {

            String id = ctx.request().getParam(ID);
            if (id == null) {
                Requests.badRequest(ctx, Requests.failure("Result id must be set").build().toString());
            } else if (resultRepository.remove(id)) {
                Requests.ok(ctx, Requests.success("Result %s removed", id).build().toString());
            } else {
                Requests.notFound(ctx, Requests.failure("Result not found for id: %s", id).build().toString());
            }
        }

    }

    @WebRoute(value = "/result/link/:id", type = BLOCKING, methods = GET)
    public static class GetLinkHandler implements Handler<RoutingContext> {

        private static final Logger LOGGER = LoggerFactory.getLogger(GetLinkHandler.class);

        @Inject
        private DelegateResultRepository resultRepository;

        @Override
        public void handle(RoutingContext ctx) {

            String linkId = ctx.request().getParam(ID);
            if (linkId == null) {
                Requests.badRequest(ctx, Requests.failure("Result link id must be set").build().toString());
                return;
            }

            ResultLink link = resultRepository.getLink(linkId);

            if (link != null) {
                String path = "/result/" + link.getResultId();
                LOGGER.info("Result link {0} found reroute to {1}", link, path);
                ctx.reroute(path);
            } else {
                Requests.notFound(ctx, Requests.failure("Result link does not exits: %s", link).build().toString());
            }
        }

    }

}
