package org.opendatamesh.platform.pp.registry.rest.v2.resources.event.autoapprove;

import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeVersion;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.ResourceType;

// This event is emitted by the Registry service to automatically approve a data product version publication request
// when the Policy service is disabled (inactive) in the configuration.
// The Registry both emits and receives this event as ReceivedEventDataProductVersionApprovedRes to complete the data product version publication approval process on its own.
public class EmittedEventDataProductVersionPublicationApprovedRes {
    private final ResourceType resourceType = ResourceType.DATA_PRODUCT_VERSION;
    private String resourceIdentifier;
    private final EventTypeRes type = EventTypeRes.DATA_PRODUCT_VERSION_PUBLICATION_APPROVED;
    private final EventTypeVersion eventTypeVersion = EventTypeVersion.V2_0_0;
    private EventContent eventContent;

    public EmittedEventDataProductVersionPublicationApprovedRes() {
        this.eventContent = new EventContent();
    }

    public EmittedEventDataProductVersionPublicationApprovedRes(String resourceIdentifier, String uuid, String tag, String dataProductUuid, String dataProductFqn) {
        this.resourceIdentifier = resourceIdentifier;
        this.eventContent = new EventContent();
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid(uuid);
        dataProductVersion.setTag(tag);
        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid(dataProductUuid);
        dataProduct.setFqn(dataProductFqn);
        dataProductVersion.setDataProduct(dataProduct);
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
        private DataProductVersion dataProductVersion;

        public EventContent() {
        }

        public DataProductVersion getDataProductVersion() {
            return dataProductVersion;
        }

        public void setDataProductVersion(DataProductVersion dataProductVersion) {
            this.dataProductVersion = dataProductVersion;
        }
    }

    public static class DataProductVersion {
        private String uuid;
        private String tag;
        private DataProduct dataProduct;

        public DataProductVersion() {
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public DataProduct getDataProduct() {
            return dataProduct;
        }

        public void setDataProduct(DataProduct dataProduct) {
            this.dataProduct = dataProduct;
        }
    }

    public static class DataProduct {
        private String uuid;
        private String fqn;

        public DataProduct() {
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getFqn() {
            return fqn;
        }

        public void setFqn(String fqn) {
            this.fqn = fqn;
        }
    }
}

