package io.github.qyvlik.jdph.beard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.qyvlik.jdph.beard.render.Output;
import io.github.qyvlik.jdph.go.ret;
import io.github.qyvlik.jdph.plugins.volume.Volume;
import io.github.qyvlik.jdph.plugins.volume.req.GetRequest;
import io.github.qyvlik.jdph.plugins.volume.resp.GetResponse;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public final class BeardUtils {

    public static void write(Path prefix, Map<String, Output> outputs) throws IOException {
        for(Output output : outputs.values()) {
            FileUtils.write(
                    Path.of(prefix.toString(), output.filename()).toFile(),
                    output.content(),
                    StandardCharsets.UTF_8
            );
        }
    }

    public static String nowUTC() {
       return ZonedDateTime.now(
                Clock.systemUTC()
        ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
    }

    public static ret<GetResponse> getVolumeFromState(final String dataRootPath, String statePath, String Name, String action) {
        byte[] volumeBytes = null;
        try {
            volumeBytes = FileUtils.readFileToByteArray(
                    Path.of(dataRootPath, statePath, Name + ".json").toFile()
            );
        } catch (IOException e) {
            return ret.failure("%s %s volume, read file failure : %s", action, Name, e.getMessage());
        }

        ObjectMapper mapper = new ObjectMapper();
        Volume volume = null;
        try {
            volume = mapper.readValue(volumeBytes, Volume.class);
        } catch (IOException e) {
            return ret.failure("%s %s volume, parse json failure : %s", action, Name, e.getMessage());
        }

        return ret.success(new GetResponse(volume));
    }
}
