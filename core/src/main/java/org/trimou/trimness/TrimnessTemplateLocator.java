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
import java.io.StringReader;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.trimou.engine.locator.TemplateLocator;
import org.trimou.trimness.template.CompositeTemplateRepository;
import org.trimou.trimness.template.Template;

/**
 *
 * @author Martin Kouba
 */
@Dependent
public class TrimnessTemplateLocator implements TemplateLocator {

    @Inject
    private CompositeTemplateRepository repository;

    @Override
    public Reader locate(String name) {
        Template template = repository.get(name);
        if (template != null) {
            return new StringReader(template.getContent());
        }
        return null;
    }

    @Override
    public Set<String> getAllIdentifiers() {
        return repository.getAll().stream().map((template) -> template.getId()).collect(Collectors.toSet());
    }

}
