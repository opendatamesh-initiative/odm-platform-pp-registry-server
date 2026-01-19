package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

class DpdsDescriptorValidationError {
    private final String fieldPath;
    private final String message;

    DpdsDescriptorValidationError(String fieldPath, String message) {
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
