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

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.trimou.engine.priority.Priorities;
import org.trimou.trimness.util.CompositeComponent;
import org.trimou.util.ImmutableSet;

/**
 * Collects all template repositories and sorts them by priority.
 *
 * @author Martin Kouba
 */
@Typed(CompositeTemplateRepository.class)
@ApplicationScoped
public class CompositeTemplateRepository extends CompositeComponent<TemplateRepository> implements TemplateRepository {

    // Make it proxyable
    CompositeTemplateRepository() {
    }

    @Inject
    public CompositeTemplateRepository(Instance<TemplateRepository> repositories) {
       super(repositories, Priorities.higherFirst());
    }

    @Override
    public Template get(String id) {
        Template template = null;
        for (TemplateRepository repository : components) {
            template = repository.get(id);
            if (template != null) {
                break;
            }
        }
        return template;
    }

    @Override
    public Set<Template> getAll() {
        ImmutableSet.ImmutableSetBuilder<Template> builder = ImmutableSet.builder();
        for (TemplateRepository repository : components) {
            repository.getAll().forEach((t) -> builder.add(t));
        }
        return builder.build();
    }

}
