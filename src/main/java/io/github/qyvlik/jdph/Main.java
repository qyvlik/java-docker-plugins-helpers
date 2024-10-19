package io.github.qyvlik.jdph;

import io.github.qyvlik.jdph.examples.volume.ExampleVolumeDriver;
import io.github.qyvlik.jdph.plugins.sdk.vertx.VertxWebContainer;
import io.github.qyvlik.jdph.plugins.volume.Server;

import java.io.IOException;
import java.net.UnixDomainSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
//        Server server = new Server(new ExampleVolumeDriver(), new JavalinWebContainer(config->{
//            config.useVirtualThreads = true;
//            config.showJavalinBanner = false;
//        }));
//        server.start(new InetSocketAddress(8080));

        Server server = new Server(new ExampleVolumeDriver(), new VertxWebContainer());
//        server.start(new InetSocketAddress(8080));

        server.start(UnixDomainSocketAddress.of("/tmp/jdph.sock"));

    }
}
