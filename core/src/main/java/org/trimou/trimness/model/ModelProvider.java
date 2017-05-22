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

import org.trimou.engine.validation.Validateable;
import org.trimou.trimness.config.TrimnessKey;
import org.trimou.trimness.util.WithId;

/**
 * Provides data models for templates. A provider whose {@link #isValid()}
 * returns <code>false</code> is not considered during data model
 * initialization.
 *
 * @author Martin Kouba
 */
public interface ModelProvider extends Validateable, WithId {

    /**
     * The namespace must be unique. Value of <tt>model</tt> is reserved for the
     * data model passed along with the render request. The namespace is used in
     * templates - the result set via {@link ModelRequest#complete(Object)} is
     * accessible under the namespace key.
     *
     * @return the namespace
     */
    String getNamespace();

    /**
     * The provider should either use {@link ModelRequest#complete(Object)} or
     * {@link ModelRequest#complete()} to signal that the given request was
     * processed. If a request is not processed within
     * {@link TrimnessKey#MODEL_INIT_TIMEOUT} the potential result is ignored
     * afterwards.
     * <p>
     * Time-consuming and blocking actions should be performed asynchronously so
     * that multiple providers could be processed in parallel.
     * </p>
     *
     * @param request
     */
    void handle(ModelRequest request);

}
