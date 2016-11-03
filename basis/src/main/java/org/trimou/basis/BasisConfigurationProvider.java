package org.trimou.basis;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class BasisConfigurationProvider {

    private ImmutableBasisConfiguration configuration;

    @Inject
    Instance<ConfigurationKeySource> sourceInstance;

    @PostConstruct
    void init() {
        Set<ConfigurationKey> keys = new HashSet<>();
        for (ConfigurationKey key : BasisConfigurationKey.values()) {
            keys.add(key);
        }
        for (ConfigurationKeySource source : sourceInstance) {
            keys.addAll(source.getConfigurationKeys());
        }
        this.configuration = new ImmutableBasisConfiguration(
                ImmutableBasisConfiguration.initPropertiesMap(keys));
    }

    @Produces
    BasisConfiguration provideConfiguration() {
        return configuration;
    }

}
