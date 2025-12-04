package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.events;

import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.EventTypeVersion;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.event.ResourceType;

public class EventDataProductInitializationApprovedRes {
    private final ResourceType resourceType = ResourceType.DATA_PRODUCT;
    private String resourceIdentifier;
    private final EventTypeRes type = EventTypeRes.DATA_PRODUCT_INITIALIZATION_APPROVED;
    private final EventTypeVersion eventTypeVersion = EventTypeVersion.V2_0_0;
    private EventContent eventContent;

    public EventDataProductInitializationApprovedRes() {
        this.eventContent = new EventContent();
    }

    public EventDataProductInitializationApprovedRes(String resourceIdentifier, DataProductRes dataProduct) {
        this.resourceIdentifier = resourceIdentifier;
        this.eventContent = new EventContent();
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
        private DataProductRes dataProduct;

        public EventContent() {
        }

        public DataProductRes getDataProduct() {
            return dataProduct;
        }

        public void setDataProduct(DataProductRes dataProduct) {
            this.dataProduct = dataProduct;
        }
    }
}

