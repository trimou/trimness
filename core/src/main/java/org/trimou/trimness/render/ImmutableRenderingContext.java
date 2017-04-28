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
package org.trimou.trimness.render;

import java.util.Map;

import org.trimou.trimness.template.Template;

/**
 *
 * @author Martin Kouba
 */
class ImmutableRenderingContext implements RenderingContext {

    static ImmutableRenderingContext of(Template template, Map<String, Object> parameters) {
        return new ImmutableRenderingContext(template, parameters);
    }

    private final Template template;

    private final Map<String, Object> parameters;

    /**
     *
     * @param template
     * @param parameters
     */
    public ImmutableRenderingContext(Template template, Map<String, Object> parameters) {
        this.template = template;
        this.parameters = parameters;
    }

    @Override
    public Template getTemplate() {
        return template;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

}
