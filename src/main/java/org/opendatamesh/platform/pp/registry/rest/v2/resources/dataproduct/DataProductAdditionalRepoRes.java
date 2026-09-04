package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(name = "DataProductAdditionalRepoRes", description = "Additional keyed Git remote associated with a data product (non-root repository)")
public class DataProductAdditionalRepoRes {

    @Schema(description = "The unique identifier of the additional repository", example = "550e8400-e29b-41d4-a716-446655440000")
    private String uuid;

    @Schema(description = "The manifest key matching instantiation.repositories[].key", example = "infra-repo")
    private String manifestKey;

    @Schema(description = "The external identifier of the repository in the Git provider", example = "my-company/infra-repo")
    private String externalIdentifier;

    @Schema(description = "The name of the repository", example = "infra-repo")
    private String name;

    @Schema(description = "Optional description of the repository", example = "Infrastructure repository for the data product")
    private String description;

    @Schema(description = "The HTTP URL for cloning the repository", example = "https://github.com/my-company/infra-repo.git")
    private String remoteUrlHttp;

    @Schema(description = "The SSH URL for cloning the repository", example = "git@github.com:my-company/infra-repo.git")
    private String remoteUrlSsh;

    @Schema(description = "The default branch of the repository", example = "main")
    private String defaultBranch;

    @Schema(description = "The Git provider type hosting the repository", example = "GITHUB", allowableValues = {"AZURE", "BITBUCKET", "GITHUB", "GITLAB"})
    private DataProductRepoProviderTypeRes providerType;

    @Schema(description = "The base URL of the Git provider", example = "https://github.com")
    private String providerBaseUrl;

    @Schema(description = "The owner identifier of the repository in the Git provider", example = "my-company")
    private String ownerId;

    @Schema(description = "The owner type of the repository", example = "ORGANIZATION", allowableValues = {"ORGANIZATION", "ACCOUNT"})
    private DataProductRepoOwnerTypeRes ownerType;

    @Schema(description = "The UUID of the associated data product", example = "550e8400-e29b-41d4-a716-446655440001")
    private String dataProductUuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getManifestKey() {
        return manifestKey;
    }

    public void setManifestKey(String manifestKey) {
        this.manifestKey = manifestKey;
    }

    public String getExternalIdentifier() {
        return externalIdentifier;
    }

    public void setExternalIdentifier(String externalIdentifier) {
        this.externalIdentifier = externalIdentifier;
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

    public String getRemoteUrlHttp() {
        return remoteUrlHttp;
    }

    public void setRemoteUrlHttp(String remoteUrlHttp) {
        this.remoteUrlHttp = remoteUrlHttp;
    }

    public String getRemoteUrlSsh() {
        return remoteUrlSsh;
    }

    public void setRemoteUrlSsh(String remoteUrlSsh) {
        this.remoteUrlSsh = remoteUrlSsh;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    public DataProductRepoProviderTypeRes getProviderType() {
        return providerType;
    }

    public void setProviderType(DataProductRepoProviderTypeRes providerType) {
        this.providerType = providerType;
    }

    public String getProviderBaseUrl() {
        return providerBaseUrl;
    }

    public void setProviderBaseUrl(String providerBaseUrl) {
        this.providerBaseUrl = providerBaseUrl;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public DataProductRepoOwnerTypeRes getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(DataProductRepoOwnerTypeRes ownerType) {
        this.ownerType = ownerType;
    }

    public String getDataProductUuid() {
        return dataProductUuid;
    }

    public void setDataProductUuid(String dataProductUuid) {
        this.dataProductUuid = dataProductUuid;
    }
}
