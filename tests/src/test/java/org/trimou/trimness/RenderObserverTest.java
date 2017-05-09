package org.trimou.trimness;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.trimou.trimness.config.TrimnessKey.GLOBAL_JSON_FILE;
import static org.trimou.trimness.config.TrimnessKey.TEMPLATE_DIR;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.enterprise.inject.spi.CDI;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.trimou.trimness.render.RenderObserver;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class RenderObserverTest {

    static final long DEFAULT_TIMEOUT = 5000;

    @Deployment
    public static Archive<?> createTestArchive() {
        return TrimnessTest.createDefaultClassPath()
                .addSystemProperty(TEMPLATE_DIR.get(),
                        "src/test/resources/templates")
                .addSystemProperty(GLOBAL_JSON_FILE.get(),
                        "src/test/resources/global-data.json")
                .add(ShrinkWrap.create(JavaArchive.class)
                        .addClasses(RenderObserverTest.class))
                .build();
    }

    @Test
    public void testOneoffHello() throws InterruptedException {
        Vertx vertx = CDI.current().select(Vertx.class).get();
        BlockingQueue<Object> synchronizer = new LinkedBlockingQueue<>();
        vertx.eventBus().send(RenderObserver.ADDR_RENDER,
                "{ \"content\" : \"Hello {{model.name}}!\", \"model\" : { \"name\" : \"Lu\"}}",
                (result) -> {
                    if (result.succeeded()) {
                        synchronizer.add(result.result().body());
                    } else {
                        synchronizer.add(result.cause());
                    }
                });
        Object reply = synchronizer.poll(DEFAULT_TIMEOUT,
                TimeUnit.MILLISECONDS);
        assertNotNull(reply);
        assertEquals("Hello Lu!", reply.toString());
    }

    @Test
    public void testHello() throws InterruptedException {
        Vertx vertx = CDI.current().select(Vertx.class).get();
        BlockingQueue<Object> synchronizer = new LinkedBlockingQueue<>();
        vertx.eventBus().send(RenderObserver.ADDR_RENDER,
                "{\"id\" : \"hello.txt\", \"model\" : [ \"Lu\" ] }}",
                (result) -> {
                    if (result.succeeded()) {
                        synchronizer.add(result.result().body());
                    } else {
                        synchronizer.add(result.cause());
                    }
                });
        Object reply = synchronizer.poll(DEFAULT_TIMEOUT,
                TimeUnit.MILLISECONDS);
        assertNotNull(reply);
        assertEquals("Hello Lu!", reply.toString());
    }

    @Test
    public void testInvalidInput() throws InterruptedException {
        Vertx vertx = CDI.current().select(Vertx.class).get();
        BlockingQueue<Object> synchronizer = new LinkedBlockingQueue<>();
        vertx.eventBus().send(RenderObserver.ADDR_RENDER,
                "{ \"content\" : \"Hello\"", (result) -> {
                    if (result.failed()) {
                        synchronizer.add(result.cause());
                    } else {
                        synchronizer.add(result.result().body());
                    }
                });
        Object reply = synchronizer.poll(DEFAULT_TIMEOUT,
                TimeUnit.MILLISECONDS);
        assertNotNull(reply);
        assertTrue(reply instanceof ReplyException);
        ReplyException exception = (ReplyException) reply;
        assertEquals(RenderObserver.CODE_INVALID_JSON, exception.failureCode());
        assertEquals(ReplyFailure.RECIPIENT_FAILURE, exception.failureType());
    }

}
