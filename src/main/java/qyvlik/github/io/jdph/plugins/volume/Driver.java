package qyvlik.github.io.jdph.plugins.volume;

import qyvlik.github.io.jdph.go.error;
import qyvlik.github.io.jdph.go.ret;
import qyvlik.github.io.jdph.plugins.volume.req.*;
import qyvlik.github.io.jdph.plugins.volume.resp.*;

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
