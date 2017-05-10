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
package org.trimou.trimness.model;

import static org.trimou.trimness.config.TrimnessKey.DEFAULT_FILE_ENCODING;
import static org.trimou.trimness.config.TrimnessKey.GLOBAL_JSON_FILE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonStructure;

import org.trimou.trimness.config.Configuration;
import org.trimou.trimness.config.TrimnessKey;
import org.trimou.trimness.util.Resources;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Attempts to read a JSON file from the class path or the filesystem and if it
 * exists and can be read, then provide all the data found to all templates.
 *
 * @author Martin Kouba
 * @see TrimnessKey#GLOBAL_JSON_FILE
 */
@ApplicationScoped
public class GlobalJsonModelProvider implements ModelProvider {

    public static final String NAMESPACE = "global";

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalJsonModelProvider.class);

    @Inject
    private Configuration configuration;

    private JsonStructure model;

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @PostConstruct
    public void init() {

        String filePath = configuration.getStringValue(GLOBAL_JSON_FILE);
        model = null;

        if (filePath.isEmpty()) {
            // Not used
            return;
        }

        ClassLoader classLoader = SecurityActions.getContextClassLoader();
        if (classLoader == null) {
            classLoader = SecurityActions.getClassLoader(GlobalJsonModelProvider.class);
        }

        URL url = Resources.find(filePath, classLoader, "global JSON file");
        File file = null;
        InputStream in = null;

        if (url != null) {
            try {
                in = url.openStream();
            } catch (IOException e) {
                LOGGER.warn("Cannot open global JSON from URL: " + url);
            }
        } else {
            file = new File(filePath);
            if (!file.canRead()) {
                LOGGER.warn("Cannot read global JSON from file: " + file);
            }
            try {
                in = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                LOGGER.warn("Cannot open global JSON file: " + file);
            }
        }

        if (in != null) {
            try (JsonReader reader = Json
                    .createReader(new InputStreamReader(in, configuration.getStringValue(DEFAULT_FILE_ENCODING)))) {
                model = reader.read();
            } catch (Exception e) {
                LOGGER.warn("Error reading global JSON from: " + url != null ? url : file, e);
            }
        }
    }

    @Override
    public void handle(ModelRequest request) {
        request.complete(model);
    }

    @Override
    public boolean isValid() {
        return model != null;
    }

}
