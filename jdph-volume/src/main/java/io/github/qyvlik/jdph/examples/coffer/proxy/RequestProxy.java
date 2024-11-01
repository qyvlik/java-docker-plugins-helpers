package io.github.qyvlik.jdph.examples.coffer.proxy;

import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;

public final class RequestProxy {
    public static ProxySelector proxy(String proxyUrl) {
        if (StringUtils.isBlank(proxyUrl)) {
            throw new IllegalArgumentException("proxyUrl is blank");
        }

        if (!StringUtils.startsWith(proxyUrl, "http://")
                && !StringUtils.startsWith(proxyUrl, "https://")
                && !StringUtils.startsWith(proxyUrl, "socks5://")) {
            throw new IllegalArgumentException("proxyUrl not start with http or https or socks");
        }

        URI uri = null;
        try {
            uri = URI.create(proxyUrl);
        } catch (Exception e) {
            throw new IllegalArgumentException("proxyUrl not uri");
        }

        InetSocketAddress address = new InetSocketAddress(uri.getHost(), uri.getPort());

        if (StringUtils.startsWith(proxyUrl, "http://") ||
                StringUtils.startsWith(proxyUrl, "https://")) {
            return ProxySelector.of(address);
        } else {
            return new Socks5ProxySelector(address);
        }
    }
}
