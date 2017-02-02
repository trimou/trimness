package org.trimou.basis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.trimou.basis.BasisConfigurationKey.FS_TEMPLATE_REPO_DIR;
import static org.trimou.basis.BasisConfigurationKey.FS_TEMPLATE_REPO_MATCH;
import static org.trimou.basis.BasisConfigurationKey.FS_TEMPLATE_REPO_SCAN_INTERVAL;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class FileSystemTemplateRepositoryTest {

    @Rule
    public WeldInitiator weld = WeldInitiator.of(FileSystemTemplateRepository.class, MockVertxProducer.class, DummyBasisConfiguration.class);

    @Test
    public void testBasicOperations() {
        DummyBasisConfiguration configuration = weld.select(DummyBasisConfiguration.class).get();
        configuration.put(FS_TEMPLATE_REPO_DIR, "src/test/resources/templates");
        configuration.put(FS_TEMPLATE_REPO_MATCH, ".*\\.html");

        FileSystemTemplateRepository repository = weld.select(FileSystemTemplateRepository.class).get();
        assertEquals(1, repository.getAll().size());
        assertNull(repository.get("hello.txt"));
        Template helloTemplate = repository.get("hello.html");
        assertNotNull(helloTemplate);
        assertEquals("hello.html", helloTemplate.getId());
        assertEquals("<html><body>Hello {{name}}!</body></html>", helloTemplate.getContent());
    }

    @Test
    public void testScanInterval() throws IOException, InterruptedException {
        DummyBasisConfiguration configuration = weld.select(DummyBasisConfiguration.class).get();
        configuration.put(FS_TEMPLATE_REPO_DIR, "src/test/resources/templates");
        configuration.put(FS_TEMPLATE_REPO_SCAN_INTERVAL, 100l);

        FileSystemTemplateRepository repository = weld.select(FileSystemTemplateRepository.class).get();

        String id = UUID.randomUUID().toString();
        File tmpFile = new File(new File(configuration.getStringValue(FS_TEMPLATE_REPO_DIR)), "temp_hello.txt");
        Files.write(tmpFile.toPath(), id.getBytes());
        Thread.sleep(200);
        Template helloTemp = repository.get("temp_hello.txt");
        assertNotNull(helloTemp);
        assertEquals(id, helloTemp.getContent());
        tmpFile.delete();
    }

}
