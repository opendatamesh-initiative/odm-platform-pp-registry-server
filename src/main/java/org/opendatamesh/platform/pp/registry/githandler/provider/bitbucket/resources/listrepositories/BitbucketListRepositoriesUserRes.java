package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listrepositories;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BitbucketListRepositoriesUserRes {
    @JsonProperty("uuid")
    private String uuid;
    @JsonProperty("username")
    private String username;
    @JsonProperty("account_id")
    private String accountId;
    @JsonProperty("nickname")
    private String nickname;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("links")
    private BitbucketListRepositoriesLinksRes links;

    public BitbucketListRepositoriesUserRes() {
    }

    public BitbucketListRepositoriesUserRes(String uuid, String username, String accountId, String nickname, 
                                 String displayName, BitbucketListRepositoriesLinksRes links) {
        this.uuid = uuid;
        this.username = username;
        this.accountId = accountId;
        this.nickname = nickname;
        this.displayName = displayName;
        this.links = links;
    }


    // Getters and setters
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public BitbucketListRepositoriesLinksRes getLinks() {
        return links;
    }

    public void setLinks(BitbucketListRepositoriesLinksRes links) {
        this.links = links;
    }
}

