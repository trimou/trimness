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
package org.trimou.trimness.model;

import static org.trimou.trimness.util.Strings.CONFIG;
import static org.trimou.trimness.util.Strings.TEMPLATE_ID;
import static org.trimou.trimness.util.Strings.TIME;

import java.time.LocalDateTime;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.trimou.engine.resolver.Mapper;
import org.trimou.trimness.config.Configuration;
import org.trimou.trimness.config.Key;
import org.trimou.util.ImmutableMap;

/**
 * Provides various metadata.
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class MetadataModelProvider implements ModelProvider {

    public static final String NAMESPACE = "meta";

    @Inject
    private Configuration configuration;

    private ConfigurationModel configModel;

    @PostConstruct
    void init() {
        configModel = new ConfigurationModel();
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public void handle(ModelRequest request) {
        request.complete(ImmutableMap.<String, Object>builder()
                // System time
                .put(TIME, LocalDateTime.now())
                // Template id
                .put(TEMPLATE_ID, request.getRenderRequest().getTemplate().getId())
                // Trimness configuration
                .put(CONFIG, configModel).build());
    }

    class ConfigurationModel implements Mapper, Iterable<Key> {

        @Override
        public Iterator<Key> iterator() {
            return configuration.iterator();
        }

        @Override
        public Object get(String key) {
            for (Key configKey : configuration) {
                if (configKey.get().equals(key) || configKey.getEnvKey().equals(key) || configKey.get().endsWith(key)) {
                    return configuration.getValue(configKey);
                }
            }
            return null;
        }

    }

}
