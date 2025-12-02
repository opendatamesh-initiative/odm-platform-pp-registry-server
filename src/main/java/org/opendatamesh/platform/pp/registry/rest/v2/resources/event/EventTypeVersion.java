package org.opendatamesh.platform.pp.registry.rest.v2.resources.event;

import java.util.Locale;

public enum EventTypeVersion {
    V1_0_0,
    V2_0_0;

    @Override
    public String toString() {
        return name().replace('_', '.');
    }

    public static EventTypeVersion fromString(String value) {
        String normalized = value.toUpperCase(Locale.ROOT).replace('.', '_');
        return EventTypeVersion.valueOf(normalized);
    }
}

