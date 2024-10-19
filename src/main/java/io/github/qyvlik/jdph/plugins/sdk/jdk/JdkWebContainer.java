package io.github.qyvlik.jdph.plugins.sdk.jdk;

import com.sun.net.httpserver.HttpServer;
import io.github.qyvlik.jdph.plugins.sdk.IServerHandler;
import io.github.qyvlik.jdph.plugins.sdk.IWebContainer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnixDomainSocketAddress;

public class JdkWebContainer implements IWebContainer {

    public JdkWebContainer() throws IOException {
        HttpServer server = HttpServer.create();
        server.start();
    }

    @Override
    public void add(String path, IServerHandler handler) {

    }

    @Override
    public void start(InetSocketAddress address) throws IOException {

    }

    @Override
    public void start(UnixDomainSocketAddress address) throws IOException {

    }
}
