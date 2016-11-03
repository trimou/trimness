package org.trimou.basis;

import java.io.Reader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.trimou.engine.config.ConfigurationKey;
import org.trimou.engine.locator.TemplateLocator;

/**
 *
 * @author Martin Kouba
 */
public class DummyTemplateLocator implements TemplateLocator {

    private final Set<ConfigurationKey> configurationKeys;

    static public DummyTemplateLocator of(
            ConfigurationKey... configurationKeys) {
        Set<ConfigurationKey> keys = new HashSet<>();
        Collections.addAll(keys, configurationKeys);
        return new DummyTemplateLocator(keys);
    }

    DummyTemplateLocator(Set<ConfigurationKey> configurationKeys) {
        this.configurationKeys = configurationKeys;
    }

    @Override
    public Reader locate(String name) {
        return null;
    }

    @Override
    public Set<ConfigurationKey> getConfigurationKeys() {
        return configurationKeys;
    }

}
