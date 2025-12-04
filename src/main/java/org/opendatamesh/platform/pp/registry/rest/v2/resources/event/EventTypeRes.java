package org.opendatamesh.platform.pp.registry.rest.v2.resources.event;

import java.util.Locale;

public enum EventTypeRes {
    // --- EMITTED EVENT TYPES ---
    // Data Product Events
    DATA_PRODUCT_INITIALIZATION_REQUESTED,
    DATA_PRODUCT_INITIALIZED,
    DATA_PRODUCT_DELETED,
    // Data Product Version Events
    DATA_PRODUCT_VERSION_PUBLICATION_REQUESTED,
    DATA_PRODUCT_VERSION_PUBLISHED,
    DATA_PRODUCT_VERSION_DELETED,

    // --- RECEIVED EVENT TYPES ---
    // Data Product Events
    DATA_PRODUCT_INITIALIZATION_APPROVED,
    DATA_PRODUCT_INITIALIZATION_REJECTED,
    // Data Product Version Events
    DATA_PRODUCT_VERSION_INITIALIZATION_APPROVED,
    DATA_PRODUCT_VERSION_INITIALIZATION_REJECTED;

    public static EventTypeRes fromString(String value) {
        return EventTypeRes.valueOf(value.toUpperCase(Locale.ROOT));
    }
}
