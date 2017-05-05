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

import org.trimou.trimness.template.Template;
import org.trimou.trimness.util.WithId;

/**
 * Any non-default result repository must be an alternative with priority. Only
 * the repository with highest priority is taken into account.
 *
 * @author Martin Kouba
 */
public interface ResultRepository extends WithId {

    /**
     *
     * @param resultId
     * @return the result with the given id or <code>null</code> if such a
     *         result does not exist
     */
    Result get(String resultId);

    /**
     * Initialize an incomplete result.
     *
     * <p>
     * The timeout is a hint to the repository. Value of <tt>0</tt> means as
     * long as possible. If it's not possible to hold the result at least for
     * the given time the repository should either log a warning message or
     * throw a runtime exception.
     * </p>
     *
     * @param template
     * @param timeout
     *            The timeout of the result
     * @return an incomplete result
     * @see Result#complete(String)
     * @see Result#fail(String)
     */
    Result init(Template template, long timeout);

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
