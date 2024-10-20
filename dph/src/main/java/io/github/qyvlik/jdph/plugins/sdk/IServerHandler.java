package io.github.qyvlik.jdph.plugins.sdk;

@FunctionalInterface
public interface IServerHandler {
    void handle(IServerContext context);
}
