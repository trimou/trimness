package org.trimou.trimness.test;

public class Timeouts {

    /**
     * Default operation timeout in ms.
     */
    static final long DEFAULT_TIMEOUT = init("testDefaultTimeout", 5000);

    /**
     * Default global/test timeout in ms.
     */
    static final long DEFAULT_GLOBAL_TIMEOUT = init("testDefaultTimeout",
            10000);

    private static long init(String key, long defaultValue) {
        String property = System.getProperty(key);
        if (property != null) {
            try {
                return Long.valueOf(property);
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

}
