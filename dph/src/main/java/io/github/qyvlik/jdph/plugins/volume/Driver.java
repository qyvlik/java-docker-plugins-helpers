package io.github.qyvlik.jdph.plugins.volume;


import io.github.qyvlik.jdph.go.error;
import io.github.qyvlik.jdph.go.ret;
import io.github.qyvlik.jdph.plugins.volume.req.*;
import io.github.qyvlik.jdph.plugins.volume.resp.*;

public interface Driver {

    error Create(CreateRequest request);

    ret<ListResponse> List();

    ret<GetResponse> Get(GetRequest request);

    error Remove(RemoveRequest request);

    ret<PathResponse> Path(PathRequest request);

    ret<MountResponse> Mount(MountRequest request);

    error Unmount(UnmountRequest request);

    CapabilitiesResponse Capabilities();
}
