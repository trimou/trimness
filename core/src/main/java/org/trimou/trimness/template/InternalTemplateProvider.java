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
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.trimou.trimness.util.Resources;

/**
 * Loads templates from the class path.
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class InternalTemplateProvider implements TemplateProvider {

    private static final String PREFIX = "trimness:";

    @Inject
    private CompositeContentTypeExtractor extractor;

    private final ClassLoader classLoader;

    public InternalTemplateProvider() {
        this.classLoader = SecurityActions.getClassLoader(InternalTemplateProvider.class);
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Template get(String id) {
        if (id.startsWith(PREFIX)) {
            URL resource = Resources.find("META-INF/trimness/" + id.substring(PREFIX.length()), classLoader,
                    "template");
            if (resource != null) {
                Supplier<Reader> contentReader = () -> {
                    try {
                        return new InputStreamReader(resource.openStream(), "UTF-8");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                };
                return ImmutableTemplate.of(id, getId(), contentReader, extractor.extract(id, contentReader));
            }
        }
        return null;
    }

}
