package io.github.qyvlik.jdph.plugins.volume;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.github.qyvlik.jdph.plugins.sdk.IServerHandler;
import io.github.qyvlik.jdph.plugins.volume.req.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executor;

public class Server {
    private final Driver driver;
    private final Map<String, IServerHandler> handlers;

    public Server(final Driver driver) {
        this.driver = driver;
        this.handlers = new TreeMap<>();
        this.handlers.put("/VolumeDriver.Create", ctx -> {
            var req = ctx.read(CreateRequest.class);
            var err = this.driver.Create(req);
            if (err != null) {
                ctx.write(500, err);
                return;
            }
            ctx.write(200, Map.of());
        });

        this.handlers.put("/VolumeDriver.Get", ctx -> {
            var req = ctx.read(GetRequest.class);
            var ret = this.driver.Get(req);
            if (ret.err() != null) {
                ctx.write(500, ret.err());
                return;
            }
            ctx.write(200, ret.result());
        });

        this.handlers.put("/VolumeDriver.List", ctx -> {
            var ret = this.driver.List();
            if (ret.err() != null) {
                ctx.write(500, ret.err());
                return;
            }
            ctx.write(200, ret.result());
        });

        this.handlers.put("/VolumeDriver.Remove", ctx -> {
            var r = ctx.read(RemoveRequest.class);
            var err = this.driver.Remove(r);
            if (err != null) {
                ctx.write(500, err);
                return;
            }
            ctx.write(200, Map.of());
        });

        this.handlers.put("/VolumeDriver.Path", ctx -> {
            var req = ctx.read(PathRequest.class);
            var ret = this.driver.Path(req);
            if (ret.err() != null) {
                ctx.write(500, ret.err());
                return;
            }
            ctx.write(200, Map.of());
        });

        this.handlers.put("/VolumeDriver.Mount", ctx -> {
            var req = ctx.read(MountRequest.class);
            var ret = this.driver.Mount(req);
            if (ret.err() != null) {
                ctx.write(500, ret.err());
                return;
            }
            ctx.write(200, ret.result());
        });

        this.handlers.put("/VolumeDriver.Unmount", ctx -> {
            var r = ctx.read(UnmountRequest.class);
            var err = this.driver.Unmount(r);
            if (err != null) {
                ctx.write(500, err);
                return;
            }
            ctx.write(200, Map.of());
        });

        this.handlers.put("/VolumeDriver.Capabilities", ctx -> {
            var resp = this.driver.Capabilities();
            ctx.write(200, resp);
        });
    }

    public void start() {
    }
}
