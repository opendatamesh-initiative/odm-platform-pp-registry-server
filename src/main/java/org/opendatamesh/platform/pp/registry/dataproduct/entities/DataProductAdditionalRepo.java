package org.opendatamesh.platform.pp.registry.dataproduct.entities;

import jakarta.persistence.*;


@Entity
@Table(name = "data_products_additional_repositories")
public class DataProductAdditionalRepo {

    @Id
    @Column(name = "uuid")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uuid;

    @Column(name = "manifest_key", nullable = false)
    private String manifestKey;

    @Column(name = "external_identifier")
    private String externalIdentifier;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "remote_url_http")
    private String remoteUrlHttp;

    @Column(name = "remote_url_ssh")
    private String remoteUrlSsh;

    @Column(name = "default_branch")
    private String defaultBranch;

    @Column(name = "provider_type")
    @Enumerated(EnumType.STRING)
    private DataProductRepoProviderType providerType;

    @Column(name = "provider_base_url")
    private String providerBaseUrl;

    @Column(name = "owner_id")
    private String ownerId;

    @Column(name = "owner_type")
    @Enumerated(EnumType.STRING)
    private DataProductRepoOwnerType ownerType;

    @Column(name = "data_product_uuid", insertable = false, updatable = false)
    private String dataProductUuid;

    @ManyToOne
    @JoinColumn(name = "data_product_uuid", nullable = false)
    private DataProduct dataProduct;

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

    public DataProductRepoProviderType getProviderType() {
        return providerType;
    }

    public void setProviderType(DataProductRepoProviderType providerType) {
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

    public DataProductRepoOwnerType getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(DataProductRepoOwnerType ownerType) {
        this.ownerType = ownerType;
    }

    public String getDataProductUuid() {
        return dataProductUuid;
    }

    public void setDataProductUuid(String dataProductUuid) {
        this.dataProductUuid = dataProductUuid;
    }

    public DataProduct getDataProduct() {
        return dataProduct;
    }

    public void setDataProduct(DataProduct dataProduct) {
        this.dataProduct = dataProduct;
    }
}
