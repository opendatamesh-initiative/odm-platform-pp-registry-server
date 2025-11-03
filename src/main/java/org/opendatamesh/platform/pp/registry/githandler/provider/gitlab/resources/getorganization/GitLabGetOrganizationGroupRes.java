package org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.getorganization;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GitLabGetOrganizationGroupRes {
    private long id;
    private String name;
    @JsonProperty("web_url")
    private String webUrl;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }
}

