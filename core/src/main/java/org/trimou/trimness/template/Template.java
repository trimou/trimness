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

/**
 * Immutable template.
 *
 * @author Martin Kouba
 */
public interface Template {

    /**
     * Must be unique accross the whole application.
     *
     * @return a template id
     */
    String getId();

    /**
     *
     * @return the contents
     */
    String getContent();

    /**
     *
     * @return the content type, may be <code>null</code>
     */
    String getContentType();

    /**
     *
     * @return <code>true</code> if the content type is set
     */
    default boolean hasContentType() {
        return getContentType() != null;
    }

}
