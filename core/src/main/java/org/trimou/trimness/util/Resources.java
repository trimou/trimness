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
package org.trimou.trimness.util;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 *
 * @author Martin Kouba
 *
 */
public final class Resources {

    private static final Logger LOGGER = LoggerFactory.getLogger(Resources.class.getName());

    private Resources() {
    }

    /**
     * Duplicit resources are ignored.
     *
     * @param name
     * @param classLoader
     * @param type
     * @return the resource URL or <code>null</code>
     */
    public static URL find(String name, ClassLoader classLoader, String type) {
        URL found = null;
        try {
            Enumeration<URL> resources = classLoader.getResources(name);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (found != null) {
                    LOGGER.info("Duplicit {0} ignored: {1}", type, resource);
                } else {
                    found = resource;
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error while reading {}", e, name);
        }
        return found;
    }

}
