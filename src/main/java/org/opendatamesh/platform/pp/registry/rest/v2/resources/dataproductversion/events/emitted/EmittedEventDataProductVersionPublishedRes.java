package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.events.emitted;

import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeVersion;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.ResourceType;

public class EmittedEventDataProductVersionPublishedRes {
    private final ResourceType resourceType = ResourceType.DATA_PRODUCT_VERSION;
    private String resourceIdentifier;
    private final EventTypeRes type = EventTypeRes.DATA_PRODUCT_VERSION_PUBLISHED;
    private final EventTypeVersion eventTypeVersion = EventTypeVersion.V2_0_0;
    private EventContent eventContent;

    public EmittedEventDataProductVersionPublishedRes() {
        this.eventContent = new EventContent();
    }

    public EmittedEventDataProductVersionPublishedRes(String resourceIdentifier, DataProductVersionRes dataProductVersion) {
        this.resourceIdentifier = resourceIdentifier;
        this.eventContent = new EventContent();
        this.eventContent.setDataProductVersion(dataProductVersion);
    }

    public ResourceType getResourceType() {
        return resourceType;
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

    public String getEventTypeVersion() {
        return eventTypeVersion.toString();
    }

    public EventContent getEventContent() {
        return eventContent;
    }

    public void setEventContent(EventContent eventContent) {
        this.eventContent = eventContent;
    }

    public static class EventContent {
        private DataProductVersionRes dataProductVersion;

        public EventContent() {
        }

        public DataProductVersionRes getDataProductVersion() {
            return dataProductVersion;
        }

        public void setDataProductVersion(DataProductVersionRes dataProductVersion) {
            this.dataProductVersion = dataProductVersion;
        }
    }
}

