package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listprojects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BitbucketListProjectsProjectRes {
    @JsonProperty("key")
    private String key;
    @JsonProperty("type")
    private String type;
    @JsonProperty("name")
    private String name;
    @JsonProperty("uuid")
    private String uuid;
    @JsonProperty("links")
    private BitbucketListProjectsLinksRes links;

    public BitbucketListProjectsProjectRes() {
    }

    public BitbucketListProjectsProjectRes(String key, String type, String name, String uuid, BitbucketListProjectsLinksRes links) {
        this.key = key;
        this.type = type;
        this.name = name;
        this.uuid = uuid;
        this.links = links;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public BitbucketListProjectsLinksRes getLinks() {
        return links;
    }

    public void setLinks(BitbucketListProjectsLinksRes links) {
        this.links = links;
    }
}

