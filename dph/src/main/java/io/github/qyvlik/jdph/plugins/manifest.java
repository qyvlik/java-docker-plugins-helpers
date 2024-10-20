package io.github.qyvlik.jdph.plugins;

import java.util.List;

public record manifest(List<String> Implements) {
    public static final manifest IPAM = new manifest(List.of("IpamDriver"));
    public static final manifest NETWORK = new manifest(List.of("NetworkDriver"));
    public static final manifest SECRET_PROVIDER = new manifest(List.of("secretprovider"));
    public static final manifest VOLUME = new manifest(List.of("VolumeDriver"));
}
