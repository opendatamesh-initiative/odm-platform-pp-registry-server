package org.opendatamesh.platform.pp.registry.rest.v2.resources.event;

import java.util.Locale;

public enum ResourceType {
    DATA_PRODUCT,
    DATA_PRODUCT_VERSION;

    public static ResourceType fromString(String value) {
        return ResourceType.valueOf(value.toUpperCase(Locale.ROOT));
    }
}

