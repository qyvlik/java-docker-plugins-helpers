package io.github.qyvlik.jdph;


import io.vertx.core.Vertx;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new HTTPVerticle());
    }
}
