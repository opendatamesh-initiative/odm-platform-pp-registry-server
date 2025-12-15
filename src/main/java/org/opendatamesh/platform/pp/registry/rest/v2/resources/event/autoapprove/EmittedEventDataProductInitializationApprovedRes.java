package org.opendatamesh.platform.pp.registry.rest.v2.resources.event.autoapprove;

import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeVersion;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.ResourceType;

// This event is emitted by the Registry service to automatically approve a data product initialization request
// when the Policy service is disabled (inactive) in the configuration.
// The Registry both emits and receives this event as ReceivedEventDataProductApprovedRes to complete the data product initialization approval process on its own.
public class EmittedEventDataProductInitializationApprovedRes {
    private final ResourceType resourceType = ResourceType.DATA_PRODUCT;
    private String resourceIdentifier;
    private final EventTypeRes type = EventTypeRes.DATA_PRODUCT_INITIALIZATION_APPROVED;
    private final EventTypeVersion eventTypeVersion = EventTypeVersion.V2_0_0;
    private EventContent eventContent;

    public EmittedEventDataProductInitializationApprovedRes() {
        this.eventContent = new EventContent();
    }

    public EmittedEventDataProductInitializationApprovedRes(String resourceIdentifier, String uuid, String fqn) {
        this.resourceIdentifier = resourceIdentifier;
        this.eventContent = new EventContent();
        DataProduct dataProduct = new DataProduct();
        dataProduct.setUuid(uuid);
        dataProduct.setFqn(fqn);
        this.eventContent.setDataProduct(dataProduct);
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
        private DataProduct dataProduct;

        public EventContent() {
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

