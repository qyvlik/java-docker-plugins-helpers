package qyvlik.github.io.jdph.plugins.sdk.jdk;

import com.sun.net.httpserver.HttpServer;
import qyvlik.github.io.jdph.plugins.sdk.IServerHandler;
import qyvlik.github.io.jdph.plugins.sdk.IWebContainer;

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
