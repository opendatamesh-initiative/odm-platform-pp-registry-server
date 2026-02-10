package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator;

class DpdsDescriptorValidationErrorMessage {
    private final String fieldPath;
    private final String message;

    DpdsDescriptorValidationErrorMessage(String fieldPath, String message) {
        this.fieldPath = fieldPath;
        this.message = message;
    }

    String getFieldPath() {
        return fieldPath;
    }

    String getMessage() {
        return message;
    }

    String format() {
        if (fieldPath != null && !fieldPath.isEmpty()) {
            return String.format("%s: %s", fieldPath, message);
        }
        return message;
    }
}
