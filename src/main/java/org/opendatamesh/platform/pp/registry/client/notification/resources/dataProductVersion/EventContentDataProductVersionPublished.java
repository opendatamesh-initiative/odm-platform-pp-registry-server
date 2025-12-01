package org.opendatamesh.platform.pp.registry.client.notification.resources.dataProductVersion;

import org.opendatamesh.platform.pp.registry.client.notification.resources.EventContent;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;

public class EventContentDataProductVersionPublished implements EventContent {
    private DataProductVersionRes dataProductVersion;

    public EventContentDataProductVersionPublished(DataProductVersionRes dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }

    public DataProductVersionRes getDataProductVersion() {
        return dataProductVersion;
    }
    
    public void setDataProductVersion(DataProductVersionRes dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }
}
