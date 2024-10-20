package qyvlik.github.io.jdph;



import qyvlik.github.io.jdph.plugins.sdk.vertx.VertxWebContainer;
import qyvlik.github.io.jdph.plugins.volume.Server;
import qyvlik.github.io.jdph.volume.ExampleVolumeDriver;

import java.io.IOException;
import java.net.UnixDomainSocketAddress;


public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server(new ExampleVolumeDriver(), new VertxWebContainer());
//        server.start(new InetSocketAddress(8080));

        server.start(UnixDomainSocketAddress.of("/tmp/jdph.sock"));
    }
}
