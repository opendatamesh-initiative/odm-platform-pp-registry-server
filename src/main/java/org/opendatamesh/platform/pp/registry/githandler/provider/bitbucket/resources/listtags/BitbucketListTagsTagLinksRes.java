package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listtags;

public class BitbucketListTagsTagLinksRes {
    private BitbucketListTagsLinkRes html;

    public BitbucketListTagsTagLinksRes() {
    }

    public BitbucketListTagsTagLinksRes(BitbucketListTagsLinkRes html) {
        this.html = html;
    }

    public BitbucketListTagsLinkRes getHtml() {
        return html;
    }

    public void setHtml(BitbucketListTagsLinkRes html) {
        this.html = html;
    }
}

