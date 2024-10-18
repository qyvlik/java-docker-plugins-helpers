package io.github.qyvlik.jdph;

import io.github.qyvlik.jdph.plugins.sdk.javalin.JavalinWebContainer;
import io.github.qyvlik.jdph.plugins.volume.Server;
import io.javalin.Javalin;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnixDomainSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server(new TestVolumeDriver(), new JavalinWebContainer(config->{
            config.useVirtualThreads = true;
            config.showJavalinBanner = false;
        }));
        server.start(new InetSocketAddress(8080));
    }
}
