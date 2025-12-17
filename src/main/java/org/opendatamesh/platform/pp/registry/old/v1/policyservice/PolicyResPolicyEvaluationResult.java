package org.opendatamesh.platform.pp.registry.old.v1.policyservice;

import com.fasterxml.jackson.databind.JsonNode;

public class PolicyResPolicyEvaluationResult {
    private Long id;
    private String dataProductId;
    private String dataProductVersion;
    private JsonNode inputObject;
    private String outputObject;
    private Boolean result;
    private Long policyId;
    private PolicyResPolicy policy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDataProductId() {
        return dataProductId;
    }

    public void setDataProductId(String dataProductId) {
        this.dataProductId = dataProductId;
    }

    public String getDataProductVersion() {
        return dataProductVersion;
    }

    public void setDataProductVersion(String dataProductVersion) {
        this.dataProductVersion = dataProductVersion;
    }

    public JsonNode getInputObject() {
        return inputObject;
    }

    public void setInputObject(JsonNode inputObject) {
        this.inputObject = inputObject;
    }

    public String getOutputObject() {
        return outputObject;
    }

    public void setOutputObject(String outputObject) {
        this.outputObject = outputObject;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public PolicyResPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(PolicyResPolicy policy) {
        this.policy = policy;
    }
}
