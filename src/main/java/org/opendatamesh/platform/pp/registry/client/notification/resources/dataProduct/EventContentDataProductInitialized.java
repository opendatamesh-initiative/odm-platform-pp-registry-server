package org.opendatamesh.platform.pp.registry.client.notification.resources.dataProduct;

import org.opendatamesh.platform.pp.registry.client.notification.resources.EventContent;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;

public class EventContentDataProductInitialized implements EventContent {
    private DataProductRes dataProduct;

    public EventContentDataProductInitialized(DataProductRes dataProduct) {
        this.dataProduct = dataProduct;
    }

    public DataProductRes getDataProduct() {
        return dataProduct;
    }

    public void setDataProduct(DataProductRes dataProduct) {
        this.dataProduct = dataProduct;
    }
}
