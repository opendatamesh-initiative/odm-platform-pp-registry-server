package org.opendatamesh.platform.pp.registry.old.v1.policyservice;

import org.opendatamesh.platform.pp.registry.utils.client.RestUtils;

class PolicyClientV1Impl implements PolicyClientV1 {
    private final RestUtils restUtils;
    private final String policyServiceBaseUrl;

    PolicyClientV1Impl(RestUtils restUtils, String policyServiceBaseUrl) {
        this.restUtils = restUtils;
        this.policyServiceBaseUrl = policyServiceBaseUrl;
    }

    @Override
    public PolicyResValidationResponse validateInput(PolicyResPolicyEvaluationRequest evaluationRequest, boolean storeResults) {
        return restUtils.genericPost(policyServiceBaseUrl + "/api/v1/pp/policy/validation", null, evaluationRequest, PolicyResValidationResponse.class);
    }
}
