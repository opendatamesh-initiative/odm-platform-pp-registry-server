package org.opendatamesh.platform.pp.registry.dataproduct.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.utils.resources.VersionedRes;


@Schema(name = "data_product_res")
public class DataProductRepositoryRes extends VersionedRes {

    @Schema(description = "The unique identifier of the data product repository")
    private String uuid;

    @Schema(description = "The fully qualified name of the data product")
    private String externalIdentifier;

    @Schema(description = "")
    private String name;

    @Schema(description = "")
    private String description;

    @Schema(description = "")
    private String descriptorRootPath;

    @Schema(description = "")
    private String remoteUrlHttp;

    @Schema(description = "")
    private String remoteUrlSsh;

    @Schema(description = "")
    private String defaultBranch;

    @Schema(description = "")
    private ProviderType providerType;

    @Schema(description = "")
    private String providerBaseUrl;

    @Schema(description = "")
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

    public ProviderType getProviderType() {
        return providerType;
    }

    public void setProviderType(ProviderType providerType) {
        this.providerType = providerType;
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
