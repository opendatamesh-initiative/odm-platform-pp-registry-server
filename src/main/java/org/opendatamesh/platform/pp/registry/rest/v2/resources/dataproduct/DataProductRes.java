package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opendatamesh.platform.pp.registry.utils.resources.VersionedRes;


@Schema(name = "data_product")
public class DataProductRes extends VersionedRes {

    @Schema(description = "The unique identifier of the data product")
    private String uuid;

    @Schema(description = "The fully qualified name of the data product")
    private String fqn;

    @Schema(description = "The domain related to the data product")
    private String domain;

    @Schema(description = "The name of the data product")
    private String name;

    @Schema(description = "The name used as display name of the data product")
    private String displayName;

    @Schema(description = "The description of the data product")
    private String description;

    private DataProductRepoRes dataProductRepoRes;

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

    public DataProductRepoRes getDataProductRepoRes() {
        return dataProductRepoRes;
    }

    public void setDataProductRepoRes(DataProductRepoRes dataProductRepoRes) {
        this.dataProductRepoRes = dataProductRepoRes;
    }
}
