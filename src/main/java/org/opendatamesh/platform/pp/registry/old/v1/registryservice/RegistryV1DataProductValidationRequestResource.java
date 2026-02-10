package org.opendatamesh.platform.pp.registry.old.v1.registryservice;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class RegistryV1DataProductValidationRequestResource {
    private Boolean validateSyntax = true;
    private Boolean validatePolicies = true;
    private List<String> policyEventTypes = new ArrayList<>();
    private JsonNode dataProductVersion;

    public RegistryV1DataProductValidationRequestResource() {
    }

    public Boolean getValidateSyntax() {
        return validateSyntax;
    }

    public void setValidateSyntax(Boolean validateSyntax) {
        this.validateSyntax = validateSyntax;
    }

    public Boolean getValidatePolicies() {
        return validatePolicies;
    }

    public void setValidatePolicies(Boolean validatePolicies) {
        this.validatePolicies = validatePolicies;
    }

    public List<String> getPolicyEventTypes() {
        return policyEventTypes;
    }

    public void setPolicyEventTypes(List<String> policyEventTypes) {
        this.policyEventTypes = policyEventTypes != null ? policyEventTypes : new ArrayList<>();
    }

    public JsonNode getDataProductVersion() {
        return dataProductVersion;
    }

    public void setDataProductVersion(JsonNode dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }
}
