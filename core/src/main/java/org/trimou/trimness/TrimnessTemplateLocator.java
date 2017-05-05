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

import java.io.Reader;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.trimou.engine.locator.TemplateLocator;
import org.trimou.trimness.template.Template;
import org.trimou.trimness.template.TemplateCache;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 *
 * @author Martin Kouba
 */
@Dependent
public class TrimnessTemplateLocator implements TemplateLocator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrimnessTemplateLocator.class.getName());

    @Inject
    private TemplateCache templateCache;

    @Override
    public Reader locate(String name) {
        Template template = templateCache.get(name);
        if (template != null) {
            try {
                return template.getContentReader();
            } catch (RuntimeException e) {
                LOGGER.error("Cannot get content reader for {0}", e, template.getId());
            }
        }
        return null;
    }

    @Override
    public Set<String> getAllIdentifiers() {
        return templateCache.getAvailableTemplateIds();
    }

}
