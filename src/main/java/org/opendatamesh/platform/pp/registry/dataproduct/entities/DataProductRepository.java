package org.opendatamesh.platform.pp.registry.dataproduct.entities;

import org.hibernate.annotations.GenericGenerator;
import org.opendatamesh.platform.pp.registry.dataproduct.resources.ProviderType;
import org.opendatamesh.platform.pp.registry.utils.entities.VersionedEntity;

import javax.persistence.*;


@Entity
@Table(name = "data_product_repository")
public class DataProductRepository extends VersionedEntity {

    @Id
    @Column(name = "uuid")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String uuid;

    @Column(name = "external_identifier")
    private String externalIdentifier;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "descriptor_root_path")
    private String descriptorRootPath;

    @Column(name = "remote_url_http")
    private String remoteUrlHttp;

    @Column(name = "remote_url_ssh")
    private String remoteUrlSsh;

    @Column(name = "default_branch")
    private String defaultBranch;

    @Column(name = "provider_type")
    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    @Column(name = "provider_base_url")
    private String providerBaseUrl;

    @Column(name = "data_product_uuid", insertable = false, updatable = false)
    private String dataProductUuid;

    @OneToOne
    @JoinColumn(name = "data_product_uuid", nullable = false, unique = true)
    private DataProduct dataProduct;

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

    public DataProduct getDataProduct() {
        return dataProduct;
    }

    public void setDataProduct(DataProduct dataProduct) {
        this.dataProduct = dataProduct;
    }
}
