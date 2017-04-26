/*
 * Copyright 2017 Trimness team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trimou.trimness.config;

import org.trimou.util.Strings;

/**
 *
 * @author Martin Kouba
 */
public enum TrimnessConfigurationKey implements ConfigurationKey {

    // org.trimou.trimness.host
    HOST("localhost"),
    // org.trimou.trimness.port
    PORT(8080),
    // org.trimou.trimness.defaultResultTimeout
    DEFAULT_RESULT_TIMEOUT(300000l),

    // org.trimou.trimness.fsTemplateRepoDir
    FS_TEMPLATE_REPO_DIR(
            System.getProperty("user.dir") + SecurityActions.getSystemProperty("file.separator") + "templates"),
    // org.trimou.trimness.fsTemplateRepoScanInterval
    FS_TEMPLATE_REPO_SCAN_INTERVAL(60000l),
    // org.trimou.trimness.fsTemplateRepoMatch
    FS_TEMPLATE_REPO_MATCH(".*"),

    // org.trimou.trimness.defaultFileEncoding
    DEFAULT_FILE_ENCODING(SecurityActions.getSystemProperty("file.encoding")),

    // org.trimou.trimness.globalJsonDataFile
    GLOBAL_JSON_DATA_FILE(""),

    ;

    TrimnessConfigurationKey(Object defaultValue) {
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
        key.append("org.trimou.trimness");
        key.append(Strings.DOT);
        key.append(Strings.uncapitalize(Strings.replace(
                Strings.capitalizeFully(propertyName, Strings.UNDERSCORE.toCharArray()[0]), Strings.UNDERSCORE, "")));
        return key.toString();
    }

}
