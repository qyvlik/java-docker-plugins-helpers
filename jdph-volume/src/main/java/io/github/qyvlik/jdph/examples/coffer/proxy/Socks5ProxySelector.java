package io.github.qyvlik.jdph.examples.coffer.proxy;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.List;

public class Socks5ProxySelector extends ProxySelector {
    private final List<Proxy> proxies;

    public Socks5ProxySelector(InetSocketAddress address) {
        proxies = Collections.singletonList(
                new Proxy(Proxy.Type.SOCKS, address)
        );
    }

    @Override
    public List<Proxy> select(URI uri) {
        return proxies;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        throw new RuntimeException(ioe);
    }
}
