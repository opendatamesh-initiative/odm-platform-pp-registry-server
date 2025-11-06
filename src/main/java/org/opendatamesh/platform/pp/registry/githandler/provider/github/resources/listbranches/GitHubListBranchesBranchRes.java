package org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listbranches;

public class GitHubListBranchesBranchRes {
    private String name;
    private GitHubListBranchesBranchCommit commit;
    private boolean isProtected;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GitHubListBranchesBranchCommit getCommit() {
        return commit;
    }

    public void setCommit(GitHubListBranchesBranchCommit commit) {
        this.commit = commit;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }
}

