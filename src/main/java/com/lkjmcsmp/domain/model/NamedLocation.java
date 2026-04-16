package com.lkjmcsmp.domain.model;

public record NamedLocation(
        String name,
        String world,
        double x,
        double y,
        double z,
        float yaw,
        float pitch) {
}
