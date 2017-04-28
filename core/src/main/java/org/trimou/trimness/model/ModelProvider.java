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
package org.trimou.trimness.model;

import java.util.Map;

import org.trimou.trimness.render.RenderingContext;

/**
 * Provides data model for templates.
 *
 * @author Martin Kouba
 */
public interface ModelProvider {

    /**
     * The namespace must be unique. Value of <tt>model</tt> is reserved for the
     * data model passed along with the render request. The namespace is used in
     * templates - all entries returned from {@link #getModel(String)}} are put
     * into a map which is accessible under the namespace key.
     *
     * @return the namespace
     */
    String getNamespace();

    /**
     *
     * @param context
     * @return the immutable data model, may be <code>null</code>
     */
    Map<String, Object> getModel(RenderingContext context);

}
