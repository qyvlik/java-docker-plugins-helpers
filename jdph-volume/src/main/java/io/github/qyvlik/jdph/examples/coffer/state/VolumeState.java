package io.github.qyvlik.jdph.examples.coffer.state;

import java.util.Map;

/**
 *
 * @param Name          Volume Name
 * @param Opts          Volume driver_opts
 * @param Mountpoint
 * @param CreatedAt     UTC datatime, format `yyyy-MM-dd'T'HH:mm:ss'Z'`
 */
public record VolumeState(String Name,
                          Map<String, String> Opts,
                          String Mountpoint,
                          String CreatedAt) {
}
