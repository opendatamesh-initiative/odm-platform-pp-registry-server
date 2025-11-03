package org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

public class RepositorySearchOptions {

    @Parameter(
            description = "User ID",
            schema = @Schema(type = "string", example = "12345678")
    )
    private String userId;

    @Parameter(
            description = "Username",
            schema = @Schema(type = "string", example = "johndoe")
    )
    private String username;

    @Parameter(
            description = "Organization ID (optional)",
            schema = @Schema(type = "string", example = "98765432")
    )
    private String organizationId;

    @Parameter(
            description = "Organization name (optional)",
            schema = @Schema(type = "string", example = "my-organization")
    )
    private String organizationName;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
}

