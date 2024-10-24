package io.github.qyvlik.jdph.examples.volume;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.qyvlik.jdph.go.error;
import io.github.qyvlik.jdph.go.ret;
import io.github.qyvlik.jdph.plugins.volume.Capability;
import io.github.qyvlik.jdph.plugins.volume.Driver;
import io.github.qyvlik.jdph.plugins.volume.Volume;
import io.github.qyvlik.jdph.plugins.volume.req.*;
import io.github.qyvlik.jdph.plugins.volume.resp.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * other example see <a href="https://github.com/intesar/SampleDockerVolumePlugin/blob/master/src/main/java/com/dchq/docker/volume/driver/adaptor/LocalVolumeAdaptorImpl.java">LocalVolumeAdaptorImpl</a>
 */
public class JDPHVolumeDriver implements Driver {
    public static final String VOLUME_MOUNT_POINT = "volumes";
    public static final String STATE_MOUNT_POINT = "states";

    private final Map<String, Renderer> renderers;

    private final String dataPath;

    public JDPHVolumeDriver(String dataPath) {
        if (!StringUtils.startsWith(dataPath, "/")) {
            throw new IllegalArgumentException("data path not starts with /");
        }
        Path volumePath = Path.of(dataPath, VOLUME_MOUNT_POINT);
        if (!volumePath.toFile().exists() && !volumePath.toFile().mkdirs()) {
            throw new IllegalStateException(String.format("%s create volume path error !", dataPath));
        }
        Path statePath = Path.of(dataPath, STATE_MOUNT_POINT);
        if (!statePath.toFile().exists() && !statePath.toFile().mkdirs()) {
            throw new IllegalStateException(String.format("%s create volume path error !", dataPath));
        }
        this.dataPath = dataPath;
        this.renderers = new ConcurrentSkipListMap<>();
    }

    public ret<GetResponse> getVolume(String Name, String action) {
        byte[] volumeBytes = null;
        try {
            volumeBytes = FileUtils.readFileToByteArray(
                    Path.of(dataPath, STATE_MOUNT_POINT, Name + ".json").toFile()
            );
        } catch (IOException e) {
            return ret.failure("%s %s volume, read file failure : %s", action, Name, e.getMessage());
        }

        ObjectMapper mapper = new ObjectMapper();
        Volume volume = null;
        try {
            volume = mapper.readValue(volumeBytes, Volume.class);
        } catch (IOException e) {
            if ("true".equalsIgnoreCase(System.getenv("DEBUG"))) {
                e.printStackTrace(System.out);
            }
            return ret.failure("%s %s volume, parse json failure : %s", action, Name, e.getMessage());
        }

        return ret.success(new GetResponse(volume));
    }

    @Override
    public error Create(CreateRequest request) {
        Renderer renderer = this.renderers.computeIfAbsent(request.Name(), (k) -> new Renderer());

        Map<String, Output> outputs = null;
        try {
            outputs = renderer.render(request);
        } catch (Exception e) {
            if ("true".equalsIgnoreCase(System.getenv("DEBUG"))) {
                e.printStackTrace(System.out);
            }
            return error.Create("create %s volume, render failure : %s", request.Name(), e.getMessage());
        }

        Path Mountpoint = Path.of(dataPath, VOLUME_MOUNT_POINT, request.Name());
        try {
            renderer.write(Mountpoint, outputs);
        } catch (Exception e) {
            if ("true".equalsIgnoreCase(System.getenv("DEBUG"))) {
                e.printStackTrace(System.out);
            }
            return error.Create("create %s volume, write files failure : %s", request.Name(), e.getMessage());
        }

        ObjectMapper mapper = new ObjectMapper();
        String CreatedAt = ZonedDateTime
                .now(Clock.systemUTC())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));

        try {
            byte[] jsonBytes = mapper.writeValueAsBytes(
                    new Volume(request.Name(), Mountpoint.toString(), CreatedAt, Map.of())
            );
            FileUtils.writeByteArrayToFile(
                    Path.of(dataPath, STATE_MOUNT_POINT, request.Name() + ".json").toFile(),
                    jsonBytes
            );
        } catch (IOException e) {
            if ("true".equalsIgnoreCase(System.getenv("DEBUG"))) {
                e.printStackTrace(System.out);
            }
            return error.Create("create %s volume, write state failure : %s", request.Name(), e.getMessage());
        }

        return null;
    }

    @Override
    public ret<GetResponse> Get(GetRequest request) {
        return getVolume(request.Name(), "Get");
    }

    // curl -d '{}' -H "Content-Type: application/json" -X POST http://localhost:8080/VolumeDriver.List
    @Override
    public ret<ListResponse> List() {
        List<Volume> volumeList = new ArrayList<>();

        File dir = Path.of(dataPath, STATE_MOUNT_POINT).toFile();
        File[] files = dir.listFiles((dir1, name) -> StringUtils.endsWith(name, ".json"));
        if (files != null) {
            for (File file : files) {
                String Name = StringUtils.removeEnd(file.getName(), ".json");
                ret<GetResponse> ret = this.getVolume(Name, "List");
                if (ret.err() == null) {
                    volumeList.add(ret.result().Volume());
                }
            }
        }

        return ret.success(new ListResponse(Collections.unmodifiableList(volumeList)));
    }


    @Override
    public error Remove(RemoveRequest request) {

        ret<GetResponse> r = this.getVolume(request.Name(), "Remove");
        if (r.err() != null) {
            return r.err();
        }

        try {
            FileUtils.forceDelete(Path.of(this.dataPath, STATE_MOUNT_POINT, request.Name() + ".json").toFile());
        } catch (Exception e) {
            if ("true".equalsIgnoreCase(System.getenv("DEBUG"))) {
                e.printStackTrace(System.out);
            }
            return error.Create("Remove %s volume, delete state : failure %s", request.Name(), e.getMessage());
        }

        try {
            Path Mountpoint = Path.of(dataPath, VOLUME_MOUNT_POINT, request.Name());
            FileUtils.forceDelete(Mountpoint.toFile());
        } catch (Exception e) {
            if ("true".equalsIgnoreCase(System.getenv("DEBUG"))) {
                e.printStackTrace(System.out);
            }
            return error.Create("Remove %s volume, delete file : failure %s", request.Name(), e.getMessage());
        }

        return null;
    }

    @Override
    public ret<PathResponse> Path(PathRequest request) {
        ret<GetResponse> r = this.getVolume(request.Name(), "Path");
        if (r.err() != null) {
            return ret.failure(r.err());
        }
        return ret.success(new PathResponse(r.result().Volume().Mountpoint()));
    }

    @Override
    public ret<MountResponse> Mount(MountRequest request) {
        ret<GetResponse> r = this.getVolume(request.Name(), "Mount");
        if (r.err() != null) {
            return ret.failure(r.err());
        }

        return ret.success(new MountResponse(r.result().Volume().Mountpoint()));
    }

    @Override
    public error Unmount(UnmountRequest request) {
        ret<GetResponse> r = this.getVolume(request.Name(), "Unmount");
        if (r.err() != null) {
            return r.err();
        }
        return null;
    }

    @Override
    public CapabilitiesResponse Capabilities() {
        return new CapabilitiesResponse(new Capability("local"));
    }
}
