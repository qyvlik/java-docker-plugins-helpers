package io.github.qyvlik.jdph.vertx;

import io.github.qyvlik.jdph.go.error;
import io.github.qyvlik.jdph.plugins.sdk.IServerContext;
import io.github.qyvlik.jdph.plugins.sdk.IServerHandler;
import io.github.qyvlik.jdph.plugins.sdk.IWebContainer;
import io.github.qyvlik.jdph.plugins.volume.resp.ErrorResponse;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.BodyHandlerImpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnixDomainSocketAddress;

public class VertxWebContainer implements IWebContainer {
    private final HttpServer server;
    private final Router router;

    public VertxWebContainer() {
        Vertx vertx = Vertx.vertx(
                new VertxOptions()
                        .setPreferNativeTransport(true)
        );
        this.server = vertx.createHttpServer();
        this.router = Router.router(vertx);
        this.router.get("/").handler(ctx ->
                ctx.response().setStatusCode(204).end()
        );

        this.router.options("/").handler(ctx ->
                ctx.response().setStatusCode(204).end()
        );

        this.router.route().handler(new BodyHandlerImpl());

    }

    @Override
    public void add(String path, IServerHandler handler) {
        router.post(path)
                .handler(ctx -> handler.handle(this.warp(ctx)));
    }

    private IServerContext warp(RoutingContext ctx) {
        return new IServerContext() {
            @Override
            public <REQ> REQ read(Class<REQ> requestClazz) {
                var body = ctx.body();
                System.out.printf("DEBUG=%s read class = %s, body =%s \n", System.getenv("DEBUG"), requestClazz.getName(), body);
//                if("true".equalsIgnoreCase(System.getenv("DEBUG"))) {
//                }
                return body.asPojo(requestClazz);
            }

            @Override
            public <RESP> void write(RESP respBody) {
                ctx.json(respBody);
            }

            @Override
            public void write(error err) {
                ctx.response().setStatusCode(500);
                ctx.json(new ErrorResponse(err.Error()));
            }
        };
    }

    @Override
    public void start(InetSocketAddress address) throws IOException {
        server.requestHandler(router).listen(SocketAddress.inetSocketAddress(address));
    }

    @Override
    public void start(UnixDomainSocketAddress address) throws IOException {
        server.requestHandler(router)
                .listen(SocketAddress.domainSocketAddress(address.toString()))
                .andThen(new Handler<AsyncResult<HttpServer>>() {
                    @Override
                    public void handle(AsyncResult<HttpServer> event) {
                        if (event.failed()) {
                            event.cause().printStackTrace();
                        }
                    }
                })
        ;
    }
}
