package qyvlik.github.io.jdph.plugins.sdk;

@FunctionalInterface
public interface IServerHandler {
    void handle(IServerContext context);
}
