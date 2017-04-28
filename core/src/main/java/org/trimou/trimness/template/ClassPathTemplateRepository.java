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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.trimou.trimness.config.Configuration;
import org.trimou.trimness.config.TrimnessKey;
import org.trimou.util.IOUtils;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Loads templates from the class path.
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class ClassPathTemplateRepository implements TemplateRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassPathTemplateRepository.class.getName());

    @Inject
    private Configuration configuration;

    @Inject
    private CompositeContentTypeExtractor extractor;

    private final ClassLoader classLoader;

    private final ConcurrentMap<String, Optional<Template>> templates;

    public ClassPathTemplateRepository() {
        ClassLoader classLoader = SecurityActions.getContextClassLoader();
        if (classLoader == null) {
            classLoader = SecurityActions.getClassLoader(ClassPathTemplateRepository.class);
        }
        this.classLoader = classLoader;
        this.templates = new ConcurrentHashMap<>();
    }

    @Override
    public Template get(String id) {
        return templates.computeIfAbsent(id, templateId -> {

            try (Reader reader = getReader(id)) {
                if (reader != null) {
                    String contents = IOUtils.toString(reader);
                    Supplier<String> contentLoader = () -> contents;
                    return Optional.of(ImmutableTemplate.of(id, contentLoader, extractor.extract(id, contentLoader)));
                }
            } catch (IOException e) {
                LOGGER.error("Cannot read template for {0}", e, templateId);
            }
            return Optional.empty();

        }).orElse(null);
    }

    @Override
    public Set<Template> getAll() {
        return templates.values().stream().filter((optional) -> optional.isPresent()).map(optional -> optional.get())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid() {
        return !"".equals(configuration.getStringValue(TrimnessKey.CLASSPATH_ROOT).trim());
    }

    private Reader getReader(String id) {
        final String name = configuration.getStringValue(TrimnessKey.CLASSPATH_ROOT) + id;
        Reader reader = null;

        try {
            Enumeration<URL> resources = classLoader.getResources(name);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (reader != null) {
                    LOGGER.info("Duplicit template ignored: {0}", name);
                } else {
                    reader = new InputStreamReader(resource.openStream(),
                            configuration.getStringValue(TrimnessKey.DEFAULT_FILE_ENCODING));
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error while reading {}", e, name);
        }
        return reader;
    }

}
