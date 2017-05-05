package org.trimou.trimness.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;
import org.trimou.trimness.DummyConfiguration;
import org.trimou.trimness.config.TrimnessKey;
import org.trimou.trimness.template.ImmutableTemplate;

/**
 *
 * @author Martin Kouba
 */
public class ModelInitializerTest {

    private static final long TIMEOUT = 100l;

    @Rule
    public WeldInitiator weld = WeldInitiator.of(ModelInitializer.class, DummyConfiguration.class, SimpleProvider.class,
            UnfinishedProvider.class, FailingProvider.class, LatecomerProvider.class);

    @Test
    public void testInit() {
        DummyConfiguration configuration = weld.select(DummyConfiguration.class).get();
        configuration.put(TrimnessKey.MODEL_INIT_TIMEOUT, TIMEOUT);

        ModelInitializer initializer = weld.select(ModelInitializer.class).get();
        Map<String, Object> model = initializer.initModel(ImmutableTemplate.of("test"), null, Collections.emptyMap());
        assertEquals(2, model.size());
        assertEquals(Collections.emptyMap(), model.get("model"));
        assertEquals("1", model.get("simple"));
    }

    static class SimpleProvider implements ModelProvider {

        @Override
        public String getNamespace() {
            return "simple";
        }

        @Override
        public void handle(ModelRequest request) {
            request.complete("1");
            try {
                request.complete("2");
                fail();
            } catch (IllegalStateException expected) {
            }
        }

    }

    static class UnfinishedProvider implements ModelProvider {

        @Override
        public String getNamespace() {
            return "dummy";
        }

        @Override
        public void handle(ModelRequest request) {
            assertEquals("test", request.getTemplate().getId());
            assertNull(request.getTemplate().getContentType());
            // Intentionally do not call setResult()
        }

    }

    static class FailingProvider implements ModelProvider {

        @Override
        public String getNamespace() {
            return "dummy";
        }

        @Override
        public void handle(ModelRequest request) {
            assertFalse(request.getParameter("foo").isPresent());
            throw new IllegalStateException();
        }

    }

    static class LatecomerProvider implements ModelProvider {

        @Override
        public String getNamespace() {
            return "late";
        }

        @Override
        public void handle(ModelRequest request) {
            ForkJoinPool.commonPool().execute(() -> {
                try {
                    Thread.sleep(TIMEOUT * 2);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                request.complete("baz!");
            });
        }

    }

}
