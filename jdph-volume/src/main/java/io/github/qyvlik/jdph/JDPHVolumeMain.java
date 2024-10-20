package io.github.qyvlik.jdph;

import io.github.qyvlik.jdph.example.JDPHVolumeDriver;
import io.github.qyvlik.jdph.plugins.volume.Server;
import io.github.qyvlik.jdph.vertx.VertxWebContainer;

import java.io.IOException;
import java.net.UnixDomainSocketAddress;

public class JDPHVolumeMain {

    public static void main(String[] args) throws IOException {
        Server server = new Server(new JDPHVolumeDriver(), new VertxWebContainer());
        server.start(UnixDomainSocketAddress.of("/run/docker/plugins/jdph-volume.sock"));
    }
}