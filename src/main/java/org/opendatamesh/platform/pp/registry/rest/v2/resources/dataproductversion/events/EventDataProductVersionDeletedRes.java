package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.events;

import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeVersion;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.ResourceType;

public class EventDataProductVersionDeletedRes {
    private final ResourceType resourceType = ResourceType.DATA_PRODUCT_VERSION;
    private String resourceIdentifier;
    private final EventTypeRes type = EventTypeRes.DATA_PRODUCT_VERSION_DELETED;
    private final EventTypeVersion eventTypeVersion = EventTypeVersion.V2_0_0;
    private EventContent eventContent;

    public EventDataProductVersionDeletedRes() {
        this.eventContent = new EventContent();
    }

    public EventDataProductVersionDeletedRes(String resourceIdentifier, String dataProductVersionUuid, String dataProductFqn, String dataProductVersionTag) {
        this.resourceIdentifier = resourceIdentifier;
        this.eventContent = new EventContent();
        this.eventContent.setDataProductVersionUuid(dataProductVersionUuid);
        this.eventContent.setDataProductFqn(dataProductFqn);
        this.eventContent.setDataProductVersionTag(dataProductVersionTag);
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
        private String dataProductVersionUuid;
        private String dataProductFqn;
        private String dataProductVersionTag;

        public EventContent() {
        }

        public String getDataProductVersionUuid() {
            return dataProductVersionUuid;
        }

        public void setDataProductVersionUuid(String dataProductVersionUuid) {
            this.dataProductVersionUuid = dataProductVersionUuid;
        }

        public String getDataProductFqn() {
            return dataProductFqn;
        }

        public void setDataProductFqn(String dataProductFqn) {
            this.dataProductFqn = dataProductFqn;
        }

        public String getDataProductVersionTag() {
            return dataProductVersionTag;
        }

        public void setDataProductVersionTag(String dataProductVersionTag) {
            this.dataProductVersionTag = dataProductVersionTag;
        }
    }
}

