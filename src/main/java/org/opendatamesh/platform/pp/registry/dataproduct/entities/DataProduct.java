package org.opendatamesh.platform.pp.registry.dataproduct.entities;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.opendatamesh.platform.pp.registry.utils.entities.VersionedEntity;

import javax.persistence.*;


@Entity
@Table(name = "data_product")
public class DataProduct extends VersionedEntity {

    @Id
    @Column(name = "uuid")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
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

    public DataProductRepo getDataProductRepository() {
        return dataProductRepo;
    }

    public void setDataProductRepository(DataProductRepo dataProductRepo) {
        this.dataProductRepo = dataProductRepo;
    }
}
