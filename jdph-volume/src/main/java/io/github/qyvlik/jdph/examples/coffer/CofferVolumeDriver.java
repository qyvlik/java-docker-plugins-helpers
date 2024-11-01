package io.github.qyvlik.jdph.examples.coffer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.qyvlik.jdph.examples.coffer.fetcher.ContentType;
import io.github.qyvlik.jdph.examples.coffer.fetcher.Source;
import io.github.qyvlik.jdph.examples.coffer.fetcher.SourceFetcher;
import io.github.qyvlik.jdph.examples.coffer.fetcher.SourceSelector;
import io.github.qyvlik.jdph.examples.coffer.id.CredentialManager;
import io.github.qyvlik.jdph.examples.coffer.out.Marker;
import io.github.qyvlik.jdph.examples.coffer.state.VolumeState;
import io.github.qyvlik.jdph.go.error;
import io.github.qyvlik.jdph.go.ret;
import io.github.qyvlik.jdph.plugins.volume.Capability;
import io.github.qyvlik.jdph.plugins.volume.Driver;
import io.github.qyvlik.jdph.plugins.volume.Volume;
import io.github.qyvlik.jdph.plugins.volume.req.*;
import io.github.qyvlik.jdph.plugins.volume.resp.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class CofferVolumeDriver implements Driver {
    public static final String VOLUME_MOUNT_POINT = "volumes";
    public static final String STATE_MOUNT_POINT = "states";

    private final String path;
    private final SourceSelector secretSelector;
    private final SourceSelector templateSelector;

    public CofferVolumeDriver(String path, CredentialManager credentialManager) {
        if (!StringUtils.startsWith(path, "/")) {
            throw new IllegalArgumentException("path not starts with /");
        }
        Path volumePath = Path.of(path, VOLUME_MOUNT_POINT);
        if (!volumePath.toFile().exists() && !volumePath.toFile().mkdirs()) {
            throw new IllegalStateException(String.format("%s create volume path error !", path));
        }
        Path statePath = Path.of(path, STATE_MOUNT_POINT);
        if (!statePath.toFile().exists() && !statePath.toFile().mkdirs()) {
            throw new IllegalStateException(String.format("%s create volume path error !", path));
        }
        this.path = path;
        this.secretSelector = new SourceSelector(credentialManager);
        this.templateSelector = new SourceSelector(credentialManager);
    }

    public ret<VolumeState> getVolumeState(String Name, String action) {
        Path volumeStateFile = Path.of(this.path, STATE_MOUNT_POINT, Name + ".json");

        byte[] volumeStateBytes = null;
        try {
            volumeStateBytes = FileUtils.readFileToByteArray(volumeStateFile.toFile());
        } catch (IOException e) {
            log.debug("{} {} volume, read file failure", Name, action, e);
            return ret.failure("%s %s volume, read file failure : %s", action, Name, e.getMessage());
        }

        ObjectMapper mapper = new ObjectMapper();
        VolumeState volumeState = null;
        try {
            volumeState = mapper.readValue(volumeStateBytes, VolumeState.class);
        } catch (IOException e) {
            log.error("{} {} volume, parser json failure", Name, action, e);
            return ret.failure("%s %s volume, parse json failure : %s", action, Name, e.getMessage());
        }

        return ret.success(volumeState);
    }

    @Override
    public error Create(CreateRequest request) {
        Path MountPoint = Path.of(this.path, VOLUME_MOUNT_POINT, request.Name(), "_data");

        String CreatedAt = ZonedDateTime
                .now(Clock.systemUTC())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));

        VolumeState state = new VolumeState(
                request.Name(),
                request.Opts(),
                MountPoint.toString(),
                CreatedAt);

        ObjectMapper mapper = new ObjectMapper();
        byte[] jsonBytes = null;
        try {
            jsonBytes = mapper.writeValueAsBytes(state);
        } catch (Exception e) {
            log.error("create {} volume, state to json failure", request.Name(), e);
            return error.Create("create %s volume, state to json failure : %s", request.Name(), e.getMessage());
        }

        Path volumeStateFile = Path.of(this.path, STATE_MOUNT_POINT, request.Name() + ".json");

        try {
            FileUtils.writeByteArrayToFile(
                    volumeStateFile.toFile(),
                    jsonBytes
            );
        } catch (IOException e) {
            log.error("create {} volume, write state failure", request.Name(), e);
            return error.Create("create %s volume, write state failure : %s", request.Name(), e.getMessage());
        }


        return null;
    }

    @Override
    public ret<GetResponse> Get(GetRequest request) {
        ret<VolumeState> r = getVolumeState(request.Name(), "Get");
        if (r.err() != null) {
            return ret.failure(r.err());
        }
        VolumeState vs = r.result();
        Volume volume = new Volume(
                vs.Name(),
                vs.Mountpoint(),
                vs.CreatedAt(),
                Map.of()
        );

        return ret.success(new GetResponse(volume));
    }

    @Override
    public ret<ListResponse> List() {
        List<Volume> volumeList = new ArrayList<>();

        File dir = Path.of(this.path, STATE_MOUNT_POINT).toFile();
        File[] files = dir.listFiles((dir1, name) -> StringUtils.endsWith(name, ".json"));
        if (files != null) {
            for (File file : files) {
                String Name = StringUtils.removeEnd(file.getName(), ".json");
                ret<VolumeState> r = this.getVolumeState(Name, "List");
                if (r.err() == null) {
                    VolumeState vs = r.result();
                    Volume volume = new Volume(
                            vs.Name(),
                            vs.Mountpoint(),
                            vs.CreatedAt(),
                            Map.of()
                    );
                    volumeList.add(volume);
                }
            }
        }
        return ret.success(new ListResponse(Collections.unmodifiableList(volumeList)));
    }

    @Override
    public error Remove(RemoveRequest request) {
        ret<VolumeState> r = getVolumeState(request.Name(), "Remove");
        if (r.err() != null) {
            return r.err();
        }

        Path volumeStatePath = Path.of(this.path, STATE_MOUNT_POINT, request.Name() + ".json");
        try {
            FileUtils.forceDelete(volumeStatePath.toFile());
        } catch (Exception e) {
            log.error("remove {} volume, delete state file: failure", request.Name(), e);
            return error.Create("remove %s volume, delete state file: failure %s", request.Name(), e.getMessage());
        }
        Path volumePath = Path.of(this.path, VOLUME_MOUNT_POINT, request.Name());
        try {
            FileUtils.forceDelete(volumePath.toFile());
        } catch (FileNotFoundException e1) {
            log.debug("remove {} volume, already delete volumePath {}", request.Name(), volumePath, e1);
            return null;
        } catch (Exception e) {
            log.error("remove {} volume, delete volumePath {} : failure", request.Name(), volumePath, e);
            return error.Create("remove %s volume, delete volumePath %s: failure %s", request.Name(), volumePath.toString(), e.getMessage());
        }

        return null;
    }

    @Override
    public ret<PathResponse> Path(PathRequest request) {
        ret<VolumeState> r = this.getVolumeState(request.Name(), "Path");
        if (r.err() != null) {
            return ret.failure(r.err());
        }

        return ret.success(new PathResponse(r.result().Mountpoint()));
    }

    @Override
    public ret<MountResponse> Mount(MountRequest request) {
        ret<VolumeState> r = this.getVolumeState(request.Name(), "Mount");
        if (r.err() != null) {
            return ret.failure(r.err());
        }

        VolumeState vs = r.result();
        Map<String, String> opts = vs.Opts();

        Map<String, String> secretSourceParameter = new TreeMap<>();
        Map<String, String> templateSourceParameter = new TreeMap<>();

        for (Map.Entry<String, String> entry : opts.entrySet()) {
            String key = entry.getKey();
            if (StringUtils.startsWith(key, "secret.")) {
                key = StringUtils.removeStart(key, "secret.");
                secretSourceParameter.put(key, entry.getValue());
            }
            if (StringUtils.startsWith(key, "template.")) {
                key = StringUtils.removeStart(key, "template.");
                templateSourceParameter.put(key, entry.getValue());
            }
        }

        Path templateWorkspace = Path.of(this.path, VOLUME_MOUNT_POINT, request.Name(), "workspace", "template");

        SourceFetcher templateFetcher = this.templateSelector.fetcher(
                templateWorkspace.toString(), request.Name(), templateSourceParameter);

        Source templateSource = templateFetcher.get();
        if (templateSource.exception().isPresent()) {
            var e = templateSource.exception().get();
            log.error("mount {} volume, fetch template failure", request.Name(), e);
            return ret.failure("mount %s volume, fetch template failure : %s", request.Name(), e.getMessage());
        }

        Marker marker = new Marker(request.Name());
        if (templateSource.contentType() == ContentType.dir) {
            templateSource.object().ifPresent((stringObjectMap -> {
                for (Map.Entry<String, Object> entry : stringObjectMap.entrySet()) {
                    marker.add(entry.getKey(), (String) entry.getValue());
                }
            }));
        } else {
            templateSource.string().ifPresent((s -> {
                marker.add(templateSourceParameter.get(SourceSelector.SOURCE_PATH), s);
            }));
        }

        Path secretWorkspace = Path.of(this.path, VOLUME_MOUNT_POINT, request.Name(), "workspace", "secret");
        SourceFetcher secretFetcher = this.secretSelector.fetcher(
                secretWorkspace.toString(), request.Name(), secretSourceParameter);
        Source secretSource = secretFetcher.get();
        if (secretSource.exception().isPresent()) {
            var e = secretSource.exception().get();
            log.error("mount {} volume, fetch secret failure", request.Name(), e);
            return ret.failure("mount %s volume, fetch secret failure : %s", request.Name(), e.getMessage());
        }

        Map<String, String> outputs = new TreeMap<>();
        try {
            if (secretSource.contentType() == ContentType.text) {
                if (secretSource.string().isPresent()) {
                    outputs = marker.markByString(secretSource.string().get());
                }
            } else {
                if (secretSource.object().isPresent()) {
                    outputs = marker.markByMap(secretSource.object().get());
                }
            }
        } catch (Exception e) {
            log.error("mount {} volume, mark failure", request.Name(), e);
            return ret.failure("mount %s volume, mark failure : %s", request.Name(), e.getMessage());
        }

        Path mountPoint = Path.of(this.path, VOLUME_MOUNT_POINT, request.Name(), "_data");

        try {
            marker.write(mountPoint.toString(), outputs);
        } catch (IOException e) {
            log.error("mount {} volume, write files failure", request.Name(), e);
            return ret.failure("mount %s volume, write files failure : %s", request.Name(), e.getMessage());
        }

        return ret.success(new MountResponse(mountPoint.toString()));
    }

    @Override
    public error Unmount(UnmountRequest request) {
        ret<VolumeState> r = this.getVolumeState(request.Name(), "Mount");
        if (r.err() != null) {
            return r.err();
        }

        Path volumePath = Path.of(this.path, VOLUME_MOUNT_POINT, request.Name());
        try {
            FileUtils.forceDelete(volumePath.toFile());
        } catch (Exception e) {
            log.error("umount {} volume, delete volumePath {} : failure", request.Name(), volumePath, e);
            return error.Create("umount %s volume, delete volumePath %s: failure %s", request.Name(), volumePath.toString(), e.getMessage());
        }

        return null;
    }

    @Override
    public CapabilitiesResponse Capabilities() {
        return new CapabilitiesResponse(new Capability("local"));
    }
}
