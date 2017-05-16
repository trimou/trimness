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

import java.util.Collections;
import java.util.Set;

import org.trimou.engine.locator.TemplateLocator;
import org.trimou.engine.priority.WithPriority;
import org.trimou.engine.validation.Validateable;
import org.trimou.trimness.util.WithId;

/**
 * The template provider is responsible for looking up the templates and
 * loading the contents. There might be several template providers installed.
 * Providers with higher priority are queried first.
 * <p>
 * An invalid repository is not considered when performing the lookup of a
 * template.
 * </p>
 * <p>
 * If a template managed by this provider is modified/removed, an event of
 * type {@link Change} should be fired so that
 * {@link TemplateCache} is able to invalidate the cached entries properly.
 * </p>
 *
 * @author Martin Kouba
 * @see Template
 * @see TemplateCache
 */
public interface TemplateProvider extends WithId, WithPriority, Validateable {

    /**
     *
     * @param id
     * @return the template with the given id or <code>null</code>
     */
    Template get(String id);

    /**
     *
     * @return the set of all available template identifiers
     * @see TemplateLocator#getAllIdentifiers()
     */
    default Set<String> getAvailableTemplateIds() {
        return Collections.emptySet();
    }

    /**
     * Modification or removal.
     */
    interface Change {

        /**
         *
         * @return the provider id
         */
        String getProviderId();

        /**
         *
         * @return the template id
         */
        String getTemplateId();

    }

}
