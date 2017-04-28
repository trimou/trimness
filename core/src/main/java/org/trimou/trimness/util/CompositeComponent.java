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
package org.trimou.trimness.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.trimou.engine.validation.Validateable;
import org.trimou.util.ImmutableList;

public abstract class CompositeComponent<T extends Validateable> {

    protected final List<T> components;

    protected CompositeComponent() {
        this.components = null;
    }

    protected CompositeComponent(Iterable<T> instances, Comparator<T> comparator) {
        List<T> components = new ArrayList<>();
        for (T component : instances) {
            if (component.isValid()) {
                components.add(component);
            }
        }
        Collections.sort(components, comparator);
        this.components = ImmutableList.copyOf(components);
    }

}
