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

import javax.enterprise.context.Dependent;

import org.trimou.engine.priority.WithPriority;
import org.trimou.engine.validation.Validateable;
import org.trimou.trimness.util.WithId;

/**
 * This component is used to store the results of asynchronous render requests.
 * <p>
 * A valid repository with the highest priority is used. It's recommended to use
 * {@link Dependent} scope - see also {@link DelegateResultRepository}.
 * </p>
 *
 * @author Martin Kouba
 */
public interface ResultRepository extends WithId, WithPriority, Validateable {

    /**
     *
     * @param resultId
     * @return the result with the given id or <code>null</code> if such a
     *         result does not exist
     */
    Result get(String resultId);

    /**
     * The repository may optionally support result links.
     *
     * @param linkId
     * @return the result link or <code>null</code> if no such link exists
     */
    default ResultLink getLink(String linkId) {
        return null;
    }

    /**
     * Initialize an incomplete result. The client must call
     * {@link Result#complete(String)} or {@link Result#fail(String)} to
     * complete the result.
     * <p>
     * The timeout is a hint to the repository. Value of <tt>0</tt> means as
     * long as possible. If it's not possible to hold the result at least for
     * the given time the repository should either log a warning message or
     * throw a runtime exception.
     * </p>
     * <p>
     * If the returned result is completed successfully the repository should
     * update any result link for which a {@link ResultLinkDefinition} exists,
     * such that {@link ResultLinkDefinition#canUpdate(RenderRequest)} returns
     * <code>true</code>.
     * </p>
     *
     * @param renderRequest
     * @return an incomplete result
     */
    Result init(RenderRequest renderRequest);

    /**
     *
     * @param resultId
     * @return <code>true</code> if removed, <code>false</code> otherwise
     */
    boolean remove(String resultId);

    /**
     *
     * @return the number of stored results
     */
    int size();

    /**
     * Remove all results.
     */
    void clear();

}
