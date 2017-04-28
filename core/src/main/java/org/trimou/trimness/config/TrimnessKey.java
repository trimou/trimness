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

import org.trimou.trimness.TrimnessVerticle;
import org.trimou.trimness.model.GlobalJsonModelProvider;
import org.trimou.trimness.render.RenderHandler;
import org.trimou.trimness.template.ClassPathTemplateRepository;
import org.trimou.trimness.template.FileSystemTemplateRepository;

/**
 * Core configuration keys.
 *
 * TODO links
 *
 * @author Martin Kouba
 */
public enum TrimnessKey implements Key {

    /**
     * <tt>org.trimou.trimness.host</tt>
     *
     * @see TrimnessVerticle
     */
    HOST("localhost"),

    /**
     * <tt>org.trimou.trimness.port</tt>
     *
     * @see TrimnessVerticle
     */
    PORT(8080),

    /**
     * <tt>org.trimou.trimness.defaultResultTimeout</tt>
     *
     * @see RenderHandler
     */
    DEFAULT_RESULT_TIMEOUT(300000l),

    /**
     * <tt>org.trimou.trimness.templateDir</tt>
     *
     * @see FileSystemTemplateRepository
     */
    TEMPLATE_DIR(System.getProperty("user.dir") + SecurityActions.getSystemProperty("file.separator") + "templates"),

    /**
     * <tt>org.trimou.trimness.templateDirScanInterval</tt>
     *
     * @see FileSystemTemplateRepository
     */
    TEMPLATE_DIR_SCAN_INTERVAL(60000l),

    /**
     * <tt> org.trimou.trimness.templateDirMatch</tt>
     *
     * @see FileSystemTemplateRepository
     */
    TEMPLATE_DIR_MATCH(".*"),

    /**
     * <tt>org.trimou.trimness.classpathRoot</tt>
     *
     * @see ClassPathTemplateRepository
     */
    CLASSPATH_ROOT("META-INF/templates/"),

    /**
     * <tt>org.trimou.trimness.defaultFileEncoding</tt>
     */
    DEFAULT_FILE_ENCODING(SecurityActions.getSystemProperty("file.encoding")),

    /**
     * <tt>org.trimou.trimness.globalJsonFile</tt>
     *
     * @see GlobalJsonModelProvider
     */
    GLOBAL_JSON_FILE(""),

    ;

    TrimnessKey(Object defaultValue) {
        this.key = Key.envToKey(name());
        this.defaultValue = defaultValue;
    }

    private Object defaultValue;

    private String key;

    @Override
    public String get() {
        return key;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

}