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

import java.util.function.Predicate;

/**
 *
 * @author Martin Kouba
 */
public class SimpleResultLinkDefinition implements ResultLinkDefinition {

    private final String id;

    private final Predicate<RenderRequest> predicate;

    /**
     *
     * @param id
     * @param predicate
     */
    public SimpleResultLinkDefinition(String id, Predicate<RenderRequest> predicate) {
        this.id = id;
        this.predicate = predicate;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean canUpdate(RenderRequest completedResult) {
        return predicate.test(completedResult);
    }

}
