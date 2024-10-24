package io.github.qyvlik.jdph.plugins.volume;

import java.util.Map;

/**
 *
 * @param Name
 * @param Mountpoint            /path/under/PropagatedMount
 * @param CreatedAt             UTC datatime, format `yyyy-MM-dd'T'HH:mm:ss'Z'`
 * @param Status
 */
public record Volume(String Name,
                     String Mountpoint,
                     String CreatedAt,
                     Map<String, Object> Status) {
}
