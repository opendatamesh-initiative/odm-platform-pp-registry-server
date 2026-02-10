package org.opendatamesh.platform.pp.registry.old.v1.policyservice;

public interface PolicyClientV1 {
    PolicyResValidationResponse validateInput(PolicyResPolicyEvaluationRequest evaluationRequest, boolean storeResults);
}
