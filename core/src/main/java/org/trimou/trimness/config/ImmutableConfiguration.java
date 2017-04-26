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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.trimou.util.ImmutableMap;

/**
 *
 * @author Martin Kouba
 */
public class ImmutableConfiguration implements Configuration {

    private static final String RESOURCE_FILE = "/trimness.properties";

    private final Map<String, Object> properties;

    /**
     *
     * @param properties
     */
    ImmutableConfiguration(Map<String, Object> properties) {
        this.properties = ImmutableMap.copyOf(properties);
    }

    public <T extends ConfigurationKey> Long getLongValue(T key) {
        return (Long) properties.getOrDefault(key.get(), key.getDefaultValue());
    }

    public <T extends ConfigurationKey> Integer getIntegerValue(T key) {
        return (Integer) properties.getOrDefault(key.get(), key.getDefaultValue());
    }

    public <T extends ConfigurationKey> String getStringValue(T key) {
        return properties.getOrDefault(key.get(), key.getDefaultValue()).toString();
    }

    public <T extends ConfigurationKey> Boolean getBooleanValue(T key) {
        return (Boolean) properties.getOrDefault(key.get(), key.getDefaultValue());
    }

    static Object convertConfigValue(Class<?> defaultValueType, Object value) {
        if (defaultValueType.equals(String.class)) {
            return value.toString();
        } else if (defaultValueType.equals(Boolean.class)) {
            return Boolean.valueOf(value.toString());
        } else if (defaultValueType.equals(Long.class)) {
            return Long.valueOf(value.toString());
        } else if (defaultValueType.equals(Integer.class)) {
            return Integer.valueOf(value.toString());
        }
        throw new IllegalStateException("Unsupported default value type: " + defaultValueType);
    }

    /**
     *
     * @param keys
     */
    static Map<String, Object> initPropertiesMap(Set<ConfigurationKey> keys) {

        Map<String, Object> properties = new HashMap<>();
        Properties resourceProperties = new Properties();

        try {
            InputStream in = ImmutableConfiguration.class.getResourceAsStream(RESOURCE_FILE);
            if (in != null) {
                try {
                    resourceProperties.load(in);
                } finally {
                    in.close();
                }
            }
        } catch (IOException e) {
            // No-op, file is optional
        }

        for (ConfigurationKey configKey : keys) {

            String key = configKey.get();

            // 1. System property
            Object value = SecurityActions.getSystemProperty(key);
            if (value == null) {
                // 2. Env variable
                value = SecurityActions.getEnv(configKey.getEnvKey());
            }
            if (value == null) {
                // 3. Properties file
                value = resourceProperties.getProperty(key);
            }

            if (value != null) {
                try {
                    value = convertConfigValue(configKey.getDefaultValue().getClass(), value);
                } catch (Exception e) {
                    throw new IllegalStateException("Invalid config property value for " + configKey, e);
                }
            } else {
                value = configKey.getDefaultValue();
            }
            properties.put(key, value);
        }
        return properties;
    }

}
