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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.weld.exceptions.IllegalStateException;
import org.trimou.trimness.config.Configuration;
import org.trimou.util.IOUtils;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Loads templates from the local filesystem.
 *
 * TODO reload only changed template, not the whole directory
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class FileSystemTemplateRepository implements TemplateRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemTemplateRepository.class.getName());

    @Inject
    private Vertx vertx;

    @Inject
    private Configuration configuration;

    @Inject
    private Event<Template> event;

    @Inject
    private CompositeContentTypeExtractor extractor;

    private Path templateDir;

    private WatchService watcher;

    private Map<String, Template> templates;

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
        templates = new HashMap<>();
        matchPattern = Pattern.compile(configuration.getStringValue(TEMPLATE_DIR_MATCH));

        // Scan the template dir
        scan();

        // If enabled, scan the template dir periodically
        Long scanInterval = configuration.getLongValue(TEMPLATE_DIR_SCAN_INTERVAL);
        if (scanInterval > 0) {
            setScanTimer(scanInterval);
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
        return templates.get(id);
    }

    @Override
    public Set<Template> getAll() {
        return new HashSet<>(templates.values());
    }

    @Override
    public boolean isValid() {
        return templateDir != null;
    }

    /**
     * Scans the template directory.
     */
    private void scan() {
        LOGGER.debug("Start scanning: {0}", templateDir);
        synchronized (templates) {
            templates.clear();
            try {
                Files.walk(templateDir).forEach(this::processPath);
            } catch (IOException e) {
                throw new IllegalStateException("Error scanning template directory: " + templateDir, e);
            }
            LOGGER.info("Finished scanning and found {0} templates in {1}", templates.size(), templateDir);
        }
    }

    private void processPath(Path path) {
        try {
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            if (attributes.isRegularFile()) {
                String id = path.toAbsolutePath().toString()
                        .substring(templateDir.toAbsolutePath().toString().length() + 1);
                if (matchPattern.matcher(id).matches()) {
                    LOGGER.debug("Found matching: {0}", path);
                    Supplier<String> contentLoader = () -> loadFile(path);
                    ImmutableTemplate template = ImmutableTemplate.of(id, contentLoader,
                            extractor.extract(id, contentLoader));
                    templates.put(id, template);
                    event.fire(template);
                } else {
                    LOGGER.debug("Ignored: {0}", path);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to read {0} attributes", e, path);
        }
    }

    private String loadFile(Path path) {
        try {
            try (FileInputStream in = new FileInputStream(path.toFile())) {
                return IOUtils.toString(new InputStreamReader(in, configuration.getStringValue(DEFAULT_FILE_ENCODING)));
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to read {0} content", e, path);
            return null;
        }
    }

    private void setScanTimer(Long interval) {
        try {
            watcher = templateDir.getFileSystem().newWatchService();
            templateDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
            if (watcher != null) {
                LOGGER.debug("Scan interval used: {0} seconds", TimeUnit.MILLISECONDS.toSeconds(interval));
                vertx.setPeriodic(interval, (id) -> {
                    WatchKey key = watcher.poll();
                    if (key != null) {
                        List<WatchEvent<?>> events = key.pollEvents();
                        if (!events.isEmpty()) {
                            LOGGER.info("Template modifications detected: {0}",
                                    events.stream().map((event) -> event.context()).collect(Collectors.toList()));
                            scan();
                        }
                    }

                });
            }
        } catch (IOException e) {
            LOGGER.warn("Unable to watch template directory - changes will not be detected", e);
        }
    }

}
