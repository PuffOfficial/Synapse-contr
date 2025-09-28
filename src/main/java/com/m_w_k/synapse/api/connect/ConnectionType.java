package com.m_w_k.synapse.api.connect;

import org.jetbrains.annotations.NotNull;

public enum ConnectionType {
    UPSTREAM, DOWNSTREAM, EQUAL, UNKNOWN_UP, UNKNOWN_DOWN, UNKNOWN;

    public static final ConnectionType[] TYPES = values();

    public boolean upstream() {
        return this == UPSTREAM || this == UNKNOWN_UP;
    }

    public boolean downstream() {
        return this == DOWNSTREAM || this == UNKNOWN_DOWN;
    }

    public @NotNull ConnectionType flip() {
        return switch (this) {
            case UPSTREAM -> DOWNSTREAM;
            case DOWNSTREAM -> UPSTREAM;
            case UNKNOWN_UP -> UNKNOWN_DOWN;
            case UNKNOWN_DOWN -> UNKNOWN_UP;
            default -> this;
        };
    }
}
