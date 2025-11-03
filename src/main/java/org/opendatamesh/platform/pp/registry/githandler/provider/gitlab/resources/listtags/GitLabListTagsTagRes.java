package org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listtags;

public class GitLabListTagsTagRes {
    private String name;
    private String message;
    private GitLabListTagsTagCommit commit;
    private String target;
    private String web_url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public GitLabListTagsTagCommit getCommit() {
        return commit;
    }

    public void setCommit(GitLabListTagsTagCommit commit) {
        this.commit = commit;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getWebUrl() {
        return web_url;
    }

    public void setWebUrl(String web_url) {
        this.web_url = web_url;
    }
}

