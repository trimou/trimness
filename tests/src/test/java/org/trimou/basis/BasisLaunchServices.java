package org.trimou.basis;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.se.api.LaunchServices;

import io.vertx.core.Vertx;

public class BasisLaunchServices extends LaunchServices {

    private Vertx vertx;

    @Override
    public void initialize() {
        BlockingQueue<Object> synchronizer = new LinkedBlockingQueue<>();
        vertx = Vertx.vertx();
        vertx.deployVerticle(new BasisVerticle(), (result) -> {
            if (result.succeeded()) {
                synchronizer.add(true);
            } else {
                synchronizer.add(result.cause());
            }
        });
        try {
            Object result = synchronizer.poll(3, TimeUnit.SECONDS);
            if (result == null) {
                throw new IllegalStateException();
            }
            if (result instanceof Throwable) {
                throw new IllegalStateException((Throwable) result);
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void shutdown() {
        if (vertx != null) {
            vertx.close();
        }
    }

}
