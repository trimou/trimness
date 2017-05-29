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
package org.trimou.trimness.render;

import static org.trimou.trimness.config.TrimnessKey.DEFAULT_FILE_ENCODING;
import static org.trimou.trimness.config.TrimnessKey.RESULT_DIR;
import static org.trimou.trimness.util.Strings.COMPLETED;
import static org.trimou.trimness.util.Strings.CONTENT_TYPE;
import static org.trimou.trimness.util.Strings.CREATED;
import static org.trimou.trimness.util.Strings.ID;
import static org.trimou.trimness.util.Strings.LINK_ID;
import static org.trimou.trimness.util.Strings.RESULT_ID;
import static org.trimou.trimness.util.Strings.STATUS;
import static org.trimou.trimness.util.Strings.TEMPLATE_ID;
import static org.trimou.trimness.util.Strings.VALUE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;

import org.trimou.trimness.config.Configuration;
import org.trimou.trimness.render.Result.Status;
import org.trimou.trimness.util.AsyncHandlers;
import org.trimou.trimness.util.Jsons;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Makes use of the local filesystem.
 *
 * @author Martin Kouba
 */
@Dependent
public class FileSystemResultRepository implements ResultRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemResultRepository.class);

    @Inject
    private Vertx vertx;

    @Inject
    private IdGenerator idGenerator;

    @Inject
    private Configuration configuration;

    private File resultDir;

    @PostConstruct
    void init() {
        String path = configuration.getStringValue(RESULT_DIR);
        if (path != null && !path.isEmpty()) {
            File dir = new File(path);
            if (!dir.canRead()) {
                LOGGER.debug("Result dir does not exist or cannot be read: " + dir);
                return;
            }
            resultDir = dir;
        }
    }

    @Override
    public boolean isValid() {
        return resultDir != null;
    }

    @Override
    public Result get(String resultId) {
        return readResult(new File(resultDir, resultId + ".json"));
    }

    @Override
    public ResultLink getLink(String linkId) {
        return readLink(linkId);
    }

    @Override
    public Result init(RenderRequest renderRequest) {
        Consumer<Result> onComplete = (result) -> {
            writeResult(result);
            if (result.isSucess() && renderRequest.getLinkId() != null) {
                writeLink(result.getId(), renderRequest.getLinkId());
            }
        };
        SimpleResult result = SimpleResult.init("" + idGenerator.nextId(), renderRequest.getTemplate().getId(),
                renderRequest.getTemplate().getContentType(), onComplete);

        writeResult(result);

        if (renderRequest.getTimeout() != null && renderRequest.getTimeout() > 0) {
            // Schedule result removal
            vertx.setTimer(renderRequest.getTimeout(), (id) -> {
                vertx.executeBlocking((future) -> {
                    if (deleteResult(result.getId())) {
                        LOGGER.info("Result timed out [id: {0}]", result.getId());
                    }
                    future.complete();
                }, AsyncHandlers.NOOP_HANDLER);
            });
        }
        LOGGER.info("Result initialized [id: {0}, template: {1}, timeout: {2}]", result.getId(),
                renderRequest.getTemplate().getId(), renderRequest.getTimeout());
        return result;
    }

    @Override
    public boolean remove(String resultId) {
        if (deleteResult(resultId)) {
            LOGGER.info("Result removed [id: {0}]", resultId);
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        try {
            return Files.walk(resultDir.toPath(), 1).filter((path) -> {
                try {
                    BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
                    return attributes.isRegularFile() && path.toString().endsWith(".json");
                } catch (Exception e) {
                    LOGGER.warn("Unable to read attributes for {0}", e, path);
                    return false;
                }
            }).mapToInt((e) -> 1).sum();
        } catch (IOException e) {
            LOGGER.warn("Unable to count result JSON files in {0}", e, resultDir);
            return 0;
        }
    }

    @Override
    public void clear() {
        try {
            Files.walk(resultDir.toPath(), 1).forEach((path) -> {
                try {
                    BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
                    if (attributes.isRegularFile() && path.toString().endsWith(".json")) {
                        Files.deleteIfExists(path);
                    }
                } catch (IOException e) {
                    LOGGER.warn("Unable to delete {0}", e, path);
                }
            });
            LOGGER.info("All results removed from {0}", resultDir);
        } catch (IOException e) {
            LOGGER.warn("Unable to clear result directory {0}", e, resultDir);
        }
    }

    private boolean deleteResult(String resultId) {
        File resultFile = new File(resultDir, resultId + ".json");
        try {
            return Files.deleteIfExists(resultFile.toPath());
        } catch (IOException e) {
            LOGGER.debug("Error deleting result file: " + resultFile, e);
            return false;
        }
    }

    private Result readResult(File resultFile) {
        if (resultFile.exists()) {
            JsonObject resulJson = readJson(resultFile);
            return new SimpleResult(resulJson.getString(ID), Jsons.getLong(resulJson, CREATED, null),
                    Jsons.getLong(resulJson, COMPLETED, null), resulJson.getString(TEMPLATE_ID),
                    resulJson.getString(CONTENT_TYPE, null), Status.valueOf(resulJson.getString(STATUS)),
                    resulJson.getString(VALUE, null), null);
        }
        return null;
    }

    private ResultLink readLink(String linkId) {
        File linkFile = new File(resultDir, linkId + ".json");
        if (linkFile.exists()) {
            JsonObject linkJson = readJson(linkFile);
            return new SimpleResultLink(linkId, linkJson.getString(RESULT_ID));
        }
        return null;
    }

    private JsonObject readJson(File file) {
        try (JsonReader reader = Jsons.INSTANCE.createReader(new InputStreamReader(new FileInputStream(file),
                configuration.getStringValue(DEFAULT_FILE_ENCODING)))) {
            return (JsonObject) reader.read();
        } catch (Exception e) {
            throw new IllegalStateException("Error reading JSON from: " + file, e);
        }
    }

    private void writeResult(Result result) {
        File resultFile = new File(resultDir, result.getId() + ".json");
        JsonObjectBuilder resultJson = Jsons.objectBuilder();
        resultJson.add(ID, result.getId());
        resultJson.add(STATUS, result.getStatus().toString());
        resultJson.add(TEMPLATE_ID, result.getTemplateId());
        if (result.getContentType() != null) {
            resultJson.add(CONTENT_TYPE, result.getContentType());
        }
        resultJson.add(CREATED, result.getCreated());
        if (result.getCompleted() != null) {
            resultJson.add(COMPLETED, result.getCompleted());
        }
        if (result.getValue() != null) {
            resultJson.add(VALUE, result.getValue());
        }
        writeJson(resultFile, resultJson.build());
    }

    private void writeLink(String resultId, String linkId) {
        File linkFile = new File(resultDir, linkId + ".json");
        writeJson(linkFile, Jsons.objectBuilder().add(RESULT_ID, resultId).add(LINK_ID, linkId).build());
    }

    private void writeJson(File file, JsonObject jsonObject) {
        try (JsonWriter writer = Jsons.INSTANCE.createWriter(new OutputStreamWriter(new FileOutputStream(file),
                configuration.getStringValue(DEFAULT_FILE_ENCODING)))) {
            writer.writeObject(jsonObject);
        } catch (Exception e) {
            throw new IllegalStateException("Error writing JSON to: " + file, e);
        }
    }

}
