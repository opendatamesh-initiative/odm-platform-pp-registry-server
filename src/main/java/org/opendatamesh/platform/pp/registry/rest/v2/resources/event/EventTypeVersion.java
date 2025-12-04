package org.opendatamesh.platform.pp.registry.rest.v2.resources.event;

public enum EventTypeVersion {
    V1_0_0("V1.0.0"),
    V2_0_0("V2.0.0");

    private final String label;

    EventTypeVersion(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    public static EventTypeVersion fromString(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("EventTypeVersion value cannot be null or empty");
        }
        
        for (EventTypeVersion version : EventTypeVersion.values()) {
            if (version.label.equalsIgnoreCase(value)) {
                return version;
            }
        }
        
        throw new IllegalArgumentException("Unknown EventTypeVersion: " + value);
    }
}

