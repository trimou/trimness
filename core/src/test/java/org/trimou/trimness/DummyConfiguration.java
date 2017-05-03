package org.trimou.trimness;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import org.trimou.trimness.config.Configuration;
import org.trimou.trimness.config.Key;

@ApplicationScoped
@Priority(1)
@Alternative
public class DummyConfiguration implements Configuration {

    private Map<Key, Object> properties;

    @PostConstruct
    public void init() {
        properties = new HashMap<>();
    }

    public void put(Key key, Object value) {
        properties.put(key, value);
    }

    @Override
    public Iterator<Key> iterator() {
        return properties.keySet().iterator();
    }

    @Override
    public Object getValue(Key key) {
        return properties.getOrDefault(key, key.getDefaultValue());
    }

}
