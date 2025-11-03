package org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.createrepository;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AzureCreateRepositoryProjectReferenceReq {
    @JsonProperty("id")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

