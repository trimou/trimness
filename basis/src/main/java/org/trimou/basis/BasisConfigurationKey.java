package org.trimou.basis;

import org.trimou.util.Strings;

/**
 *
 * @author Martin Kouba
 */
public enum BasisConfigurationKey implements ConfigurationKey {

    // org.trimout.basis.host
    HOST("localhost"),
    // org.trimout.basis.port
    PORT(8080),
    // org.trimout.basis.defaultResultTimeout
    DEFAULT_RESULT_TIMEOUT(300000l),

    // org.trimout.basis.fsTemplateRepoDir
    FS_TEMPLATE_REPO_DIR(
            System.getProperty("user.dir")
                    + SecurityActions.getSystemProperty("file.separator")
                    + "templates"),
    // org.trimout.basis.fsTemplateRepoScanInterval
    FS_TEMPLATE_REPO_SCAN_INTERVAL(60000l),
    // org.trimout.basis.fsTemplateRepoMatch
    FS_TEMPLATE_REPO_MATCH(".*"),

    // org.trimout.basis.defaultFileEncoding
    DEFAULT_FILE_ENCODING(SecurityActions.getSystemProperty("file.encoding")),

    // org.trimout.basis.globalJsonDataFile
    GLOBAL_JSON_DATA_FILE(""),

    ;

    BasisConfigurationKey(Object defaultValue) {
        this.key = buildKey(this.toString());
        this.defaultValue = defaultValue;
    }

    private Object defaultValue;

    private String key;

    @Override
    public String get() {
        return key;
    }

    @Override
    public String getEnvKey() {
        return "BASIS_" + name();
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    static String buildKey(String propertyName) {
        StringBuilder key = new StringBuilder();
        key.append(BasisConfigurationKey.class.getPackage().getName());
        key.append(Strings.DOT);
        key.append(Strings.uncapitalize(Strings.replace(
                Strings.capitalizeFully(propertyName,
                        Strings.UNDERSCORE.toCharArray()[0]),
                Strings.UNDERSCORE, "")));
        return key.toString();
    }

}
