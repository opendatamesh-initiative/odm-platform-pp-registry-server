package org.opendatamesh.platform.pp.registry.dataproductversion.entities;

import jakarta.persistence.*;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.utils.entities.VersionedEntity;

@Entity
@Table(name = "data_products_versions")
public class DataProductVersionShort extends VersionedEntity {

    @Id
    @Column(name = "uuid")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uuid;

    @Column(name = "data_product_uuid", insertable = false, updatable = false)
    private String dataProductUuid;

    @ManyToOne
    @JoinColumn(name = "data_product_uuid", nullable = false)
    private DataProduct dataProduct;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "tag")
    private String tag;

    @Column(name = "validation_state")
    @Enumerated(EnumType.STRING)
    private DataProductVersionValidationState validationState;

    @Column(name = "descriptor_spec")
    private String spec;

    @Column(name = "descriptor_spec_version")
    private String specVersion;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public DataProductVersionValidationState getValidationState() {
        return validationState;
    }

    public void setValidationState(DataProductVersionValidationState validationState) {
        this.validationState = validationState;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getSpecVersion() {
        return specVersion;
    }

    public void setSpecVersion(String specVersion) {
        this.specVersion = specVersion;
    }
}
