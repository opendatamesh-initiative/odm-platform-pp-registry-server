package org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.githandler.model.OwnerType;
import org.opendatamesh.platform.pp.registry.githandler.model.Visibility;

import java.util.List;

@Schema(name = "repository", description = "Repository resource representing a Git repository from a Git provider")
public class RepositoryRes {

    @Schema(description = "The unique identifier of the repository", example = "123456789")
    private String id;

    @Schema(description = "The name of the repository", example = "my-repository")
    private String name;

    @Schema(description = "The description of the repository", example = "A sample repository for demonstration")
    private String description;

    @Schema(description = "The HTTP clone URL of the repository", example = "https://github.com/my-company/my-repository.git")
    private String cloneUrlHttp;

    @Schema(description = "The SSH clone URL of the repository", example = "git@github.com:my-company/my-repository.git")
    private String cloneUrlSsh;

    @Schema(description = "The default branch of the repository", example = "main")
    private String defaultBranch;

    @Schema(description = "The type of the repository owner", example = "ORGANIZATION")
    private OwnerType ownerType;

    @Schema(description = "The ID of the repository owner", example = "12345678")
    private String ownerId;

    @Schema(description = "The visibility of the repository", example = "PRIVATE")
    private Visibility visibility;

    @Schema(description = "Additional provider-specific properties", example = "[{\"name\": \"full_name\", \"value\": \"owner/repo\"}]")
    private List<ProviderAdditionalPropertyRes> additionalProviderProperties;

    public RepositoryRes() {
    }

    public RepositoryRes(String id, String name, String description, String cloneUrlHttp, 
                        String cloneUrlSsh, String defaultBranch, OwnerType ownerType, 
                        String ownerId, Visibility visibility) {
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

    public OwnerType getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(OwnerType ownerType) {
        this.ownerType = ownerType;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public List<ProviderAdditionalPropertyRes> getAdditionalProviderProperties() {
        return additionalProviderProperties;
    }

    public void setAdditionalProviderProperties(List<ProviderAdditionalPropertyRes> additionalProviderProperties) {
        this.additionalProviderProperties = additionalProviderProperties;
    }

}
