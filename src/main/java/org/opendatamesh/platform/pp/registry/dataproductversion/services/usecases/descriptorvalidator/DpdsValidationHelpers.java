package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator;

import org.springframework.util.StringUtils;

final class DpdsValidationHelpers {

    private static final SemanticVersionValidator SEMVER = new SemanticVersionValidator();

    private DpdsValidationHelpers() {
    }

    static void validateRequiredStringField(String value, String fieldPath, DpdsDescriptorValidationContext context) {
        if (!StringUtils.hasText(value)) {
            context.addError(fieldPath, "Required field is missing or empty");
        }
    }

    static void validateSemanticVersionIfPresent(String version, String fieldPath, DpdsDescriptorValidationContext context) {
        if (StringUtils.hasText(version) && !SEMVER.isValid(version)) {
            context.addError(fieldPath, String.format("Version '%s' does not follow semantic versioning specification (MAJOR.MINOR.PATCH[-PRERELEASE][+BUILD])", version));
        }
    }

    static String getPortEntityType(String portType) {
        if (portType == null) return null;
        return switch (portType) {
            case "inputPort" -> "inputport";
            case "outputPort" -> "outputport";
            case "discoveryPort" -> "discoveryport";
            case "observabilityPort" -> "observabilityport";
            case "controlPort" -> "controlport";
            default -> null;
        };
    }
}
