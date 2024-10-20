package io.github.qyvlik.jdph;

import io.github.qyvlik.jdph.example.JDPHVolumeDriver;
import io.github.qyvlik.jdph.plugins.volume.Server;
import io.github.qyvlik.jdph.vertx.VertxWebContainer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.UnixDomainSocketAddress;

public class JDPHVolumeMain {

    public static void main(String[] args) throws IOException {
        System.setOut(new PrintStream(new File("/var/log/plugin.log")));
        Server server = new Server(new JDPHVolumeDriver(), new VertxWebContainer());
        server.start(UnixDomainSocketAddress.of("/run/docker/plugins/jdph-volume.sock"));
    }
}