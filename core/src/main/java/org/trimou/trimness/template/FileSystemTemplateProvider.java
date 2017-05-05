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
package org.trimou.trimness.template;

import static org.trimou.trimness.config.TrimnessKey.DEFAULT_FILE_ENCODING;
import static org.trimou.trimness.config.TrimnessKey.TEMPLATE_DIR;
import static org.trimou.trimness.config.TrimnessKey.TEMPLATE_DIR_MATCH;
import static org.trimou.trimness.config.TrimnessKey.TEMPLATE_DIR_SCAN_INTERVAL;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.weld.exceptions.IllegalStateException;
import org.trimou.trimness.config.Configuration;
import org.trimou.trimness.util.AsyncHandlers;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Loads templates from the local filesystem.
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class FileSystemTemplateProvider implements TemplateProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemTemplateProvider.class.getName());

    @Inject
    private Vertx vertx;

    @Inject
    private Configuration configuration;

    @Inject
    private Event<Change> changeEvent;

    @Inject
    private CompositeContentTypeExtractor extractor;

    private Path templateDir;

    private WatchService watcher;

    // Matches the relative part of the path
    // E.g. for file "/home/basis/templates/foo.html" and template dir
    // "/home/basis/templates" matches "foo.html"
    private Pattern matchPattern;

    @PostConstruct
    public void init() {

        String path = configuration.getStringValue(TEMPLATE_DIR);

        File dir = new File(path);
        if (!dir.canRead()) {
            LOGGER.debug("Template dir does not exist or cannot be read: " + dir);
            return;
        }

        templateDir = dir.toPath();
        matchPattern = Pattern.compile(configuration.getStringValue(TEMPLATE_DIR_MATCH));

        // If enabled, scan the template dir periodically to monitor changes
        Long scanInterval = configuration.getLongValue(TEMPLATE_DIR_SCAN_INTERVAL);
        if (scanInterval > 0) {
            initScanTimer(scanInterval);
        } else {
            LOGGER.info("Template directory changes will not be detected");
        }
    }

    @PreDestroy
    void destroy() {
        if (watcher != null) {
            try {
                watcher.close();
            } catch (IOException e) {
                LOGGER.error("Cannot close watcher", e);
            }
        }
    }

    @Override
    public Template get(String id) {
        File templateFile = new File(templateDir.toFile(), id);
        if (templateFile.canRead()) {
            Path templatePath = templateFile.toPath();
            try {
                BasicFileAttributes attributes = Files.readAttributes(templatePath, BasicFileAttributes.class);
                if (attributes.isRegularFile()) {
                    if (matchPattern.matcher(id).matches()) {
                        LOGGER.debug("Found matching: {0}", templatePath);
                        Supplier<Reader> contentReader = () -> {
                            try {
                                return new InputStreamReader(new FileInputStream(templatePath.toFile()),
                                        configuration.getStringValue(DEFAULT_FILE_ENCODING));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        };
                        return ImmutableTemplate.of(id, getId(), contentReader, extractor.extract(id, contentReader));
                    } else {
                        LOGGER.debug("Ignored: {0}", templatePath);
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Unable to read attributes for {0}", e, templatePath);
            }
        }
        return null;
    }

    @Override
    public Set<String> getAvailableTemplateIds() {
        return scan();
    }

    @Override
    public boolean isValid() {
        return templateDir != null;
    }

    /**
     * Scans the template directory.
     */
    private Set<String> scan() {
        LOGGER.debug("Start scanning: {0}", templateDir);
        Set<String> ids = new HashSet<>();
        try {
            Files.walk(templateDir).forEach((path) -> {
                try {
                    BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
                    if (attributes.isRegularFile()) {
                        String id = path.toAbsolutePath().toString()
                                .substring(templateDir.toAbsolutePath().toString().length() + 1);
                        if (matchPattern.matcher(id).matches()) {
                            LOGGER.debug("Found matching: {0}", path);
                            ids.add(id);
                        } else {
                            LOGGER.debug("Ignored: {0}", path);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn("Unable to read attributes for {0}", e, path);
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("Error scanning template directory: " + templateDir, e);
        }
        LOGGER.info("Finished scanning and found {0} templates in {1}", ids.size(), templateDir);
        return ids;
    }

    private void initScanTimer(Long interval) {
        try {
            watcher = templateDir.getFileSystem().newWatchService();
            templateDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            if (watcher != null) {

                LOGGER.debug("Scan interval used: {0} seconds", TimeUnit.MILLISECONDS.toSeconds(interval));
                vertx.setPeriodic(interval, (id) -> {

                    vertx.executeBlocking(future -> {

                        WatchKey key = watcher.poll();
                        if (key != null) {

                            List<WatchEvent<?>> events = key.pollEvents();

                            if (!events.isEmpty()) {
                                LOGGER.debug("{0} template directory events", events.size());
                                for (WatchEvent<?> event : events) {
                                    if (StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())
                                            || StandardWatchEventKinds.ENTRY_MODIFY.equals(event.kind())
                                            || StandardWatchEventKinds.ENTRY_DELETE.equals(event.kind())) {
                                        Path path = (Path) event.context();
                                        if (path != null && matchPattern.matcher(path.toString()).matches()) {
                                            changeEvent.fire(new ImmutableChange(getId(), path.toString()));
                                        }
                                    }
                                }
                            }
                        }
                        future.complete();

                    }, AsyncHandlers.NOOP_HANDLER);
                });
            }
        } catch (IOException e) {
            LOGGER.warn("Unable to watch the template directory - changes will not be detected", e);
        }
    }

}
