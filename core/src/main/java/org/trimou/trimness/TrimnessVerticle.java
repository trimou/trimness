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
package org.trimou.trimness;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.vertx.web.WeldWebVerticle;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.locator.MapTemplateLocator;
import org.trimou.handlebars.HelpersBuilder;
import org.trimou.trimness.config.Configuration;
import org.trimou.trimness.config.TrimnessKey;
import org.trimou.trimness.model.ModelProvider;
import org.trimou.trimness.render.DelegateResultRepository;
import org.trimou.trimness.util.Strings;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;

/**
 * The core component which starts CDI container, builds template engine and
 * starts web server.
 *
 * @author Martin Kouba
 */
public class TrimnessVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrimnessVerticle.class.getName());

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        // Start Weld
        final WeldWebVerticle weldVerticle = new WeldWebVerticle();
        vertx.deployVerticle(weldVerticle, (r) -> {
            if (r.succeeded()) {

                WeldContainer container = weldVerticle.container();
                TrimnessTemplateLocator templateLocator = container.select(TrimnessTemplateLocator.class).get();

                Configuration configuration = container.select(Configuration.class).get();

                // Self checks
                Set<String> namespaces = new HashSet<>();
                namespaces.add("data");
                for (ModelProvider provider : container.select(ModelProvider.class)) {
                    if (namespaces.contains(provider.getNamespace())) {
                        throw new IllegalStateException(
                                "Non-unique model provider namespace detected: " + provider.getNamespace());
                    }
                    namespaces.add(provider.getNamespace());
                }
                // Make sure a result repository is available
                container.select(DelegateResultRepository.class).get();

                // Build template engine
                MustacheEngineBuilder builder = MustacheEngineBuilder.newBuilder();
                builder.addTemplateLocator(templateLocator);
                builder.addTemplateLocator(
                        MapTemplateLocator.builder().put(TrimouEngine.TEST_TEMPLATE, "{{this}}").build());
                builder.registerHelpers(HelpersBuilder.extra().build());
                // Make it possible to configure the builder
                container.event().select(MustacheEngineBuilder.class).fire(builder);
                MustacheEngine engine = builder.build();
                container.select(TrimouEngine.class).get().setMustacheEngine(engine);

                // Start web server
                Router router = weldVerticle.createRouter();
                // Setup CORS handler
                router.route().order(-1).handler(CorsHandler.create("*").allowedMethod(HttpMethod.POST)
                        .allowedMethod(HttpMethod.GET).allowedHeader(Strings.HEADER_CONTENT_TYPE));
                vertx.createHttpServer().requestHandler(router::accept).listen(
                        configuration.getIntegerValue(TrimnessKey.PORT),
                        configuration.getStringValue(TrimnessKey.HOST));

                String version = null;
                Properties buildProperties = getBuildProperties();
                if (buildProperties != null) {
                    version = buildProperties.getProperty("version");
                }
                if (version == null || version.isEmpty()) {
                    version = "SNAPSHOT";
                }

                LOGGER.info(
                        "\n=========================================\nTrimness {0} verticle started:\n{1}\n=========================================",
                        version,
                        StreamSupport.stream(configuration.spliterator(), false)
                                .sorted((o1, o2) -> o1.get().compareTo(o2.get()))
                                .map((key) -> key.get() + "=" + configuration.getStringValue(key))
                                .collect(Collectors.joining("\n")));

                startFuture.complete();
            } else {
                startFuture.fail(r.cause());
            }
        });

    }

    private Properties getBuildProperties() {
        try {
            // First try to get trimou-build.properties file
            InputStream in = TrimnessVerticle.class.getResourceAsStream("/trimness-build.properties");
            if (in != null) {
                try {
                    Properties buildProperties = new Properties();
                    buildProperties.load(in);
                    return buildProperties;
                } finally {
                    in.close();
                }
            }
        } catch (IOException ignored) {
        }
        return null;
    }

}
