package io.github.qyvlik.jdph.plugins.volume;


import io.github.qyvlik.jdph.go.error;
import io.github.qyvlik.jdph.go.ret;
import io.github.qyvlik.jdph.plugins.volume.req.*;
import io.github.qyvlik.jdph.plugins.volume.resp.*;

import java.io.IOException;

/**
 * https://docs.docker.com/engine/extend/plugins_volume/#volumedrivercreate
 */
public interface Driver {

    error Create(CreateRequest request);

    ret<GetResponse> Get(GetRequest request);

    ret<ListResponse> List();

    error Remove(RemoveRequest request);

    ret<PathResponse> Path(PathRequest request);

    ret<MountResponse> Mount(MountRequest request);

    error Unmount(UnmountRequest request);

    CapabilitiesResponse Capabilities();
}
