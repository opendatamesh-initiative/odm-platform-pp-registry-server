package org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listcommits;

public class GitHubListCommitsCommit {
    private GitHubListCommitsCommitAuthor author;
    private GitHubListCommitsCommitAuthor committer;
    private String message;

    public GitHubListCommitsCommitAuthor getAuthor() {
        return author;
    }

    public void setAuthor(GitHubListCommitsCommitAuthor author) {
        this.author = author;
    }

    public GitHubListCommitsCommitAuthor getCommitter() {
        return committer;
    }

    public void setCommitter(GitHubListCommitsCommitAuthor committer) {
        this.committer = committer;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

