package org.trimou.basis;

/**
 *
 * @author Martin Kouba
 */
public interface BasisConfiguration {

    public <T extends ConfigurationKey> Long getLongValue(T key);

    public <T extends ConfigurationKey> Integer getIntegerValue(T key);

    public <T extends ConfigurationKey> String getStringValue(T key);

    public <T extends ConfigurationKey> Boolean getBooleanValue(T key);

}
