package org.opendatamesh.platform.pp.registry.old.v1.registryservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opendatamesh.dpds.model.DataProductVersionDPDS;

public class RegistryV1DataProductEventTypeResource {
    @JsonProperty("dataProductVersion")
    DataProductVersionDPDS dataProductVersion = null;

    public DataProductVersionDPDS getDataProductVersion() {
        return dataProductVersion;
    }

    public void setDataProductVersion(DataProductVersionDPDS dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }

}
