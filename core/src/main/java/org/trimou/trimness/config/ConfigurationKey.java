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
 *
 * @author Martin Kouba
 */
public interface ConfigurationKey {

    /**
     *
     * @return the key itself
     */
    String get();

    /**
     *
     * @return the key for environment variable
     */
    default String getEnvKey() {
        return get();
    }

    /**
     * The set of supported value types which can be automatically converted
     * consist of {@link String}, {@link Boolean}, {@link Integer} and
     * {@link Long}.
     *
     * @return the default value
     */
    Object getDefaultValue();

}
