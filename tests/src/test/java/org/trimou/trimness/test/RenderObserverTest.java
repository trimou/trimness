package org.trimou.trimness.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.trimou.trimness.config.TrimnessKey.GLOBAL_JSON_FILE;
import static org.trimou.trimness.config.TrimnessKey.TEMPLATE_DIR;
import static org.trimou.trimness.test.Timeouts.DEFAULT_TIMEOUT;
import static org.trimou.trimness.util.Strings.RESULT_ID;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.enterprise.inject.spi.CDI;
import javax.json.JsonObject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.trimou.trimness.render.DelegateResultRepository;
import org.trimou.trimness.render.RenderObserver;
import org.trimou.trimness.render.Renderer;
import org.trimou.trimness.render.Result;
import org.trimou.trimness.render.ResultRepository;
import org.trimou.trimness.util.Jsons;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class RenderObserverTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return TrimnessTest.createDefaultClassPath()
                .add(ShrinkWrap.create(JavaArchive.class)
                        .addAsManifestResource(
                                new StringAsset(Jsons.objectBuilder()
                                        .add("foo", "bar").build().toString()),
                                "global-data.json"))
                .addSystemProperty(TEMPLATE_DIR.get(),
                        "src/test/resources/templates")
                .addSystemProperty(GLOBAL_JSON_FILE.get(),
                        "META-INF/global-data.json")
                .add(ShrinkWrap.create(JavaArchive.class)
                        .addClasses(RenderObserverTest.class, Timer.class))
                .build();
    }

    @Test
    public void testOneoffHello() throws InterruptedException {
        Vertx vertx = CDI.current().select(Vertx.class).get();
        BlockingQueue<Object> synchronizer = new LinkedBlockingQueue<>();
        vertx.eventBus().send(RenderObserver.ADDR_RENDER,
                Jsons.objectBuilder()
                        .add("templateContent", "Hello {{model.name}}!")
                        .add("model", Jsons.objectBuilder().add("name", "Lu"))
                        .build().toString(),
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
                Jsons.objectBuilder().add("templateId", "hello.txt")
                        .add("model", Jsons.arrayBuilder("Lu")).build()
                        .toString(),
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
    public void testHelloGlobalData() throws InterruptedException {
        Vertx vertx = CDI.current().select(Vertx.class).get();
        BlockingQueue<Object> synchronizer = new LinkedBlockingQueue<>();
        vertx.eventBus().send(RenderObserver.ADDR_RENDER,
                Jsons.objectBuilder().add("templateId", "hello-global-data.txt")
                        .add("model", Jsons.objectBuilder().add("name", 1))
                        .build().toString(),
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
        assertEquals("##hello-global-data.txt## Hello 1 and bar!",
                reply.toString());
    }

    @Test
    public void testInvalidInput() throws InterruptedException {
        Vertx vertx = CDI.current().select(Vertx.class).get();
        BlockingQueue<Object> synchronizer = new LinkedBlockingQueue<>();
        vertx.eventBus().send(RenderObserver.ADDR_RENDER,
                "{ \"templateContent\" : \"Hello\"", (result) -> {
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
        assertEquals(Renderer.ERR_CODE_INVALID_INPUT, exception.failureCode());
        assertEquals(ReplyFailure.RECIPIENT_FAILURE, exception.failureType());
    }

    @Test
    public void testHelloAsync() throws InterruptedException {
        Vertx vertx = CDI.current().select(Vertx.class).get();
        BlockingQueue<Object> synchronizer = new LinkedBlockingQueue<>();
        vertx.eventBus().send(RenderObserver.ADDR_RENDER,
                Jsons.objectBuilder().add("templateId", "hello.txt")
                        .add("model", Jsons.arrayBuilder("Lu"))
                        .add("async", true).build().toString(),
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
        JsonObject response = Jsons.asJsonObject(reply.toString());
        String resultId = response.getString(RESULT_ID);
        ResultRepository resultRepository = CDI.current()
                .select(DelegateResultRepository.class).get();

        Timer.of(DEFAULT_TIMEOUT).stopIf(() -> {
            Result result = resultRepository.get(resultId);
            if (result.isComplete()) {
                synchronizer.add(result.getValue());
                return true;
            }
            return false;
        }).countDown();
        assertEquals("Hello Lu!", synchronizer.poll().toString());
    }

}