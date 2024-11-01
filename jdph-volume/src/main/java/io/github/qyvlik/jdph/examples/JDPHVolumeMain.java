package io.github.qyvlik.jdph.examples;

import io.github.qyvlik.jdph.examples.coffer.CofferVolumeDriver;
import io.github.qyvlik.jdph.examples.coffer.id.CredentialManager;
import io.github.qyvlik.jdph.examples.coffer.id.env.MemoryCredentialManager;
import io.github.qyvlik.jdph.plugins.volume.Server;
import io.github.qyvlik.jdph.vertx.VertxWebContainer;
import java.io.PrintStream;
import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.util.Map;

public class JDPHVolumeMain {
    public static void main(String[] args) throws IOException {

        String logFile = System.getenv("LOG_FILE");
        if (logFile != null && !logFile.isBlank()) {
            var ps = new PrintStream(logFile);
            System.setOut(ps);
            System.setErr(ps);
        }

        Map<String, String> envs = System.getenv();
        CredentialManager cm = MemoryCredentialManager.create(envs, MemoryCredentialManager.CREDENTIAL_PREFIX);

        if ("true".equalsIgnoreCase(System.getenv("DEBUG"))) {
            Server server = new Server(new CofferVolumeDriver("/jdph-volume", cm), new VertxWebContainer());
            server.start(UnixDomainSocketAddress.of("/run/docker/plugins/jdph-volume.sock"));
        } else {
            // local dev
            Server server = new Server(new CofferVolumeDriver("/tmp/jdph-volume", cm), new VertxWebContainer());
            server.start(UnixDomainSocketAddress.of("/tmp/jdph-volume.sock"));
        }
    }
}