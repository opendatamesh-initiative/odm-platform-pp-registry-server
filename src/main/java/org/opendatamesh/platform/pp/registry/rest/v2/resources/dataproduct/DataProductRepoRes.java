package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(name = "DataProductRepoRes", description = "Data Product Repository resource representing the Git repository associated with a data product")
public class DataProductRepoRes {

    @Schema(description = "The unique identifier of the data product repository", example = "550e8400-e29b-41d4-a716-446655440000")
    private String uuid;

    @Schema(description = "The external identifier of the repository in the Git provider (e.g., repository name or ID)", example = "my-company/data-product-repo")
    private String externalIdentifier;

    @Schema(description = "The name of the repository", example = "customer-data-product")
    private String name;

    @Schema(description = "Optional description of the repository", example = "Repository containing customer data product definitions and schemas")
    private String description;

    @Schema(description = "The root path where the data product descriptor files are located in the repository", example = "/descriptors")
    private String descriptorRootPath;

    @Schema(description = "The HTTP URL for cloning the repository", example = "https://github.com/my-company/data-product-repo.git")
    private String remoteUrlHttp;

    @Schema(description = "The SSH URL for cloning the repository", example = "git@github.com:my-company/data-product-repo.git")
    private String remoteUrlSsh;

    @Schema(description = "The default branch of the repository", example = "main")
    private String defaultBranch;

    @Schema(description = "The Git provider type hosting the repository", example = "GITHUB", allowableValues = {"AZURE", "BITBUCKET", "GITHUB", "GITLAB"})
    private DataProductRepoProviderTypeRes dataProductRepoProviderType;

    @Schema(description = "The base URL of the Git provider", example = "https://github.com")
    private String providerBaseUrl;

    @Schema(description = "The UUID of the associated data product", example = "550e8400-e29b-41d4-a716-446655440001")
    private String dataProductUuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public String getDescriptorRootPath() {
        return descriptorRootPath;
    }

    public void setDescriptorRootPath(String descriptorRootPath) {
        this.descriptorRootPath = descriptorRootPath;
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
        return dataProductRepoProviderType;
    }

    public void setProviderType(DataProductRepoProviderTypeRes dataProductRepoProviderType) {
        this.dataProductRepoProviderType = dataProductRepoProviderType;
    }

    public String getProviderBaseUrl() {
        return providerBaseUrl;
    }

    public void setProviderBaseUrl(String providerBaseUrl) {
        this.providerBaseUrl = providerBaseUrl;
    }

    public String getDataProductUuid() {
        return dataProductUuid;
    }

    public void setDataProductUuid(String dataProductUuid) {
        this.dataProductUuid = dataProductUuid;
    }
}
