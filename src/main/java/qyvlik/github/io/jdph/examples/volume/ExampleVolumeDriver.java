package qyvlik.github.io.jdph.examples.volume;

import qyvlik.github.io.jdph.go.error;
import qyvlik.github.io.jdph.go.ret;
import qyvlik.github.io.jdph.plugins.volume.Capability;
import qyvlik.github.io.jdph.plugins.volume.Driver;
import qyvlik.github.io.jdph.plugins.volume.Volume;
import qyvlik.github.io.jdph.plugins.volume.req.*;
import qyvlik.github.io.jdph.plugins.volume.resp.*;

import java.util.*;

public class ExampleVolumeDriver implements Driver {
    private Set<String> volumes;
    private int create;
    private int get;
    private int list;
    private int path;
    private int mount;
    private int unmount;
    private int remove;
    private int capabilities;

    public ExampleVolumeDriver() {
        this.volumes = new TreeSet<>();
        this.create = 0;
        this.get = 0;
        this.list = 0;
        this.path = 0;
        this.mount = 0;
        this.unmount = 0;
        this.remove = 0;
        this.capabilities = 0;
    }

    // curl -d '{"Name": "Hello"}' -H "Content-Type: application/json" -X POST http://localhost:8080/VolumeDriver.Create
    @Override
    public error Create(CreateRequest request) {
        this.create++;
        this.volumes.add(request.Name());
        return null;
    }

    // curl -d '{}' -H "Content-Type: application/json" -X POST http://localhost:8080/VolumeDriver.List
    @Override
    public ret<ListResponse> List() {
        this.list++;
        List<Volume> volumeList = new ArrayList<>();
        for (String name : this.volumes) {
            volumeList.add(new Volume(
                    name,
                    null,
                    null,
                    null
            ));
        }
        return ret.success(new ListResponse(Collections.unmodifiableList(volumeList)));
    }

    @Override
    public ret<GetResponse> Get(GetRequest request) {
        this.get++;
        if (this.volumes.contains(request.Name())) {
            return ret.success(new GetResponse(new Volume(
                    request.Name(),
                    null,
                    null,
                    null
            )));
        }
        return ret.failure("no such volume");
    }

    @Override
    public error Remove(RemoveRequest request) {
        this.remove++;
        if (this.volumes.contains(request.Name())) {
            this.volumes.remove(request.Name());
            return null;
        }
        return error.Create("no such volume");
    }

    @Override
    public ret<PathResponse> Path(PathRequest request) {
        this.path++;
        if (this.volumes.contains(request.Name())) {
            return ret.success(new PathResponse(null));
        }
        return ret.failure("no such volume");
    }

    @Override
    public ret<MountResponse> Mount(MountRequest request) {
        this.mount++;
        if (this.volumes.contains(request.Name())) {
            return ret.success(new MountResponse(null));
        }

        return ret.failure("no such volume");
    }

    @Override
    public error Unmount(UnmountRequest request) {
        this.unmount++;
        if (this.volumes.contains(request.Name())) {
            return null;
        }
        return error.Create("no such volume");
    }

    @Override
    public CapabilitiesResponse Capabilities() {
        this.capabilities++;
        return new CapabilitiesResponse(new Capability("local"));
    }
}
