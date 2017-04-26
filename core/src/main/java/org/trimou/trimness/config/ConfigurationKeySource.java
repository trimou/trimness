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

import java.util.Collections;
import java.util.Set;

/**
 * NOTE: Configuration key source may not inject and use BasisConfiguration!
 *
 * @author Martin Kouba
 */
public interface ConfigurationKeySource {

    /**
     *
     * @return the set of configuration keys to discover
     */
    default Set<ConfigurationKey> getConfigurationKeys() {
        return Collections.emptySet();
    }

}
