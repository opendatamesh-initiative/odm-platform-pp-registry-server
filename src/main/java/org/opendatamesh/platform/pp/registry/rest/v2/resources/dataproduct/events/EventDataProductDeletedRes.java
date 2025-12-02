package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.events;

import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeVersion;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.ResourceType;

public class EventDataProductDeletedRes {
    private final ResourceType resourceType = ResourceType.DATA_PRODUCT;
    private String resourceIdentifier;
    private final EventTypeRes type = EventTypeRes.DATA_PRODUCT_DELETED;
    private final EventTypeVersion eventTypeVersion = EventTypeVersion.V2_0_0;
    private EventContent eventContent;

    public EventDataProductDeletedRes() {
        this.eventContent = new EventContent();
    }

    public EventDataProductDeletedRes(String resourceIdentifier, String dataProductUuid, String dataProductFqn) {
        this.resourceIdentifier = resourceIdentifier;
        this.eventContent = new EventContent();
        this.eventContent.setDataProductUuid(dataProductUuid);
        this.eventContent.setDataProductFqn(dataProductFqn);
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

    public EventTypeVersion getEventTypeVersion() {
        return eventTypeVersion;
    }

    public EventContent getEventContent() {
        return eventContent;
    }

    public void setEventContent(EventContent eventContent) {
        this.eventContent = eventContent;
    }

    public static class EventContent {
        private String dataProductUuid;
        private String dataProductFqn;

        public EventContent() {
        }

        public String getDataProductUuid() {
            return dataProductUuid;
        }

        public void setDataProductUuid(String dataProductUuid) {
            this.dataProductUuid = dataProductUuid;
        }

        public String getDataProductFqn() {
            return dataProductFqn;
        }

        public void setDataProductFqn(String dataProductFqn) {
            this.dataProductFqn = dataProductFqn;
        }
    }
}

