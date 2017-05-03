package org.trimou.trimness.render;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;
import org.trimou.trimness.MockVertxProducer;
import org.trimou.trimness.render.Result.Code;
import org.trimou.trimness.template.ImmutableTemplate;

/**
 *
 * @author Martin Kouba
 */
public class InMemoryResultRepositoryTest {

    @Rule
    public WeldInitiator weld = WeldInitiator.of(InMemoryResultRepository.class, MockVertxProducer.class);

    @Test
    public void testBasicOperations() {
        InMemoryResultRepository repository = weld.select(InMemoryResultRepository.class).get();
        assertEquals(0, repository.size());
        Result result1 = repository.init(ImmutableTemplate.of("foo"), 10000);
        assertEquals(1, repository.size());
        assertNotNull(result1);
        assertNotNull(result1.getId());
        assertEquals(Code.INCOMPLETE, result1.getCode());
        assertNull(result1.getContentType());
        assertEquals(result1.getId(), repository.get(result1.getId()).getId());
        result1.complete("hello");
        assertEquals(1, repository.size());
        Result result2 = repository.get(result1.getId());
        assertNotNull(result2);
        assertEquals(result1.getId(), result2.getId());
        assertEquals("hello", result2.getOutput());
        try {
            result1.fail("foo");
            fail();
        } catch (Exception expected) {
        }
        assertTrue(repository.remove(result1.getId()));
        assertNull(repository.get(result1.getId()));
        assertEquals(0, repository.size());
    }

}
