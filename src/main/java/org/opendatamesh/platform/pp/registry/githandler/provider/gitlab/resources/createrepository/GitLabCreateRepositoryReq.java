package org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.createrepository;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GitLabCreateRepositoryReq {
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("visibility")
    private String visibility;

    @JsonProperty("namespace_id")
    private String namespaceId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
}

