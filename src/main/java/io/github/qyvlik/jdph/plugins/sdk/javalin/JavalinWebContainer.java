package io.github.qyvlik.jdph.plugins.sdk.javalin;

import io.github.qyvlik.jdph.plugins.sdk.IWebContainer;
import io.github.qyvlik.jdph.plugins.sdk.IServerContext;
import io.github.qyvlik.jdph.plugins.sdk.IServerHandler;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnixDomainSocketAddress;
import java.util.Map;

public class JavalinWebContainer implements IWebContainer {

    private final Javalin javalin;

    public JavalinWebContainer() {
        javalin = Javalin.create();
    }

    @Override
    public void add(String path, IServerHandler handler) {
        javalin.post(path, (ctx -> handler.handle(this.warp(ctx))));
    }


    @Override
    public void start(InetSocketAddress address) throws IOException {
        javalin.start(address.getHostName(), address.getPort());
    }

    @Override
    public void start(UnixDomainSocketAddress address) throws IOException {
        throw new UnsupportedOperationException();
    }

    private IServerContext warp(Context ctx) {
        return new IServerContext() {
            @Override
            public <REQ> REQ read(Class<REQ> requestClazz) {
                return ctx.bodyAsClass(requestClazz);
            }

            @Override
            public <RESP> void write(int statusCode, RESP respBody) {
                ctx.status(statusCode)
                        .json(respBody);
            }
        };
    }
}
