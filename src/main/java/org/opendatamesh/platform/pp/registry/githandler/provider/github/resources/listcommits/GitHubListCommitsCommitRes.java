package org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listcommits;

public class GitHubListCommitsCommitRes {
    private String sha;
    private GitHubListCommitsCommit commit;
    private String url;

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public GitHubListCommitsCommit getCommit() {
        return commit;
    }

    public void setCommit(GitHubListCommitsCommit commit) {
        this.commit = commit;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

