package org.opendatamesh.platform.pp.registry.descriptorvariable.entities;

import jakarta.persistence.*;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;

@Entity
@Table(name = "data_products_descriptor_variables")
public class DescriptorVariable {

    @Id
    @Column(name = "sequence_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sequenceId;

    @Column(name = "data_product_version_uuid", insertable = false, updatable = false)
    private String dataProductVersionUuid;

    @ManyToOne
    @JoinColumn(name = "data_product_version_uuid", nullable = false)
    private DataProductVersion dataProductVersion;

    @Column(name = "variable_key", nullable = false)
    private String variableKey;

    @Column(name = "variable_value")
    private String variableValue;

    public Long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
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

    public String getVariableKey() {
        return variableKey;
    }

    public void setVariableKey(String variableKey) {
        this.variableKey = variableKey;
    }

    public String getVariableValue() {
        return variableValue;
    }

    public void setVariableValue(String variableValue) {
        this.variableValue = variableValue;
    }
}
