package org.trimou.trimness.render;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.weld.junit4.MockBean;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;
import org.trimou.trimness.MockVertxProducer;
import org.trimou.trimness.render.Result.Status;
import org.trimou.trimness.template.ImmutableTemplate;
import org.trimou.trimness.util.Jsons;

/**
 *
 * @author Martin Kouba
 */
public class InMemoryResultRepositoryTest {

    @Rule
    public WeldInitiator weld = WeldInitiator
            .from(InMemoryResultRepository.class, IdGenerator.class, ResultLinkDefinitions.class,
                    MockVertxProducer.class)
            .addBeans(MockBean.of(new SimpleResultLinkDefinition("test", (r) -> r.getTemplate().getId().equals("bang")),
                    ResultLinkDefinition.class))
            .build();

    @Test
    public void testBasicOperations() {
        InMemoryResultRepository repository = weld.select(InMemoryResultRepository.class).get();
        repository.clear();
        assertEquals(0, repository.size());
        Result result = repository.init(new SimpleRenderRequest(System.currentTimeMillis(), ImmutableTemplate.of("foo"),
                10000l, Jsons.EMPTY_OBJECT));
        assertEquals(1, repository.size());
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(Status.INCOMPLETE, result.getStatus());
        assertNull(result.getContentType());
        assertEquals(result.getId(), repository.get(result.getId()).getId());
        result.complete("hello");
        assertEquals(1, repository.size());
        Result result2 = repository.get(result.getId());
        assertNotNull(result2);
        assertEquals(result.getId(), result2.getId());
        assertEquals("hello", result2.getOutput());
        try {
            result.fail("foo");
            fail();
        } catch (Exception expected) {
        }
        assertTrue(repository.remove(result.getId()));
        assertNull(repository.get(result.getId()));
        assertEquals(0, repository.size());
    }

    @Test
    public void testTimeout() throws InterruptedException {
        InMemoryResultRepository repository = weld.select(InMemoryResultRepository.class).get();
        repository.clear();
        Result result = repository.init(new SimpleRenderRequest(System.currentTimeMillis(), ImmutableTemplate.of("foo"),
                100l, Jsons.EMPTY_OBJECT));
        Thread.sleep(200);
        assertNull(repository.get(result.getId()));
    }

    @Test
    public void testLink() throws InterruptedException {
        InMemoryResultRepository repository = weld.select(InMemoryResultRepository.class).get();
        repository.clear();
        assertNull(repository.getLink("test"));
        Result result = repository.init(new SimpleRenderRequest(System.currentTimeMillis(),
                ImmutableTemplate.of("bang"), 0l, Jsons.EMPTY_OBJECT));
        result.complete("hello");
        ResultLink link = repository.getLink("test");
        assertNotNull(link);
        assertEquals("hello", repository.get(link.getResultId()).getOutput());
    }

}
