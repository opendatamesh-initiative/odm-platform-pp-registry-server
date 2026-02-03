package org.opendatamesh.platform.pp.registry.old.v1.registryservice;

public class RegistryV1DataProductValidationResult {
    private boolean validated;
    private Object validationOutput;
    private Boolean blockingFlag;

    public RegistryV1DataProductValidationResult() {
    }

    public RegistryV1DataProductValidationResult(Boolean blockingFlag, Object validationOutput, boolean validated) {
        this.blockingFlag = blockingFlag;
        this.validationOutput = validationOutput;
        this.validated = validated;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public Object getValidationOutput() {
        return validationOutput;
    }

    public void setValidationOutput(Object validationOutput) {
        this.validationOutput = validationOutput;
    }

    public Boolean getBlockingFlag() {
        return blockingFlag;
    }

    public void setBlockingFlag(Boolean blockingFlag) {
        this.blockingFlag = blockingFlag;
    }
}
