package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listorganizations;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BitbucketListOrganizationsWorkspaceRes {
    @JsonProperty("uuid")
    private String uuid;
    @JsonProperty("name")
    private String name;
    @JsonProperty("links")
    private BitbucketListOrganizationsLinksRes links;

    public BitbucketListOrganizationsWorkspaceRes() {
    }

    public BitbucketListOrganizationsWorkspaceRes(String uuid, String name, BitbucketListOrganizationsLinksRes links) {
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

    public BitbucketListOrganizationsLinksRes getLinks() {
        return links;
    }

    public void setLinks(BitbucketListOrganizationsLinksRes links) {
        this.links = links;
    }
}

