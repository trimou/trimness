package org.trimou.basis;

import java.util.HashSet;
import java.util.Set;

import org.jboss.weld.vertx.web.WeldWebVerticle;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.locator.MapTemplateLocator;

import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * The core component which starts CDI container, builds template engine and
 * starts web server.
 *
 * @author Martin Kouba
 */
public class BasisVerticle extends WeldWebVerticle {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(BasisVerticle.class.getName());

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        // Start Weld
        super.start();

        BasisTemplateLocator basisTemplateLocator = container()
                .select(BasisTemplateLocator.class).get();

        BasisConfiguration configuration = container()
                .select(BasisConfiguration.class).get();

        // Self check
        Set<String> namespaces = new HashSet<>();
        namespaces.add("data");
        for (DataItemProvider provider : container()
                .select(DataItemProvider.class)) {
            if (namespaces.contains(provider.getNamespace())) {
                throw new IllegalStateException(
                        "Non-unique namespace detected: "
                                + provider.getNamespace());
            }
            namespaces.add(provider.getNamespace());
        }

        // Build template engine
        MustacheEngineBuilder builder = MustacheEngineBuilder.newBuilder();
        builder.addTemplateLocator(basisTemplateLocator);
        builder.addTemplateLocator(MapTemplateLocator.builder()
                .put(MustacheEngineProvider.TEST_TEMPLATE, "{{this}}").build());
        MustacheEngine engine = builder.build();
        container().select(MustacheEngineProvider.class).get()
                .setMustacheEngine(engine);

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        registerRoutes(router);

        // Start web server
        vertx.createHttpServer().requestHandler(router::accept).listen(
                configuration.getIntegerValue(BasisConfigurationKey.PORT),
                configuration.getStringValue(BasisConfigurationKey.HOST));

        LOGGER.info("Basis verticle started for deployment {0}",
                deploymentID());

        startFuture.complete();
    }

}
