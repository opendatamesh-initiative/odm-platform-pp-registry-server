package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DataProductVersionAdditionalTagRes", description = "Git snapshot tag for an additional (non-root) data product repository. Value object: identified by repositoryKey on the parent version; no independent id.")
public class DataProductVersionAdditionalTagRes {

    @Schema(description = "Key of the additional repository this tag belongs to", example = "infra-repo")
    private String repositoryKey;

    @Schema(description = "The Git tag name on that additional repository", example = "1.0.0")
    private String tag;

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
}
