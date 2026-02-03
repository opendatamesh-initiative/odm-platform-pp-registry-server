package org.opendatamesh.platform.pp.registry.old.v1.registryservice;

import java.util.HashMap;
import java.util.Map;

public class RegistryV1DataProductValidationResponseResource {
    private RegistryV1DataProductValidationResult syntaxValidationResult;
    private Map<String, RegistryV1DataProductValidationResult> policiesValidationResults = new HashMap<>();

    public RegistryV1DataProductValidationResponseResource() {
    }

    public RegistryV1DataProductValidationResult getSyntaxValidationResult() {
        return syntaxValidationResult;
    }

    public void setSyntaxValidationResult(RegistryV1DataProductValidationResult syntaxValidationResult) {
        this.syntaxValidationResult = syntaxValidationResult;
    }

    public Map<String, RegistryV1DataProductValidationResult> getPoliciesValidationResults() {
        return policiesValidationResults;
    }

    public void setPoliciesValidationResults(Map<String, RegistryV1DataProductValidationResult> policiesValidationResults) {
        this.policiesValidationResults = policiesValidationResults != null ? policiesValidationResults : new HashMap<>();
    }
}
