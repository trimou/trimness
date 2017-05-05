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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.trimou.engine.MustacheEngine;
import org.trimou.engine.config.EngineConfigurationKey;
import org.trimou.engine.priority.Priorities;
import org.trimou.trimness.util.CompositeComponent;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Simple computing (lazy loading) cache of templates.
 * <p>
 * Makes use of all valid template providers. Providers with higher priority are
 * queried first.
 * </p>
 *
 * @author Martin Kouba
 * @see TemplateProvider
 */
@Typed(TemplateCache.class)
@ApplicationScoped
public class TemplateCache extends CompositeComponent<TemplateProvider> implements TemplateProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateCache.class.getName());

    @Inject
    private MustacheEngine engine;

    private final ConcurrentMap<String, Optional<Template>> cache;

    // Make it proxyable
    TemplateCache() {
        this.cache = null;
    }

    @Inject
    public TemplateCache(Instance<TemplateProvider> repositories) {
        super(repositories, Priorities.higherFirst());
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public Template get(String id) {
        return cache.computeIfAbsent(id, this::find).orElse(null);
    }

    @Override
    public Set<String> getAvailableTemplateIds() {
        Set<String> ids = new HashSet<>();
        for (TemplateProvider repository : components) {
            ids.addAll(repository.getAvailableTemplateIds());
        }
        return ids;
    }

    public boolean remove(String templateId) {
        return this.cache.remove(templateId) != null;
    }

    public void clear() {
        this.cache.clear();
    }

    void onTemplateChange(@Observes Change change) {
        Optional<Template> removed = cache.remove(change.getTemplateId());
        if (removed != null && removed.isPresent()
                && engine.getConfiguration().getBooleanPropertyValue(EngineConfigurationKey.TEMPLATE_CACHE_ENABLED)
                && !engine.getConfiguration().getBooleanPropertyValue(EngineConfigurationKey.DEBUG_MODE)) {
            // Then invalidate the compiled template
            LOGGER.debug("Invalidating {0} from {1}", change.getTemplateId(), change.getProviderId());
            engine.invalidateTemplateCache((templateName) -> templateName.equals(change.getTemplateId()));
        }
    }

    private Optional<Template> find(String id) {
        Template template = null;
        for (TemplateProvider repository : components) {
            template = repository.get(id);
            if (template != null) {
                break;
            }
        }
        return Optional.ofNullable(template);
    }

}
