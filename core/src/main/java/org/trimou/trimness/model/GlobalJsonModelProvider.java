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
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.trimou.trimness.config.Configuration;
import org.trimou.trimness.config.TrimnessKey;
import org.trimou.util.ImmutableMap;
import org.trimou.util.ImmutableMap.ImmutableMapBuilder;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Read a JSON data file (if exists) and provides all the data found to all
 * templates.
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

    private Map<String, Object> model;

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @PostConstruct
    public void init() {

        String filePath = configuration.getStringValue(GLOBAL_JSON_FILE);
        model = Collections.emptyMap();
        if (filePath.isEmpty()) {
            return;
        }

        File file = new File(filePath);
        if (!file.canRead()) {
            LOGGER.debug("Global JSON data file does not exist or cannot be read: " + file);
            return;
        }

        try {

            JsonReader reader = Json.createReader(new InputStreamReader(new FileInputStream(file),
                    configuration.getStringValue(DEFAULT_FILE_ENCODING)));
            JsonStructure global = reader.read();

            if (ValueType.OBJECT.equals(global.getValueType())) {

                ImmutableMapBuilder<String, Object> builder = ImmutableMap.<String, Object>builder();
                JsonObject globalDataObject = (JsonObject) global;

                for (Entry<String, JsonValue> entry : globalDataObject.entrySet()) {
                    builder.put(entry.getKey(), entry.getValue());
                }
                model = builder.build();
            }

        } catch (Exception e) {
            LOGGER.warn("Error reading global JSON data file: " + file, e);
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
