package org.trimou.trimness;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class MockVertxProducer {

    private ScheduledExecutorService executor;

    @Dependent
    @Produces
    public Vertx produceVertx() {
        executor = Executors.newScheduledThreadPool(1);
        Vertx vertx = Mockito.mock(Vertx.class);
        Mockito.when(vertx.setPeriodic(ArgumentMatchers.anyLong(), ArgumentMatchers.any()))
                .thenAnswer(new Answer<Long>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public Long answer(InvocationOnMock invocation) throws Throwable {
                        Object[] args = invocation.getArguments();
                        executor.scheduleAtFixedRate(() -> {
                            Handler<Long> handler = (Handler<Long>) args[1];
                            handler.handle(1l);
                        }, 0, (Long) args[0], TimeUnit.MILLISECONDS);
                        return 1l;
                    }
                });
        Mockito.when(vertx.setTimer(ArgumentMatchers.anyLong(), ArgumentMatchers.any())).thenAnswer(new Answer<Long>() {
            @SuppressWarnings("unchecked")
            @Override
            public Long answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                executor.schedule(() -> {
                    Handler<Long> handler = (Handler<Long>) args[1];
                    handler.handle(1l);
                }, (Long) args[0], TimeUnit.MILLISECONDS);
                return 1l;
            }
        });
        return vertx;
    }

    public void disposeVertx(@Disposes Vertx vertx) {
        if (executor != null) {
            executor.shutdown();
        }
    }
}
