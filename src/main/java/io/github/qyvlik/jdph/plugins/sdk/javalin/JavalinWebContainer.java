package io.github.qyvlik.jdph.plugins.sdk.javalin;

import io.github.qyvlik.jdph.go.error;
import io.github.qyvlik.jdph.plugins.sdk.IServerContext;
import io.github.qyvlik.jdph.plugins.sdk.IServerHandler;
import io.github.qyvlik.jdph.plugins.sdk.IWebContainer;
import io.github.qyvlik.jdph.plugins.volume.resp.ErrorResponse;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnixDomainSocketAddress;
import java.util.function.Consumer;

public class JavalinWebContainer implements IWebContainer {

    private final Javalin javalin;

    public JavalinWebContainer() {
        javalin = Javalin.create();
        javalin.get("/", ctx -> {
            ctx.status(HttpStatus.NO_CONTENT);
        });

    }

    public JavalinWebContainer(Consumer<JavalinConfig> config) {
        javalin = Javalin.create(config);
        javalin.get("/", ctx -> {
            ctx.status(HttpStatus.NO_CONTENT);
        });
    }

    @Override
    public void add(String path, IServerHandler handler) {
        javalin.post(path, ctx ->
                handler.handle(this.warp(ctx))
        );
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
            public <RESP> void write(RESP respBody) {
                ctx.json(respBody);
            }

            @Override
            public void write(error err) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .json(new ErrorResponse(err.Error()));
            }
        };
    }
}
