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

import java.util.Optional;

import org.trimou.trimness.template.Template;

/**
 * Render request.
 *
 * @author Martin Kouba
 */
public interface RenderRequest {

    /**
     *
     * @return the template
     */
    Template getTemplate();

    /**
     *
     * @return the timeout
     */
    Optional<Long> getTimeout();

    /**
     * Parameters are passed along with a render request.
     *
     * @param name
     * @return the parameter
     */
    Optional<Object> getParameter(String name);

}
