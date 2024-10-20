package io.github.qyvlik.jdph;


import io.vertx.core.Vertx;


public class Main {
    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new HTTPVerticle());
    }
}
