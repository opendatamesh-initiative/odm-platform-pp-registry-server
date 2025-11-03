package org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listcommits;

public class AzureListCommitsCommitRes {
    private String commitId;
    private String comment;
    private AzureListCommitsCommitAuthorRes author;

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public AzureListCommitsCommitAuthorRes getAuthor() {
        return author;
    }

    public void setAuthor(AzureListCommitsCommitAuthorRes author) {
        this.author = author;
    }
}

