package org.opendatamesh.platform.pp.registry.old.v1.policyservice;

interface PolicyClientV1 {
    PolicyResValidationResponse validateInput(PolicyResPolicyEvaluationRequest evaluationRequest, boolean storeResults);
}
