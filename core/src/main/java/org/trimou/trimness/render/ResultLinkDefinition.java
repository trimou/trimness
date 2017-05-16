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

import org.trimou.engine.validation.Validateable;
import org.trimou.trimness.util.WithId;

/**
 * {@link Result} link definition.
 * <p>
 * {@link #getId()} represents the link name and must be unique, otherwise the
 * application will fail to bootstrap. {@link #getId()} must also match the
 * <code>^[a-zA-Z_0-9-]{1,50}</code> pattern, otherwise the component is not
 * valid and ignored.
 * </p>
 *
 * @author Martin Kouba
 */
public interface ResultLinkDefinition extends WithId, Validateable {

    /**
     *
     * @param renderRequest
     * @return <code>true</code> if the result link should be updated,
     *         <code>false</code> otherwise
     */
    boolean canUpdate(RenderRequest renderRequest);

}
