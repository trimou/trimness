package org.trimou.trimness.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class KeyTest {

    @Test
    public void testEnvToKey() {
        assertEquals("org.trimou.trimness.defaultFileEncoding", Key.envToKey(TrimnessKey.DEFAULT_FILE_ENCODING.name()));
        assertEquals("org.trimou.trimness.defaultFileEncoding",
                Key.envToKey(TrimnessKey.DEFAULT_FILE_ENCODING.getEnvKey()));
    }

    @Test
    public void testKeyToEnv() {
        assertEquals(Key.ENV_PREFIX + TrimnessKey.DEFAULT_FILE_ENCODING.name(),
                Key.keyToEnv("org.trimou.trimness.defaultFileEncoding"));
    }

}
