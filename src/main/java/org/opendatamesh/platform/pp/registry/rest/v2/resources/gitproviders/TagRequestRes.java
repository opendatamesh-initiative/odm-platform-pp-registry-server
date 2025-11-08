package org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders;

public class TagRequestRes {

    private String tagName;

    private String message;

    private String target;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
