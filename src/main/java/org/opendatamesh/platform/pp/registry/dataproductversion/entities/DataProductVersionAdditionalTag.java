package org.opendatamesh.platform.pp.registry.dataproductversion.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "data_products_versions_additional_tags")
public class DataProductVersionAdditionalTag {

    @Id
    @Column(name = "sequence_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sequenceId;

    @Column(name = "repository_key", nullable = false)
    private String repositoryKey;

    @Column(name = "tag", nullable = false)
    private String tag;

    @Column(name = "data_product_version_uuid", insertable = false, updatable = false)
    private String dataProductVersionUuid;

    @ManyToOne
    @JoinColumn(name = "data_product_version_uuid", nullable = false)
    private DataProductVersion dataProductVersion;

    public Long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public String getRepositoryKey() {
        return repositoryKey;
    }

    public void setRepositoryKey(String repositoryKey) {
        this.repositoryKey = repositoryKey;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDataProductVersionUuid() {
        return dataProductVersionUuid;
    }

    public void setDataProductVersionUuid(String dataProductVersionUuid) {
        this.dataProductVersionUuid = dataProductVersionUuid;
    }

    public DataProductVersion getDataProductVersion() {
        return dataProductVersion;
    }

    public void setDataProductVersion(DataProductVersion dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }
}
