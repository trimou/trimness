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
package org.trimou.trimness.monitor;

import static org.trimou.trimness.util.Strings.APP_JSON;
import static org.trimou.trimness.util.Strings.CHECKS;
import static org.trimou.trimness.util.Strings.DESCRIPTION;
import static org.trimou.trimness.util.Strings.FAILURE;
import static org.trimou.trimness.util.Strings.HEADER_CONTENT_TYPE;
import static org.trimou.trimness.util.Strings.ID;
import static org.trimou.trimness.util.Strings.RESULT;
import static org.trimou.trimness.util.Strings.SUCCESS;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import org.jboss.weld.vertx.web.WebRoute;
import org.jboss.weld.vertx.web.WebRoute.HandlerType;
import org.trimou.trimness.monitor.HealthCheck.Result;
import org.trimou.trimness.util.Jsons;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

/**
 *
 * @author Martin Kouba
 */
public class MonitorHandlers {

    /**
     * A simple health-check resource.
     */
    @WebRoute(value = "/monitor/health", methods = HttpMethod.GET, type = HandlerType.BLOCKING, produces = APP_JSON)
    public static class HealthCheckHandler implements Handler<RoutingContext> {

        @Inject
        private Instance<HealthCheck> healthChecks;

        @Override
        public void handle(RoutingContext ctx) {
            boolean isOk = true;
            JsonObjectBuilder builder = Jsons.objectBuilder();
            JsonArrayBuilder checks = Jsons.arrayBuilder();
            for (HealthCheck healthCheck : healthChecks) {
                Result result = healthCheck.perform();
                if (!result.isOk()) {
                    isOk = false;
                }
                JsonObjectBuilder check = Jsons.objectBuilder();
                check.add(ID, healthCheck.getId());
                check.add(RESULT, result.isOk() ? SUCCESS : FAILURE);
                if (result.hasDetails()) {
                    check.add(DESCRIPTION, result.getDetails());
                }
                checks.add(check);
            }
            builder.add(CHECKS, checks);
            builder.add(RESULT, isOk ? SUCCESS : FAILURE);
            ctx.response().putHeader(HEADER_CONTENT_TYPE, APP_JSON).setStatusCode(isOk ? 200 : 503)
                    .end(builder.build().toString());
        }

    }

    /**
     * A simple ping resource.
     */
    @WebRoute(value = "/monitor/ping", methods = HttpMethod.HEAD)
    public static class PingHandler implements Handler<RoutingContext> {

        @Override
        public void handle(RoutingContext ctx) {
            ctx.response().setStatusCode(200).end();
        }

    }

}
