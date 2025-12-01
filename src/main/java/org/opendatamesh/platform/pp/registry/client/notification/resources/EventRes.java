package org.opendatamesh.platform.pp.registry.client.notification.resources;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Locale;

public class EventRes {
    private ResourceType resourceType;
    private String resourceIdentifier;
    private EventTypeRes type;
    private EventTypeVersion eventTypeVersion;
    private EventContent eventContent;

    public EventRes() {

    }

    public EventRes(ResourceType resourceType, String resourceIdentifier, EventTypeRes type, EventTypeVersion eventTypeVersion, EventContent eventContent) {
        this.resourceType = resourceType;
        this.resourceIdentifier = resourceIdentifier;
        this.type = type;
        this.eventTypeVersion = eventTypeVersion;
        this.eventContent = eventContent;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceIdentifier() {
        return resourceIdentifier;
    }

    public void setResourceIdentifier(String resourceIdentifier) {
        this.resourceIdentifier = resourceIdentifier;
    }

    public EventTypeRes getType() {
        return type;
    }

    public void setType(EventTypeRes type) {
        this.type = type;
    }

    public EventTypeVersion getEventTypeVersion() {
        return eventTypeVersion;
    }

    public void setEventTypeVersion(EventTypeVersion eventTypeVersion) {
        this.eventTypeVersion = eventTypeVersion;
    }

    public EventContent getEventContent() {
        return eventContent;
    }

    public void setEventContent(EventContent eventContent) {
        this.eventContent = eventContent;
    }

    // Enums for resource type, event type and event type version

    public enum ResourceType {
        DATA_PRODUCT,
        DATA_PRODUCT_VERSION;

        public static ResourceType fromString(String value) {
            return ResourceType.valueOf(value.toUpperCase(Locale.ROOT));
        }
    }

    public enum EventTypeVersion {
        V1_0_0;

        public static EventTypeVersion fromString(String value) {
            return EventTypeVersion.valueOf(value.toUpperCase(Locale.ROOT));
        }
    }
}
