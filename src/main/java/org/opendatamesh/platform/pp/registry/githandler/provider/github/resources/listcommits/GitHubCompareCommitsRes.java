package org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listcommits;

public class GitHubCompareCommitsRes {
    private CompareCommitRes[] commits;
    private Integer total_commits;

    public void setCommits(CompareCommitRes[] commits) {
        this.commits = commits;
    }

    public CompareCommitRes[] getCommits() {
        return commits;
    }

    public Integer getTotal_commits() {
        return total_commits;
    }

    public void setTotal_commits(Integer total_commits) {
        this.total_commits = total_commits;
    }

    public static class CompareCommitRes {
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


}

