package org.trimou.trimness.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.trimou.trimness.config.TrimnessKey.TEMPLATE_DIR;
import static org.trimou.trimness.config.TrimnessKey.TEMPLATE_DIR_MATCH;
import static org.trimou.trimness.config.TrimnessKey.TEMPLATE_DIR_SCAN_INTERVAL;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;
import org.trimou.trimness.DummyConfiguration;
import org.trimou.trimness.MockVertxProducer;
import org.trimou.trimness.template.CompositeContentTypeExtractor;
import org.trimou.trimness.template.FileSystemTemplateProvider;
import org.trimou.trimness.template.Template;

/**
 *
 * @author Martin Kouba
 */
public class FileSystemTemplateProviderTest {

    @Rule
    public WeldInitiator weld = WeldInitiator.of(FileSystemTemplateProvider.class, MockVertxProducer.class, DummyConfiguration.class,
            CompositeContentTypeExtractor.class);

    @Test
    public void testBasicOperations() {
        DummyConfiguration configuration = weld.select(DummyConfiguration.class).get();
        configuration.put(TEMPLATE_DIR, "src/test/resources/templates");
        configuration.put(TEMPLATE_DIR_MATCH, ".*\\.html");

        FileSystemTemplateProvider provider = weld.select(FileSystemTemplateProvider.class).get();
        assertEquals(1, provider.getAvailableTemplateIds().size());
        assertNull(provider.get("hello.txt"));
        Template helloTemplate = provider.get("hello.html");
        assertNotNull(helloTemplate);
        assertEquals("hello.html", helloTemplate.getId());
        assertEquals("<html><body>Hello {{name}}!</body></html>", helloTemplate.getContent());
    }

    @Test
    public void testScanInterval() throws IOException, InterruptedException {
        DummyConfiguration configuration = weld.select(DummyConfiguration.class).get();
        configuration.put(TEMPLATE_DIR, "src/test/resources/templates");
        configuration.put(TEMPLATE_DIR_SCAN_INTERVAL, 100l);

        FileSystemTemplateProvider provider = weld.select(FileSystemTemplateProvider.class).get();

        String id = UUID.randomUUID().toString();
        File tmpFile = new File(new File(configuration.getStringValue(TEMPLATE_DIR)), "temp_hello.txt");
        Files.write(tmpFile.toPath(), id.getBytes());
        Thread.sleep(200);
        Template helloTemp = provider.get("temp_hello.txt");
        assertNotNull(helloTemp);
        assertEquals(id, helloTemp.getContent());

        // Test modification
        id = UUID.randomUUID().toString();
        Files.write(tmpFile.toPath(), id.getBytes());
        Thread.sleep(200);
        helloTemp = provider.get("temp_hello.txt");
        assertNotNull(helloTemp);
        assertEquals(id, helloTemp.getContent());

        tmpFile.delete();
    }

}
