package io.github.qyvlik.jdph.plugins.volume;

import io.github.qyvlik.jdph.TestVolumeDriver;
import io.github.qyvlik.jdph.plugins.sdk.javalin.JavalinWebContainer;
//import io.javalin.event.HandlerMetaInfo;
//import io.javalin.event.LifecycleEventListener;
//import io.javalin.websocket.WsConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.UnixDomainSocketAddress;
import java.net.http.HttpRequest;

public class VolumeDriverTest {
    @Test
    public void test() throws IOException, InterruptedException {
        Server server = new Server(new TestVolumeDriver(), new JavalinWebContainer(config->{
            config.useVirtualThreads = true;
            config.showJavalinBanner = false;
        }));
        server.start(UnixDomainSocketAddress.of("/tmp/jdph.sock"));

        // curl --unix-socket /tmp/jdph.sock http://localhost/hoge.json

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("unix:/tmp/jdph.sock"))
                .build();

    }
}