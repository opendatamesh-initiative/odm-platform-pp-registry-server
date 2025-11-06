package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.getrepository;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BitbucketGetRepositoryRepositoryProjectRes {
    @JsonProperty("key")
    private String key;
    @JsonProperty("type")
    private String type;
    @JsonProperty("name")
    private String name;
    @JsonProperty("uuid")
    private String uuid;

    public BitbucketGetRepositoryRepositoryProjectRes() {
    }

    public BitbucketGetRepositoryRepositoryProjectRes(String key, String type, String name, String uuid) {
        this.key = key;
        this.type = type;
        this.name = name;
        this.uuid = uuid;
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
}

