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

import javax.enterprise.context.Dependent;

import org.trimou.trimness.util.Strings;

/**
 *
 * @author Martin Kouba
 */
@Dependent
public class SuffixContentTypeExtractor implements ContentTypeExtractor {

    @Override
    public String extract(String id, Supplier<String> contentLoader) {
        if (id.endsWith(Strings.SUFFIX_HTML) || id.endsWith(Strings.SUFFIX_HTM)) {
            return Strings.TEXT_HTML;
        } else if (id.endsWith(Strings.SUFFIX_JSON)) {
            return Strings.APP_JSON;
        } else if (id.endsWith(Strings.SUFFIX_CSS)) {
            return Strings.TEXT_CSS;
        } else if (id.endsWith(Strings.SUFFIX_TXT)) {
            return Strings.TEXT_PLAIN;
        } else if (id.endsWith(Strings.SUFFIX_JS)) {
            return Strings.APP_JAVASCRIPT;
        }
        return null;
    }

}
