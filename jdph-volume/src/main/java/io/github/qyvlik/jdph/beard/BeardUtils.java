package io.github.qyvlik.jdph.beard;

import io.github.qyvlik.jdph.beard.render.Output;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
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
}
