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

import org.trimou.trimness.render.ResultHandlers.GetLinkHandler;
import org.trimou.trimness.util.WithId;

/**
 * Link to a {@link Result}. {@link #getId()} returns the link name/id used in
 * the path.
 *
 * @author Martin Kouba
 * @see GetLinkHandler
 */
public interface ResultLink extends WithId {

    /**
     *
     * @return the result id
     * @see Result#getId()
     */
    String getResultId();

}
