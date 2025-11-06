package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listtags;

public class BitbucketListTagsTagRes {
    private String name;
    private String message;
    private BitbucketListTagsTagTargetRes target;
    private BitbucketListTagsTagLinksRes links;

    public BitbucketListTagsTagRes() {
    }

    public BitbucketListTagsTagRes(String name, String message, BitbucketListTagsTagTargetRes target, BitbucketListTagsTagLinksRes links) {
        this.name = name;
        this.message = message;
        this.target = target;
        this.links = links;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BitbucketListTagsTagTargetRes getTarget() {
        return target;
    }

    public void setTarget(BitbucketListTagsTagTargetRes target) {
        this.target = target;
    }

    public BitbucketListTagsTagLinksRes getLinks() {
        return links;
    }

    public void setLinks(BitbucketListTagsTagLinksRes links) {
        this.links = links;
    }
}

