package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listcommits;

import java.util.Date;

public class BitbucketListCommitsCommitRes {
    private String hash;
    private String message;
    private BitbucketListCommitsCommitAuthorRes author;
    private Date date;

    public BitbucketListCommitsCommitRes() {
    }

    public BitbucketListCommitsCommitRes(String hash, String message, BitbucketListCommitsCommitAuthorRes author, Date date) {
        this.hash = hash;
        this.message = message;
        this.author = author;
        this.date = date;
    }


    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BitbucketListCommitsCommitAuthorRes getAuthor() {
        return author;
    }

    public void setAuthor(BitbucketListCommitsCommitAuthorRes author) {
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

