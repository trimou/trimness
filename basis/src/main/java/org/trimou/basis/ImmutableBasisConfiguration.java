package org.trimou.basis;

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
public class ImmutableBasisConfiguration implements BasisConfiguration {

    private static final String RESOURCE_FILE = "/trimou-basis.properties";

    private final Map<String, Object> properties;

    /**
     *
     * @param properties
     */
    ImmutableBasisConfiguration(Map<String, Object> properties) {
        this.properties = ImmutableMap.copyOf(properties);
    }

    public <T extends ConfigurationKey> Long getLongValue(T key) {
        return (Long) properties.getOrDefault(key.get(), key.getDefaultValue());
    }

    public <T extends ConfigurationKey> Integer getIntegerValue(T key) {
        return (Integer) properties.getOrDefault(key.get(),
                key.getDefaultValue());
    }

    public <T extends ConfigurationKey> String getStringValue(T key) {
        return properties.getOrDefault(key.get(), key.getDefaultValue())
                .toString();
    }

    public <T extends ConfigurationKey> Boolean getBooleanValue(T key) {
        return (Boolean) properties.getOrDefault(key.get(),
                key.getDefaultValue());
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
        throw new IllegalStateException(
                "Unsupported default value type: " + defaultValueType);
    }

    /**
     *
     * @param keys
     */
    static Map<String, Object> initPropertiesMap(Set<ConfigurationKey> keys) {

        Map<String, Object> properties = new HashMap<>();
        Properties resourceProperties = new Properties();

        try {
            InputStream in = ImmutableBasisConfiguration.class
                    .getResourceAsStream(RESOURCE_FILE);
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
                    value = convertConfigValue(
                            configKey.getDefaultValue().getClass(), value);
                } catch (Exception e) {
                    throw new IllegalStateException(
                            "Invalid config property value for " + configKey,
                            e);
                }
            } else {
                value = configKey.getDefaultValue();
            }
            properties.put(key, value);
        }
        return properties;
    }

}
