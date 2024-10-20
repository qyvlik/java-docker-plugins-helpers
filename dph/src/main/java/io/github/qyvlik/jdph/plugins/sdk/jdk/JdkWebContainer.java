package io.github.qyvlik.jdph.plugins.sdk.jdk;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.qyvlik.jdph.plugins.sdk.IServerHandler;
import io.github.qyvlik.jdph.plugins.sdk.IWebContainer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnixDomainSocketAddress;

@Deprecated
public class JdkWebContainer implements IWebContainer {

    private final HttpServer server;

    public JdkWebContainer() throws IOException {
        this.server = HttpServer.create();
        this.server.createContext("/", (HttpExchange exchange) -> {
            exchange.sendResponseHeaders(204, 0);
        });
    }

    @Override
    public void add(String path, IServerHandler handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void start(InetSocketAddress address) throws IOException {
        this.server.bind(address, 0);
        this.server.start();
    }

    @Override
    public void start(UnixDomainSocketAddress address) throws IOException {
        throw new UnsupportedOperationException();
    }
}
