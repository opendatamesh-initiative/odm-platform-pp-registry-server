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

    @Parameter(
            description = "Extension filter: scope id (case-sensitive). Must be used together with extensionPropertyKey and extensionPropertyValue.",
            schema = @Schema(type = "string")
    )
    private String extensionPropertyScope;

    @Parameter(
            description = "Extension filter: property key under the scope (case-sensitive). Must be used together with extensionPropertyScope and extensionPropertyValue.",
            schema = @Schema(type = "string")
    )
    private String extensionPropertyKey;

    @Parameter(
            description = "Extension filter: scalar value as text; compared to PostgreSQL jsonb_extract_path_text of the stored JSON (exact match, v1 string-oriented semantics).",
            schema = @Schema(type = "string")
    )
    private String extensionPropertyValue;


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

    public String getExtensionPropertyScope() {
        return extensionPropertyScope;
    }

    public void setExtensionPropertyScope(String extensionPropertyScope) {
        this.extensionPropertyScope = extensionPropertyScope;
    }

    public String getExtensionPropertyKey() {
        return extensionPropertyKey;
    }

    public void setExtensionPropertyKey(String extensionPropertyKey) {
        this.extensionPropertyKey = extensionPropertyKey;
    }

    public String getExtensionPropertyValue() {
        return extensionPropertyValue;
    }

    public void setExtensionPropertyValue(String extensionPropertyValue) {
        this.extensionPropertyValue = extensionPropertyValue;
    }
}
