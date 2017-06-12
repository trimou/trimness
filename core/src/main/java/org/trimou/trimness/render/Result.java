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

import org.trimou.trimness.util.WithId;

/**
 * Represents a result of a rendering request.
 *
 * @author Martin Kouba
 */
public interface Result extends WithId {

    /**
     *
     * @return the time the result was created
     */
    Long getCreated();

    /**
     *
     * @return the time the result was completed, may be <code>null</code>
     */
    Long getCompleted();

    /**
     *
     * @return the code
     */
    Status getStatus();

    /**
     *
     * @return the rendered output or the error message in case of failure
     */
    String getValue();

    /**
     *
     * @return the template id
     */
    String getTemplateId();

    /**
     *
     * @return the content type, may be null
     */
    String getContentType();

    /**
     * If the result is already complete an {@link IllegalStateException} is
     * thrown.
     *
     * @param errorMessage
     */
    void fail(String errorMessage);

    /**
     * If the result is already complete an {@link IllegalStateException} is
     * thrown.
     *
     * @param output
     */
    void complete(String output);

    /**
     *
     * @return
     */
    default boolean isComplete() {
        return !Status.INCOMPLETE.equals(getStatus());
    }

    /**
     *
     * @return
     */
    default boolean isSucess() {
        return Status.SUCESS.equals(getStatus());
    }

    /**
     *
     * @return
     */
    default boolean isFailure() {
        return Status.FAILURE.equals(getStatus());
    }

    public enum Status {
        SUCESS, FAILURE, INCOMPLETE
    }

}
