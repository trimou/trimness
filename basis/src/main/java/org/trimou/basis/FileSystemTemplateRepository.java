package org.trimou.basis;

import static org.trimou.basis.BasisConfigurationKey.DEFAULT_FILE_ENCODING;
import static org.trimou.basis.BasisConfigurationKey.FS_TEMPLATE_REPO_DIR;
import static org.trimou.basis.BasisConfigurationKey.FS_TEMPLATE_REPO_MATCH;
import static org.trimou.basis.BasisConfigurationKey.FS_TEMPLATE_REPO_SCAN_INTERVAL;

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
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.weld.exceptions.IllegalStateException;
import org.trimou.basis.Template.ContentLoader;
import org.trimou.util.IOUtils;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * TODO add basic content type support based on file suffix
 *
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class FileSystemTemplateRepository implements TemplateRepository {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(FileSystemTemplateRepository.class.getName());

    @Inject
    private Vertx vertx;

    @Inject
    private BasisConfiguration configuration;

    private Path templateDir;

    private WatchService watcher;

    private Map<String, Template> templates;

    // Matches the relative part of the path
    // E.g. for file "/home/basis/templates/foo.html" and template dir
    // "/home/basis/templates" matches "foo.html"
    private Pattern matchPattern;

    @PostConstruct
    public void init() {
        templates = new HashMap<>();

        String path = configuration.getStringValue(FS_TEMPLATE_REPO_DIR);
        matchPattern = Pattern
                .compile(configuration.getStringValue(FS_TEMPLATE_REPO_MATCH));

        File dir = new File(path);
        templateDir = dir.toPath();
        if (!dir.canRead()) {
            LOGGER.warn(
                    "Template dir does not exist or cannot be read: " + dir);
            return;
        }

        // Scan the template dir
        scan();

        // If enabled, scan the template dir periodically
        Long scanInterval = configuration
                .getLongValue(FS_TEMPLATE_REPO_SCAN_INTERVAL);
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

    /**
     * Scans the template directory.
     */
    void scan() {
        LOGGER.info("Start scanning: {0}", templateDir);
        synchronized (templates) {
            templates.clear();
            try {
                Files.walk(templateDir).forEach(this::processPath);
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Error scanning template directory: " + templateDir, e);
            }
            LOGGER.info("Finished scanning and found {0} templates in {1}",
                    templates.size(), templateDir);
        }
    }

    private void processPath(Path path) {
        try {
            BasicFileAttributes attributes = Files.readAttributes(path,
                    BasicFileAttributes.class);
            if (attributes.isRegularFile()) {
                String id = path.toAbsolutePath().toString().substring(
                        templateDir.toAbsolutePath().toString().length() + 1);
                if (matchPattern.matcher(id).matches()) {
                    LOGGER.debug("Found matching: {}", path);
                    templates.put(id,
                            Template.of(id, getContentLoader(path), null));
                } else {
                    LOGGER.debug("Ignored: {}", path);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to read {0} attributes", e, path);
        }
    }

    private ContentLoader getContentLoader(Path path) {
        return () -> {
            try {
                try (FileInputStream in = new FileInputStream(path.toFile())) {
                    return IOUtils.toString(new InputStreamReader(in,
                            configuration.getStringValue(
                                    DEFAULT_FILE_ENCODING)));
                }
            } catch (Exception e) {
                LOGGER.warn("Unable to read {0} content", e, path);
                return null;
            }
        };
    }

    private void setScanTimer(Long interval) {
        try {
            watcher = templateDir.getFileSystem().newWatchService();
            templateDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
            if (watcher != null) {
                LOGGER.debug("Scan interval used: {0} seconds",
                        TimeUnit.MILLISECONDS.toSeconds(interval));
                vertx.setPeriodic(interval, (id) -> {
                    WatchKey key = watcher.poll();
                    if (key != null) {
                        List<WatchEvent<?>> events = key.pollEvents();
                        if (!events.isEmpty()) {
                            scan();
                        }
                    }

                });
            }
        } catch (IOException e) {
            LOGGER.warn(
                    "Unable to watch template directory - changes will not be detected",
                    e);
        }
    }

}
