package org.opendatamesh.platform.pp.registry.githandler.model;

import java.util.List;

/**
 * Represents a Git repository
 */
public class Repository {
    private String id;
    private String name;
    private String description;
    private String cloneUrlHttp;
    private String cloneUrlSsh;
    private String defaultBranch;
    private RepositoryOwnerType ownerType;
    private String ownerId;
    private RepositoryVisibility visibility;

    private List<ProviderCustomResourceProperty> providerCustomResourceProperties;

    public Repository() {
    }

    public Repository(String id, String name, String description, String cloneUrlHttp,
            String cloneUrlSsh, String defaultBranch, RepositoryOwnerType ownerType,
            String ownerId, RepositoryVisibility visibility) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cloneUrlHttp = cloneUrlHttp;
        this.cloneUrlSsh = cloneUrlSsh;
        this.defaultBranch = defaultBranch;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.visibility = visibility;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCloneUrlHttp() {
        return cloneUrlHttp;
    }

    public void setCloneUrlHttp(String cloneUrlHttp) {
        this.cloneUrlHttp = cloneUrlHttp;
    }

    public String getCloneUrlSsh() {
        return cloneUrlSsh;
    }

    public void setCloneUrlSsh(String cloneUrlSsh) {
        this.cloneUrlSsh = cloneUrlSsh;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    public RepositoryOwnerType getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(RepositoryOwnerType ownerType) {
        this.ownerType = ownerType;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public RepositoryVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(RepositoryVisibility visibility) {
        this.visibility = visibility;
    }

    public List<ProviderCustomResourceProperty> getProviderCustomResourceProperties() {
        return providerCustomResourceProperties;
    }

    public void setProviderCustomResourceProperties(List<ProviderCustomResourceProperty> providerCustomResourceProperties) {
        this.providerCustomResourceProperties = providerCustomResourceProperties;
    }

    @Override
    public String toString() {
        return "Repository{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", cloneUrlHttp='" + cloneUrlHttp + '\'' +
                ", cloneUrlSsh='" + cloneUrlSsh + '\'' +
                ", defaultBranch='" + defaultBranch + '\'' +
                ", ownerType=" + ownerType +
                ", ownerId='" + ownerId + '\'' +
                ", visibility=" + visibility +
                '}';
    }
}
