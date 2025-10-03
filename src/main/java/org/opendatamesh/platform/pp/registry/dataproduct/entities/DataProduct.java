package org.opendatamesh.platform.pp.registry.dataproduct.entities;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.opendatamesh.platform.pp.registry.utils.entities.VersionedEntity;

import jakarta.persistence.*;


@Entity
@Table(name = "data_products")
public class DataProduct extends VersionedEntity {

    @Id
    @Column(name = "uuid")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uuid;

    @Column(name = "fqn")
    private String fqn;

    @Column(name = "domain")
    private String domain;

    @Column(name = "name")
    private String name;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "description")
    private String description;

    @Column(name = "validation_state")
    @Enumerated(EnumType.STRING)
    private DataProductValidationState validationState;

    @OneToOne(mappedBy = "dataProduct", orphanRemoval = true, cascade = CascadeType.ALL)
    @Fetch(FetchMode.SELECT)
    private DataProductRepo dataProductRepo;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DataProductValidationState getValidationState() {
        return validationState;
    }

    public void setValidationState(DataProductValidationState validationState) {
        this.validationState = validationState;
    }

    public DataProductRepo getDataProductRepo() {
        return dataProductRepo;
    }

    public void setDataProductRepo(DataProductRepo dataProductRepo) {
        this.dataProductRepo = dataProductRepo;
    }
}
