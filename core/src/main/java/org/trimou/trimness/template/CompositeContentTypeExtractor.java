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

import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.trimou.engine.priority.Priorities;
import org.trimou.trimness.util.CompositeComponent;

/**
 * Collects all content type extractors and sorts them by priority.
 *
 * @author Martin Kouba
 */
@Typed(CompositeContentTypeExtractor.class)
@ApplicationScoped
public class CompositeContentTypeExtractor extends CompositeComponent<ContentTypeExtractor> implements ContentTypeExtractor {

    // Make it proxyable
    CompositeContentTypeExtractor() {
    }

    @Inject
    public CompositeContentTypeExtractor(Instance<ContentTypeExtractor> extractorsInstance) {
        super(extractorsInstance, Priorities.higherFirst());
    }

    @Override
    public String extract(String id, Supplier<String> contentLoader) {
        String contentType = null;
        for (ContentTypeExtractor contentTypeExtractor : components) {
            contentType = contentTypeExtractor.extract(id, contentLoader);
            if (contentType != null) {
                break;
            }
        }
        return contentType;
    }

}
