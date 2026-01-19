package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import java.util.regex.Pattern;

class SemanticVersionValidator {
    // Semantic Versioning 2.0.0 regex pattern
    // Matches: MAJOR.MINOR.PATCH[-PRERELEASE][+BUILD]
    // Where MAJOR, MINOR, PATCH are non-negative integers, no leading zeros
    private static final Pattern SEMVER_PATTERN = Pattern.compile(
            "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$"
    );

    boolean isValid(String version) {
        if (version == null || version.isEmpty()) {
            return false;
        }
        return SEMVER_PATTERN.matcher(version).matches();
    }

    void validate(String version, String fieldPath, DpdsDescriptorValidationContext context) {
        if (version == null || version.isEmpty()) {
            context.addError(fieldPath, "Version field is required");
        } else if (!isValid(version)) {
            context.addError(fieldPath, String.format("Version '%s' does not follow semantic versioning specification (MAJOR.MINOR.PATCH[-PRERELEASE][+BUILD])", version));
        }
    }
}
