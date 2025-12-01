package org.opendatamesh.platform.pp.registry.client.notification.resources.dataProduct;

import org.opendatamesh.platform.pp.registry.client.notification.resources.EventContent;

public class EventContentDataProductDeleted implements EventContent {
    private String dataProductUuid;
    private String dataProductFqn;

    public EventContentDataProductDeleted(String dataProductUuid, String dataProductFqn) {
        this.dataProductUuid = dataProductUuid;
        this.dataProductFqn = dataProductFqn;
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
