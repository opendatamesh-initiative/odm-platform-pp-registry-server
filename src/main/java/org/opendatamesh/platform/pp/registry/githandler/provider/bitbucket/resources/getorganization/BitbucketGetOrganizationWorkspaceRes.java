package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.getorganization;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BitbucketGetOrganizationWorkspaceRes {
    @JsonProperty("uuid")
    private String uuid;
    @JsonProperty("name")
    private String name;
    @JsonProperty("links")
    private BitbucketGetOrganizationLinksRes links;

    public BitbucketGetOrganizationWorkspaceRes() {
    }

    public BitbucketGetOrganizationWorkspaceRes(String uuid, String name, BitbucketGetOrganizationLinksRes links) {
        this.uuid = uuid;
        this.name = name;
        this.links = links;
    }


    // Getters and setters
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BitbucketGetOrganizationLinksRes getLinks() {
        return links;
    }

    public void setLinks(BitbucketGetOrganizationLinksRes links) {
        this.links = links;
    }
}

