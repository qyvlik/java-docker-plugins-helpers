package io.github.qyvlik.jdph.plugins.sdk;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnixDomainSocketAddress;
import java.util.Map;

public interface IWebContainer {
    void handle(Map<String, IServerHandler> handlers);

    void start(InetSocketAddress address) throws IOException;

    void start(UnixDomainSocketAddress address) throws IOException;
}
