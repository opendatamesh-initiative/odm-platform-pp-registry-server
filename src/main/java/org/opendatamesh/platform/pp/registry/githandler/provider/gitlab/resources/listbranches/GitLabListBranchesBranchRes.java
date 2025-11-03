package org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listbranches;

public class GitLabListBranchesBranchRes {
    private String name;
    private GitLabListBranchesBranchCommit commit;
    private boolean isProtected;
    private boolean isDefault;
    private String web_url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GitLabListBranchesBranchCommit getCommit() {
        return commit;
    }

    public void setCommit(GitLabListBranchesBranchCommit commit) {
        this.commit = commit;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getWebUrl() {
        return web_url;
    }

    public void setWebUrl(String web_url) {
        this.web_url = web_url;
    }
}

