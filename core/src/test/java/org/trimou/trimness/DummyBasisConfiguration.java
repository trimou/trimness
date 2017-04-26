package org.trimou.trimness;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import org.trimou.trimness.config.Configuration;
import org.trimou.trimness.config.ConfigurationKey;

@ApplicationScoped
@Priority(1)
@Alternative
public class DummyBasisConfiguration implements Configuration {

    private Map<String, Object> properties;

    @PostConstruct
    public void init() {
        properties = new HashMap<>();
    }

    void put(ConfigurationKey key, Object value) {
        properties.put(key.get(), value);
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

}
