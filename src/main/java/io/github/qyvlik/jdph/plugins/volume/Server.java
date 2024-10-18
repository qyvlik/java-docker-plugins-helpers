package io.github.qyvlik.jdph.plugins.volume;

import io.github.qyvlik.jdph.plugins.volume.req.*;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

public class Server {

    private final Driver driver;
    private Javalin server;


    public Server(final Driver driver, final InetSocketAddress address) throws IOException {
        this.driver = driver;
    }

    public void initMux() {

        this.server = Javalin.create(/*config*/);

        this.server.post("/VolumeDriver.Create", ctx -> {
            var req = ctx.bodyAsClass(CreateRequest.class);
            var err = this.driver.Create(req);
            if (err != null) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
                ctx.json(err);
                return;
            }
            ctx.json(Map.of());
        });

        this.server.post("/VolumeDriver.Get", ctx -> {
            var req = ctx.bodyAsClass(GetRequest.class);
            var ret = this.driver.Get(req);
            if (ret.err() != null) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
                ctx.json(ret.err());
                return;
            }
            ctx.json(ret.result());
        });

        this.server.post("/VolumeDriver.List", ctx -> {
            var ret = this.driver.List();
            if (ret.err() != null) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
                ctx.json(ret.err());
                return;
            }
            ctx.json(ret.result());
        });

        this.server.post("/VolumeDriver.Remove", ctx -> {
            var r = ctx.bodyAsClass(RemoveRequest.class);
            var err = this.driver.Remove(r);
            if (err != null) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
                ctx.json(err);
                return;
            }
            ctx.json(Map.of());
        });

        this.server.post("/VolumeDriver.Path", ctx -> {
            var req = ctx.bodyAsClass(PathRequest.class);
            var ret = this.driver.Path(req);
            if (ret.err() != null) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
                ctx.json(ret.err());
                return;
            }
            ctx.json(Map.of());
        });

        this.server.post("/VolumeDriver.Mount", ctx -> {
            var req = ctx.bodyAsClass(MountRequest.class);
            var ret = this.driver.Mount(req);
            if (ret.err() != null) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
                ctx.json(ret.err());
                return;
            }
            ctx.json(ret.result());
        });

        this.server.post("/VolumeDriver.Unmount", ctx -> {
            var r = ctx.bodyAsClass(UnmountRequest.class);
            var err = this.driver.Unmount(r);
            if (err != null) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
                ctx.json(err);
                return;
            }
            ctx.json(Map.of());
        });

        this.server.post("/VolumeDriver.Capabilities", ctx -> {
            var resp = this.driver.Capabilities();
            ctx.json(resp);
        });

    }

}
