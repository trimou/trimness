package org.trimou.basis;

import static org.trimou.basis.Strings.APP_JSON;
import static org.trimou.basis.Strings.CHECKS;
import static org.trimou.basis.Strings.DESCRIPTION;
import static org.trimou.basis.Strings.FAILURE;
import static org.trimou.basis.Strings.HEADER_CONTENT_TYPE;
import static org.trimou.basis.Strings.ID;
import static org.trimou.basis.Strings.RESULT;
import static org.trimou.basis.Strings.SUCCESS;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.weld.vertx.web.WebRoute;
import org.jboss.weld.vertx.web.WebRoute.HandlerType;
import org.trimou.basis.HealthCheck.Result;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

/**
 *
 * @author Martin Kouba
 */
public class MonitorResources {

    /**
     * A simple health-check resource.
     */
    @WebRoute(value = "/monitor/health", methods = HttpMethod.GET, type = HandlerType.BLOCKING, produces = APP_JSON)
    public static class HealthCheckResource implements Handler<RoutingContext> {

        @Inject
        private Instance<HealthCheck> healthChecks;

        @Override
        public void handle(RoutingContext ctx) {
            boolean isOk = true;
            JsonObject response = new JsonObject();
            JsonArray checks = new JsonArray();
            for (HealthCheck healthCheck : healthChecks) {
                Result result = healthCheck.perform();
                if (!result.isOk()) {
                    isOk = false;
                }
                JsonObject check = new JsonObject();
                check.addProperty(ID, healthCheck.getId());
                check.addProperty(RESULT, result.isOk() ? SUCCESS : FAILURE);
                if (result.hasDetails()) {
                    check.addProperty(DESCRIPTION, result.getDetails());
                }
                checks.add(check);
            }
            response.add(CHECKS, checks);
            response.addProperty(RESULT, isOk ? SUCCESS : FAILURE);
            ctx.response().putHeader(HEADER_CONTENT_TYPE, APP_JSON)
                    .setStatusCode(isOk ? 200 : 503).end(response.toString());
        }

    }

    /**
     * A simple ping resource.
     */
    @WebRoute(value = "/monitor/ping", methods = HttpMethod.HEAD)
    public static class PingResource implements Handler<RoutingContext> {

        @Override
        public void handle(RoutingContext ctx) {
            ctx.response().setStatusCode(200).end();
        }

    }

}
