package org.opendatamesh.platform.pp.registry.githandler.model;

import java.time.OffsetDateTime;

public class TagRequest {

    private String tagName;           // Nome del tag, es: v1.0.0
    private String type;              // Obbligatorio per bitbucket (String payload.put("type", "<string>");)
    private String target;            // Commit SHA o branch da taggare
    private String message;           // Messaggio del tag (annotated)
    private String description;       // Descrizione release (GitLab)
    private String taggerName;        // Solo GitHub annotated
    private String taggerEmail;       // Solo GitHub annotated
    private OffsetDateTime taggerDate;// Solo GitHub annotated

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTaggerName() {
        return taggerName;
    }

    public void setTaggerName(String taggerName) {
        this.taggerName = taggerName;
    }

    public String getTaggerEmail() {
        return taggerEmail;
    }

    public void setTaggerEmail(String taggerEmail) {
        this.taggerEmail = taggerEmail;
    }

    public OffsetDateTime getTaggerDate() {
        return taggerDate;
    }

    public void setTaggerDate(OffsetDateTime taggerDate) {
        this.taggerDate = taggerDate;
    }
}
