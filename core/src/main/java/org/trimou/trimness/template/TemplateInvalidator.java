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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.trimou.engine.MustacheEngine;
import org.trimou.engine.config.EngineConfigurationKey;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * If a template is modified, the responsible repository should fire an event of type
 * {@link Template} so that it can be invalidated properly.
 *
 * @author Martin Kouba
 * @see TemplateRepository
 */
@ApplicationScoped
public class TemplateInvalidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateInvalidator.class.getName());

    @Inject
    private MustacheEngine engine;

    public void observe(@Observes Template template) {
        if (isInvalidationEnabled()) {
            LOGGER.debug("Invalidating {0}", template);
            engine.invalidateTemplateCache((templateName) -> templateName.equals(template.getId()));
        }
    }

    private boolean isInvalidationEnabled() {
        return engine.getConfiguration().getBooleanPropertyValue(EngineConfigurationKey.TEMPLATE_CACHE_ENABLED)
                && !engine.getConfiguration().getBooleanPropertyValue(EngineConfigurationKey.DEBUG_MODE);
    }

}
