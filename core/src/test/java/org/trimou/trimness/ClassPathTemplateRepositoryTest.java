package org.trimou.trimness;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;
import org.trimou.trimness.template.ClassPathTemplateRepository;
import org.trimou.trimness.template.CompositeContentTypeExtractor;
import org.trimou.trimness.template.Template;

/**
 *
 * @author Martin Kouba
 */
public class ClassPathTemplateRepositoryTest {

    @Rule
    public WeldInitiator weld = WeldInitiator.of(ClassPathTemplateRepository.class, DummyConfiguration.class,
            CompositeContentTypeExtractor.class);

    @Test
    public void testBasicOperations() {
        ClassPathTemplateRepository repository = weld.select(ClassPathTemplateRepository.class).get();
        Template helloTemplate = repository.get("hello.html");
        assertEquals(1, repository.getAll().size());
        assertNull(repository.get("hello.txt"));
        assertNotNull(helloTemplate);
        assertEquals("hello.html", helloTemplate.getId());
        assertEquals("<html><body>Hello {{name}}!</body></html>", helloTemplate.getContent());
    }

}
