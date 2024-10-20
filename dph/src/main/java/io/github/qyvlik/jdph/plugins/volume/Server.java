package io.github.qyvlik.jdph.plugins.volume;


import io.github.qyvlik.jdph.plugins.sdk.IWebContainer;
import io.github.qyvlik.jdph.plugins.volume.req.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnixDomainSocketAddress;
import java.util.Map;

public class Server {
    private final Driver driver;
    private final IWebContainer container;

    public Server(final Driver driver, final IWebContainer container) {
        this.driver = driver;
        this.container = container;
        container.add("/VolumeDriver.Create", ctx -> {
            var req = ctx.read(CreateRequest.class);
            var err = this.driver.Create(req);
            if (err != null) {
                ctx.write(err);
                return;
            }
            ctx.write(Map.of());
        });

        container.add("/VolumeDriver.Get", ctx -> {
            var req = ctx.read(GetRequest.class);
            var ret = this.driver.Get(req);
            if (ret.err() != null) {
                ctx.write(ret.err());
                return;
            }
            ctx.write(ret.result());
        });

        container.add("/VolumeDriver.List", ctx -> {
            var ret = this.driver.List();
            if (ret.err() != null) {
                ctx.write(ret.err());
                return;
            }
            ctx.write(ret.result());
        });

        container.add("/VolumeDriver.Remove", ctx -> {
            var r = ctx.read(RemoveRequest.class);
            var err = this.driver.Remove(r);
            if (err != null) {
                ctx.write(err);
                return;
            }
            ctx.write(Map.of());
        });

        container.add("/VolumeDriver.Path", ctx -> {
            var req = ctx.read(PathRequest.class);
            var ret = this.driver.Path(req);
            if (ret.err() != null) {
                ctx.write(ret.err());
                return;
            }
            ctx.write(Map.of());
        });

        container.add("/VolumeDriver.Mount", ctx -> {
            var req = ctx.read(MountRequest.class);
            var ret = this.driver.Mount(req);
            if (ret.err() != null) {
                ctx.write(ret.err());
                return;
            }
            ctx.write(ret.result());
        });

        container.add("/VolumeDriver.Unmount", ctx -> {
            var r = ctx.read(UnmountRequest.class);
            var err = this.driver.Unmount(r);
            if (err != null) {
                ctx.write(err);
                return;
            }
            ctx.write(Map.of());
        });

        container.add("/VolumeDriver.Capabilities", ctx -> {
            var resp = this.driver.Capabilities();
            ctx.write(resp);
        });
    }

    public void start(InetSocketAddress address) throws IOException {
        this.container.start(address);
    }

    public void start(UnixDomainSocketAddress address) throws IOException {
        this.container.start(address);
    }
}
