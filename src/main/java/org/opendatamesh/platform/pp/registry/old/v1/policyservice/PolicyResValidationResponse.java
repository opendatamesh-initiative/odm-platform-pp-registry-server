package org.opendatamesh.platform.pp.registry.old.v1.policyservice;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PolicyResValidationResponse {

    @JsonProperty("result")
    private Boolean result;

    @JsonProperty("policyResults")
    private List<PolicyResPolicyEvaluationResult> policyResults;

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public List<PolicyResPolicyEvaluationResult> getPolicyResults() {
        return policyResults;
    }

    public void setPolicyResults(List<PolicyResPolicyEvaluationResult> policyResults) {
        this.policyResults = policyResults;
    }

}
