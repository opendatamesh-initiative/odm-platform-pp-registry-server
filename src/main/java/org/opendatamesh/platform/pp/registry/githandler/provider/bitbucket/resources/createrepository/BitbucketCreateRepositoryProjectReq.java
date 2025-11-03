package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.createrepository;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Project object for Bitbucket repository creation request.
 * According to Bitbucket API, the project can be specified using either 'key' or 'uuid'.
 */
public class BitbucketCreateRepositoryProjectReq {
    @JsonProperty("key")
    private String key;

    @JsonProperty("uuid")
    private String uuid;

    public BitbucketCreateRepositoryProjectReq() {
    }

    public BitbucketCreateRepositoryProjectReq(String key) {
        this.key = key;
    }

    public BitbucketCreateRepositoryProjectReq(String key, String uuid) {
        this.key = key;
        this.uuid = uuid;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}

