package qyvlik.github.io.jdph.plugins.sdk;

import qyvlik.github.io.jdph.go.error;

public interface IServerContext {
    <REQ> REQ read(Class<REQ> requestClazz);
    <RESP> void write(RESP respBody);

    void write(error err);
}
