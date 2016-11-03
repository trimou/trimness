package org.trimou.basis;

/**
 *
 * @author Martin Kouba
 */
public interface ConfigurationKey {

    /**
     *
     * @return the key itself
     */
    String get();

    /**
     *
     * @return the key for environment variable
     */
    default String getEnvKey() {
        return get();
    }

    /**
     * The set of supported value types which can be automatically converted
     * consist of {@link String}, {@link Boolean}, {@link Integer} and
     * {@link Long}.
     *
     * @return the default value
     */
    Object getDefaultValue();

}
