package org.trimou.basis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Test;
import org.trimou.basis.Result.Code;

/**
 *
 * @author Martin Kouba
 */
public class InMemoryResultRepositoryTest {

    @Test
    public void testBasicOperations() {
        try (WeldContainer container = new Weld().disableDiscovery()
                .beanClasses(InMemoryResultRepository.class).initialize()) {
            InMemoryResultRepository repository = container
                    .select(InMemoryResultRepository.class).get();
            assertEquals(0, repository.size());
            Result next = repository.next("foo", null);
            assertEquals(1, repository.size());
            assertNotNull(next);
            assertEquals(Code.INCOMPLETE, next.getCode());
            assertEquals(next.getId(), repository.get(next.getId()).getId());
            repository.complete(next.getId(), Code.SUCESS, null, "hello");
            assertEquals(1, repository.size());
            Result result = repository.get(next.getId());
            assertNotNull(result);
            assertEquals(next.getId(), result.getId());
            assertEquals("hello", result.getOutput());
            assertTrue(repository.remove(next.getId()));
            assertNull(repository.get(next.getId()));
            assertEquals(0, repository.size());
        }
    }

}
