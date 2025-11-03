package org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.createrepository;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AzureCreateRepositoryReq {
    @JsonProperty("name")
    private String name;

    @JsonProperty("project")
    private AzureCreateRepositoryProjectReferenceReq project;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AzureCreateRepositoryProjectReferenceReq getProject() {
        return project;
    }

    public void setProject(AzureCreateRepositoryProjectReferenceReq project) {
        this.project = project;
    }
}

