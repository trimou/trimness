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

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 *
 * @author Martin Kouba
 */
@Dependent
public class ConfigurationInitializer {

    private ImmutableConfiguration configuration;

    @Inject
    Instance<KeySource> keySources;

    @PostConstruct
    void init() {
        Set<Key> keys = new HashSet<>();
        for (Key key : TrimnessKey.values()) {
            keys.add(key);
        }
        for (KeySource source : keySources) {
            for (Key key : source.getKeys()) {
                checkConflict(keys, key);
                keys.add(key);
            }
        }
        this.configuration = ImmutableConfiguration.init(keys);
    }

    private void checkConflict(Set<Key> keys, Key candidate) {
        for (Key key : keys) {
            if (key.get().equals(candidate.get())) {
                throw new IllegalStateException(String.format("Configuration key conflict between %s and %s for %s",
                        key, candidate, key.get()));
            }
            if (key.getEnvKey().equals(candidate.getEnvKey())) {
                throw new IllegalStateException(String.format("Configuration key conflict between %s and %s for env %s",
                        key, candidate, key.getEnvKey()));
            }
        }
    }

    @ApplicationScoped
    @Produces
    Configuration provideConfiguration() {
        return configuration;
    }

}
