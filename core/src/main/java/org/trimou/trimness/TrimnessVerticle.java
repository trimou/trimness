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
import org.trimou.trimness.config.Configuration;
import org.trimou.trimness.config.TrimnessKey;
import org.trimou.trimness.model.ModelProvider;
import org.trimou.util.Strings;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

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
                TrimnessTemplateLocator basisTemplateLocator = container.select(TrimnessTemplateLocator.class).get();

                Configuration configuration = container.select(Configuration.class).get();

                // Self check
                Set<String> namespaces = new HashSet<>();
                namespaces.add("data");
                for (ModelProvider provider : container.select(ModelProvider.class)) {
                    if (namespaces.contains(provider.getNamespace())) {
                        throw new IllegalStateException("Non-unique namespace detected: " + provider.getNamespace());
                    }
                    namespaces.add(provider.getNamespace());
                }

                // Build template engine
                MustacheEngineBuilder builder = MustacheEngineBuilder.newBuilder();
                builder.addTemplateLocator(basisTemplateLocator);
                builder.addTemplateLocator(
                        MapTemplateLocator.builder().put(MustacheEngineProvider.TEST_TEMPLATE, "{{this}}").build());
                MustacheEngine engine = builder.build();
                container.select(MustacheEngineProvider.class).get().setMustacheEngine(engine);

                // Start web server
                vertx.createHttpServer().requestHandler(weldVerticle.createRouter()::accept).listen(
                        configuration.getIntegerValue(TrimnessKey.PORT),
                        configuration.getStringValue(TrimnessKey.HOST));

                String version = null;
                Properties buildProperties = getBuildProperties();
                if (buildProperties != null) {
                    version = buildProperties.getProperty("version");
                }
                if (Strings.isEmpty(version)) {
                    version = "SNAPSHOT";
                }

                LOGGER.info(
                        "\n=========================================\nTrimness {0} verticle started:\n{1}\n=========================================",
                        version,
                        StreamSupport.stream(configuration.spliterator(), false)
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
