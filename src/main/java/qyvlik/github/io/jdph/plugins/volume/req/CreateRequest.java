package qyvlik.github.io.jdph.plugins.volume.req;

import java.util.Map;

public record CreateRequest(String Name, Map<String, String> Options) {
}
