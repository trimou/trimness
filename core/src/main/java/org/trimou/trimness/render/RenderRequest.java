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
 * Render request.
 *
 * @author Martin Kouba
 */
public interface RenderRequest {

    /**
     *
     * @return the time the request was accepted
     * @see System#currentTimeMillis()
     */
    Long getTime();

    /**
     *
     * @return the template
     */
    Template getTemplate();

    /**
     * Timeout is always set for async requests.
     *
     * @return the timeout, may be <code>null</code>
     */
    Long getTimeout();

    /**
     * The link id must match the <code>^[a-zA-Z_0-9-]{1,60}</code> pattern.
     *
     * @return the id of a link that should be created/updated if the request is
     *         completed sucessfully
     * @see ResultLink
     */
    String getLinkId();

    /**
     * Parameters could be passed along with a render request.
     *
     * @param name
     * @return the parameters
     */
    JsonObject getParameters();

}
