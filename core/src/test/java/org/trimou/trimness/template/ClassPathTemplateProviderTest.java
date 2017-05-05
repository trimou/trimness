package org.trimou.trimness.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;
import org.trimou.trimness.DummyConfiguration;
import org.trimou.trimness.template.ClassPathTemplateProvider;
import org.trimou.trimness.template.CompositeContentTypeExtractor;
import org.trimou.trimness.template.Template;

/**
 *
 * @author Martin Kouba
 */
public class ClassPathTemplateProviderTest {

    @Rule
    public WeldInitiator weld = WeldInitiator.of(ClassPathTemplateProvider.class, DummyConfiguration.class,
            CompositeContentTypeExtractor.class);

    @Test
    public void testBasicOperations() {
        ClassPathTemplateProvider provider = weld.select(ClassPathTemplateProvider.class).get();
        Template helloTemplate = provider.get("hello.html");
        assertEquals(0, provider.getAvailableTemplateIds().size());
        assertNull(provider.get("hello.txt"));
        assertNotNull(helloTemplate);
        assertEquals("hello.html", helloTemplate.getId());
        assertEquals("<html><body>Hello {{name}}!</body></html>", helloTemplate.getContent());
    }

}
