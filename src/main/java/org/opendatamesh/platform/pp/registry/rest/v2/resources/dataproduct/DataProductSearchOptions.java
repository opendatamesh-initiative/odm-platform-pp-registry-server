package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

public class DataProductSearchOptions {

    @Parameter(
            description = "Filter data products by domain. Exact match (case-insensitive).",
            schema = @Schema(type = "string")
    )
    private String domain;

    @Parameter(
            description = "Filter data products by name. Exact match (case-insensitive).",
            schema = @Schema(type = "string")
    )
    private String name;

    @Parameter(
            description = "Filter data products by fully qualified name (FQN). Exact match (case-insensitive).",
            schema = @Schema(type = "string")
    )
    private String fqn;


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

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }
}
