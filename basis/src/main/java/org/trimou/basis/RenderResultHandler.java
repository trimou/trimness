package org.trimou.basis;

import static io.vertx.core.http.HttpMethod.GET;
import static org.jboss.weld.vertx.web.WebRoute.HandlerType.BLOCKING;
import static org.trimou.basis.Strings.APP_JSON;
import static org.trimou.basis.Strings.ID;
import static org.trimou.basis.Strings.RESULT_TYPE;

import javax.inject.Inject;

import org.jboss.weld.vertx.web.WebRoute;
import org.trimou.basis.Resources.ResultType;

import com.google.gson.Gson;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * Pick up the result of an async rendering.
 */
@WebRoute(value = "/render/:id", type = BLOCKING, methods = GET, consumes = APP_JSON)
public class RenderResultHandler implements Handler<RoutingContext> {

    @Inject
    private ResultRepository resultRepository;

    @Inject
    private Gson gson;

    @Override
    public void handle(RoutingContext ctx) {

        Long id = getId(ctx);
        if (id == null) {
            Resources
                    .badRequest(ctx,
                            Resources
                                    .failure("Invalid result id: %s",
                                            ctx.request().getParam(ID))
                                    .toString());
            return;
        }

        Result result = resultRepository.get(id);

        if (result == null) {
            Resources.notFound(ctx, Resources
                    .failure("Result not found for id: %s", id).toString());
        }

        if (!result.isComplete()) {
            Resources.ok(ctx, Resources
                    .success("Result %s not complete yet", id).toString());
            return;
        }

        ResultType resultType = ResultType
                .of(ctx.request().getParam(RESULT_TYPE));

        switch (resultType) {
        case RAW:
            Resources.ok(ctx, result.getOutput());
            break;
        case METADATA:
            Resources.ok(ctx, Resources.metadataResult(result, gson));
        default:
            throw new IllegalStateException(
                    "Unsupported result type: " + resultType);
        }
    }

    private Long getId(RoutingContext ctx) {
        try {
            return Long.valueOf(ctx.request().getParam(ID));
        } catch (NumberFormatException e) {
            return null;
        }
    }

}