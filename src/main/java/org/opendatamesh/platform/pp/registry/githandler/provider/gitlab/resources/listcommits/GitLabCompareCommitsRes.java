package org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listcommits;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class GitLabCompareCommitsRes {
    private CompareCommitRes[] commits;
    @JsonProperty("web_url")
    private String web_url;

    public CompareCommitRes[] getCommits() {
        return commits;
    }

    public void setCommits(CompareCommitRes[] commits) {
        this.commits = commits;
    }

    public String getWeb_url() {
        return web_url;
    }

    public void setWeb_url(String web_url) {
        this.web_url = web_url;
    }

    public static class CompareCommitRes {
        private String id;
        private String message;
        @JsonProperty("author_name")
        private String author_name;
        @JsonProperty("author_email")
        private String author_email;
        @JsonProperty("authored_date")
        private Date authored_date;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getAuthorName() {
            return author_name;
        }

        public void setAuthorName(String author_name) {
            this.author_name = author_name;
        }

        public String getAuthorEmail() {
            return author_email;
        }

        public void setAuthorEmail(String author_email) {
            this.author_email = author_email;
        }

        public Date getAuthoredDate() {
            return authored_date;
        }

        public void setAuthoredDate(Date authored_date) {
            this.authored_date = authored_date;
        }
    }
}
