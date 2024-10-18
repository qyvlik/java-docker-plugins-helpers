package io.github.qyvlik.jdph.plugins.volume;

import java.util.Map;

public record Volume(String Name,
                     String Mountpoint,
                     String CreatedAt,
                     Map<String, Object> Status) {
}
