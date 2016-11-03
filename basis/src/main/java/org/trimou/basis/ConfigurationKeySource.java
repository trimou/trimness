package org.trimou.basis;

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
