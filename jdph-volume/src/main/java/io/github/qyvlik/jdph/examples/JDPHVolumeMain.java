package io.github.qyvlik.jdph.examples;

import io.github.qyvlik.jdph.examples.volume.JDPHVolumeDriver;
import io.github.qyvlik.jdph.plugins.volume.Server;
import io.github.qyvlik.jdph.vertx.VertxWebContainer;

import java.io.IOException;
import java.io.PrintStream;
import java.net.UnixDomainSocketAddress;

public class JDPHVolumeMain {
    public static void main(String[] args) throws IOException {
        String logFile = System.getenv("LOG_FILE");
        if (logFile != null && !logFile.isBlank()) {
            var ps = new PrintStream(logFile);
            System.setOut(ps);
            System.setErr(ps);
        }

        if ("true".equalsIgnoreCase(System.getenv("DEBUG"))) {
            Server server = new Server(new JDPHVolumeDriver("/data/jdph-volume"), new VertxWebContainer());
            server.start(UnixDomainSocketAddress.of("/run/docker/plugins/jdph-volume.sock"));
        } else {
            // local dev
            Server server = new Server(new JDPHVolumeDriver("/tmp/jdph-volume"), new VertxWebContainer());
            server.start(UnixDomainSocketAddress.of("/tmp/jdph-volume.sock"));
        }
    }
}