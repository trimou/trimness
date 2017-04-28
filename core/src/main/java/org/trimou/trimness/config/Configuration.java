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
package org.trimou.trimness.config;

/**
 * Represents an immutable configuration.
 *
 * @author Martin Kouba
 */
public interface Configuration extends Iterable<Key> {

    default Long getLongValue(Key key) {
        return (Long) getValue(key);
    }

    default Integer getIntegerValue(Key key) {
        return (Integer) getValue(key);
    }

    default String getStringValue(Key key) {
        return getValue(key).toString();
    }

    default Boolean getBooleanValue(Key key) {
        return (Boolean) getValue(key);
    }

    Object getValue(Key key);

}
