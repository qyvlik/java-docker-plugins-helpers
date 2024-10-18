package io.github.qyvlik.jdph.plugins.sdk;

public interface IServerContext {
    <REQ> REQ read(Class<REQ> requestClazz);
    <RESP> void write(int statusCode, RESP respBody);
}
