package io.github.qyvlik.jdph.plugins.volume.req;

import java.util.Map;

public record CreateRequest(String Name, Map<String, String> Opts) {
}
