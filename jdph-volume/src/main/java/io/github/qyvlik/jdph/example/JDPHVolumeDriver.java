package io.github.qyvlik.jdph.example;


import io.github.qyvlik.jdph.beard.BeardUtils;
import io.github.qyvlik.jdph.beard.render.Output;
import io.github.qyvlik.jdph.beard.render.Renderer;
import io.github.qyvlik.jdph.beard.secret.HttpSecretSource;
import io.github.qyvlik.jdph.beard.secret.SecretContentType;
import io.github.qyvlik.jdph.beard.secret.SecretSource;
import io.github.qyvlik.jdph.go.error;
import io.github.qyvlik.jdph.go.ret;
import io.github.qyvlik.jdph.plugins.volume.Capability;
import io.github.qyvlik.jdph.plugins.volume.Driver;
import io.github.qyvlik.jdph.plugins.volume.Volume;
import io.github.qyvlik.jdph.plugins.volume.req.*;
import io.github.qyvlik.jdph.plugins.volume.resp.*;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * other example see <a href="https://github.com/intesar/SampleDockerVolumePlugin/blob/master/src/main/java/com/dchq/docker/volume/driver/adaptor/LocalVolumeAdaptorImpl.java">LocalVolumeAdaptorImpl</a>
 */
public class JDPHVolumeDriver implements Driver {
    private Set<String> volumes;
    private int create;
    private int get;
    private int list;
    private int path;
    private int mount;
    private int unmount;
    private int remove;
    private int capabilities;

    public static final String VOLUME_MOUNT_POINT = "volumes";
    public static final String STATE_MOUNT_POINT = "states";

    private final Map<String, Renderer> renderers;

    private final String dataRootPath;

    public JDPHVolumeDriver(String dataRootPath) {
        if (Path.of(dataRootPath, VOLUME_MOUNT_POINT).toFile().mkdirs()) {
            throw new IllegalStateException(String.format("%s create volume path error !", dataRootPath));
        }
        if (Path.of(dataRootPath, STATE_MOUNT_POINT).toFile().mkdirs()) {
            throw new IllegalStateException(String.format("%s create volume path error !", dataRootPath));
        }
        this.dataRootPath = dataRootPath;
        this.renderers = new ConcurrentSkipListMap<>();
    }

    public JDPHVolumeDriver() {
        this("/data/jdph-volume");
    }

    private SecretSource create(String secretSource, String secretContentType) {
        try {
            return new HttpSecretSource(URI.create(secretSource), SecretContentType.valueOf(secretContentType));
        } catch (Exception e) {
            System.out.printf("create secret source failure :%s \n", e.getMessage());
            return null;
        }
    }

    @Override
    public error Create(CreateRequest request) {
        Renderer renderer = this.renderers.computeIfAbsent(request.Name(), (k) -> new Renderer());

        Map<String, Output> outputs = null;
        try {
            outputs = renderer.render(request);
        } catch (Exception e) {
            return error.Create("create %s volume, render failure : %s", request.Name(), e.getMessage());
        }

        Path prefix = Path.of(dataRootPath, VOLUME_MOUNT_POINT, request.Name());
        try {
            BeardUtils.write(prefix, outputs);
        } catch (Exception e) {
            return error.Create("create %s volume, write failure : %s", request.Name(), e.getMessage());
        }

        // todo write to state

        return null;
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
