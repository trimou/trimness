package org.trimou.basis;

import io.vertx.core.Vertx;

/**
 *
 * @author Martin Kouba
 */
public class Basis {

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new BasisVerticle());
    }

}
