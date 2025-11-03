package org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.checkconnection;

public class AzureCheckConnectionUserRes {
    private String id;
    private String subjectDescriptor;
    private String displayName;
    private String imageUrl;
    private String url;
    private String providerDisplayName;
    private String descriptor;
    private AzureCheckConnectionUserPropertiesRes properties;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubjectDescriptor() {
        return subjectDescriptor;
    }

    public void setSubjectDescriptor(String subjectDescriptor) {
        this.subjectDescriptor = subjectDescriptor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProviderDisplayName() {
        return providerDisplayName;
    }

    public void setProviderDisplayName(String providerDisplayName) {
        this.providerDisplayName = providerDisplayName;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public AzureCheckConnectionUserPropertiesRes getProperties() {
        return properties;
    }

    public void setProperties(AzureCheckConnectionUserPropertiesRes properties) {
        this.properties = properties;
    }
}

