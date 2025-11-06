package org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listtags;

public class GitHubListTagsTagRes {
    private String name;
    private GitHubListTagsTagCommit commit;
    private String zipball_url;
    private String tarball_url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GitHubListTagsTagCommit getCommit() {
        return commit;
    }

    public void setCommit(GitHubListTagsTagCommit commit) {
        this.commit = commit;
    }

    public String getZipballUrl() {
        return zipball_url;
    }

    public void setZipballUrl(String zipball_url) {
        this.zipball_url = zipball_url;
    }

    public String getTarballUrl() {
        return tarball_url;
    }

    public void setTarballUrl(String tarball_url) {
        this.tarball_url = tarball_url;
    }
}

