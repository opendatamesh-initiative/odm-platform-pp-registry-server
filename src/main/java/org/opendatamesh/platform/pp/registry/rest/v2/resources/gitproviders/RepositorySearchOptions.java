package org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

public class RepositorySearchOptions {

    @Parameter(description = "Whether to show user repositories (true) or organization repositories (false)")
    private boolean showUserRepositories;

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

    public boolean isShowUserRepositories() {
        return showUserRepositories;
    }

    public void setShowUserRepositories(boolean showUserRepositories) {
        this.showUserRepositories = showUserRepositories;
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

