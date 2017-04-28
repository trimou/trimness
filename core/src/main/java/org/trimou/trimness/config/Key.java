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

import org.trimou.util.Strings;

/**
 * Represents a configuration key.
 *
 * @author Martin Kouba
 */
public interface Key {

    String KEY_PREFIX = "org.trimou.trimness";

    String ENV_PREFIX = "TRIMNESS_";

    /**
     * Any key should start with {@value #KEY_PREFIX}.
     *
     * @return the key itself, used when processing system properties and
     *         properties file
     */
    String get();

    /**
     * Any env variable should start with {@value #ENV_PREFIX}.
     *
     * @return the key for environment variable
     */
    default String getEnvKey() {
        return keyToEnv(get());
    }

    /**
     * The set of supported value types which can be automatically converted
     * consist of {@link String}, {@link Boolean}, {@link Integer} and
     * {@link Long}.
     *
     * @return the default value
     */
    Object getDefaultValue();

    /**
     * A simple conversion util method. An env key such as
     * <tt>DEFAULT_RESULT_TIMEOUT</tt> or
     * <tt>TRIMNESS_DEFAULT_RESULT_TIMEOUT</tt> is transformed to
     * <tt>org.trimou.trimness.defaultResultTimeout</tt>.
     *
     * @param envKey
     * @return the key
     */
    static String envToKey(String envKey) {
        if (envKey.startsWith(ENV_PREFIX)) {
            envKey = envKey.substring(ENV_PREFIX.length());
        }
        StringBuilder builder = new StringBuilder();
        builder.append(KEY_PREFIX);
        builder.append(Strings.DOT);
        char[] envKeyChars = envKey.toCharArray();
        for (int i = 0; i < envKeyChars.length; i++) {
            if (envKeyChars[i] == '_') {
                i++;
                builder.append(Character.toUpperCase(envKeyChars[i]));
            } else {
                builder.append(Character.toLowerCase(envKeyChars[i]));
            }
        }
        return builder.toString();
    }

    /**
     * A simple conversion util method. A key such as
     * <tt>org.trimou.trimness.defaultResultTimeout</tt> is converted to
     * <tt>TRIMNESS_DEFAULT_RESULT_TIMEOUT</tt>.
     *
     * @param key
     * @return the env variable
     */
    static String keyToEnv(String key) {
        if (key.startsWith(KEY_PREFIX)) {
            key = key.substring(KEY_PREFIX.length() + 1);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(ENV_PREFIX);
        for (char keyChar : key.toCharArray()) {
            if (!Character.isUpperCase(keyChar)) {
                builder.append(Character.toUpperCase(keyChar));
            } else {
                builder.append(Strings.UNDERSCORE);
                builder.append(Character.toUpperCase(keyChar));
            }
        }
        return builder.toString();
    }

}
