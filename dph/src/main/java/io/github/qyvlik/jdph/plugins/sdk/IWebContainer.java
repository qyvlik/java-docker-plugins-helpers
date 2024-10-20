package io.github.qyvlik.jdph.plugins.sdk;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnixDomainSocketAddress;

public interface IWebContainer {

    void add(String path, IServerHandler handler);

    void start(InetSocketAddress address) throws IOException;

    void start(UnixDomainSocketAddress address) throws IOException;
}
