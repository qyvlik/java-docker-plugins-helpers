package io.github.qyvlik.jdph.plugins.sdk;

import io.github.qyvlik.jdph.go.error;

public interface IServerContext {
    <REQ> REQ read(Class<REQ> requestClazz);
    <RESP> void write(RESP respBody);

    void write(error err);
}
