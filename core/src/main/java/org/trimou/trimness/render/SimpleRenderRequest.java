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

import javax.json.JsonObject;

import org.trimou.trimness.template.Template;

/**
 *
 * @author Martin Kouba
 */
public class SimpleRenderRequest implements RenderRequest {

    private final Long time;

    private final Template template;

    private final Long timeout;

    private final String linkId;

    private final JsonObject parameters;

    /**
     *
     * @param template
     * @param parameters
     */
    public SimpleRenderRequest(Template template, JsonObject parameters) {
        this(template, null, null, parameters);
    }

    /**
     *
     * @param template
     * @param timeout
     * @param linkId
     * @param parameters
     */
    public SimpleRenderRequest(Template template, Long timeout, String linkId, JsonObject parameters) {
        this(System.currentTimeMillis(), template, timeout, linkId, parameters);
    }

    /**
     *
     * @param time
     * @param template
     * @param timeout
     * @param linkId
     * @param parameters
     */
    public SimpleRenderRequest(Long time, Template template, Long timeout, String linkId, JsonObject parameters) {
        this.time = time;
        this.template = template;
        this.timeout = timeout;
        this.linkId = linkId;
        this.parameters = parameters;
    }

    @Override
    public Long getTime() {
        return time;
    }

    @Override
    public Template getTemplate() {
        return template;
    }

    @Override
    public Long getTimeout() {
        return timeout;
    }

    @Override
    public String getLinkId() {
        return linkId;
    }

    @Override
    public JsonObject getParameters() {
        return parameters;
    }

}
