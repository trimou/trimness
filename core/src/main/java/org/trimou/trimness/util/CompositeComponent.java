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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;

import org.trimou.engine.validation.Validateable;
import org.trimou.util.ImmutableList;

/**
 * Components should be either {@link Dependent} or {@link ApplicationScoped}.
 *
 * @author Martin Kouba
 *
 * @param <T>
 */
public abstract class CompositeComponent<T extends Validateable & WithId> implements Iterable<T> {

    protected final List<T> components;

    protected CompositeComponent() {
        this.components = null;
    }

    protected CompositeComponent(Instance<T> instances, Comparator<T> comparator) {
        List<T> components = new ArrayList<>();
        for (T component : instances) {
            if (isComponentValid(component)) {
                components.add(component);
            } else if (destroyInvalid()) {
                instances.destroy(component);
            }
        }
        if (!components.isEmpty()) {
            if (checkUniqueIds() && components.stream().map((e) -> e.getId()).distinct().count() < components.size()) {
                throw new IllegalStateException("Non-unique components found: " + components);
            }
            if (comparator != null) {
                components.sort(comparator);
            }
        }
        this.components = ImmutableList.copyOf(components);
    }

    public List<T> getComponents() {
        return components;
    }

    public boolean isEmpty() {
        return components.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return components.iterator();
    }

    protected boolean checkUniqueIds() {
        return false;
    }

    protected boolean isComponentValid(T component) {
        return component.isValid();
    }

    protected boolean destroyInvalid() {
        return true;
    }

    protected T first() {
        return components.isEmpty() ? null : components.get(0);
    }

}
