package org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "providerIdentifier", description = "Provider identifier resource representing Git provider type and base URL")
public class ProviderIdentifierRes {

    @Schema(description = "Type of the Git provider (e.g., github, gitlab, bitbucket)", example = "github", required = true)
    private String providerType;

    @Schema(description = "Base URL of the Git provider (optional, defaults to provider-specific URL)", example = "https://api.github.com")
    private String providerBaseUrl;

    public ProviderIdentifierRes() {
    }

    public ProviderIdentifierRes(String providerType, String providerBaseUrl) {
        this.providerType = providerType;
        this.providerBaseUrl = providerBaseUrl;
    }

    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    public String getProviderBaseUrl() {
        return providerBaseUrl;
    }

    public void setProviderBaseUrl(String providerBaseUrl) {
        this.providerBaseUrl = providerBaseUrl;
    }
}
