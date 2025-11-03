package org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listorganizations;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GitHubListOrganizationsOrganizationRes {
    private long id;
    private String login;

    @JsonProperty("html_url")
    private String htmlUrl;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }
}

