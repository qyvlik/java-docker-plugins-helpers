package io.github.qyvlik.jdph.example;


import io.github.qyvlik.jdph.beard.Renderer;
import io.github.qyvlik.jdph.go.error;
import io.github.qyvlik.jdph.go.ret;
import io.github.qyvlik.jdph.plugins.volume.Capability;
import io.github.qyvlik.jdph.plugins.volume.Driver;
import io.github.qyvlik.jdph.plugins.volume.Volume;
import io.github.qyvlik.jdph.plugins.volume.req.*;
import io.github.qyvlik.jdph.plugins.volume.resp.*;
import org.apache.commons.lang3.StringUtils;

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

    public JDPHVolumeDriver(String dataRootPath) {
        if (Path.of(dataRootPath, VOLUME_MOUNT_POINT).toFile().mkdirs()) {
            throw new IllegalStateException(String.format("%s create volume path error !", dataRootPath));
        }
        if (Path.of(dataRootPath, STATE_MOUNT_POINT).toFile().mkdirs()) {
            throw new IllegalStateException(String.format("%s create volume path error !", dataRootPath));
        }
        this.renderers = new ConcurrentSkipListMap<>();
    }

    public JDPHVolumeDriver() {
        this("/data/jdph-volume");
    }

    // curl -d '{"Name": "Hello"}' -H "Content-Type: application/json" -X POST http://localhost:8080/VolumeDriver.Create
    @Override
    public error Create(CreateRequest request) {


        for (Map.Entry<String, String> entry : request.Opts().entrySet()) {
            if (entry.getKey().equals("secret.source")) {
                String secretSource = entry.getValue();
            }
            if (entry.getKey().equals("secret.content-type")) {
                String secretContentType = entry.getValue();
            }
            if (entry.getKey().startsWith("template.content.")) {
                String app = StringUtils.removeStart(entry.getKey(), "mustache.content.");
                String mustacheContent = entry.getValue();
            }
            if (entry.getKey().startsWith("template.output.")) {
                String app = StringUtils.removeEnd(entry.getKey(), "mustache.output.");
                String mustacheOutput = entry.getValue();
            }
        }


        this.renderers.compute(request.Name(), (key, oldRenderer) -> {
            boolean renew = oldRenderer == null;
            Renderer renderer = renew ? new Renderer() : oldRenderer;
            renderer.clear();



            return renew ? renderer : null;
        });


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
