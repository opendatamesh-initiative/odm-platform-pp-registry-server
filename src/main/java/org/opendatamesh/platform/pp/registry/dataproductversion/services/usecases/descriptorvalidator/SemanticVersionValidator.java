package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator;

import java.util.regex.Pattern;

class SemanticVersionValidator {
    private static final Pattern SEMVER_PATTERN = Pattern.compile(
            "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$"
    );

    boolean isValid(String version) {
        if (version == null || version.isEmpty()) return false;
        return SEMVER_PATTERN.matcher(version).matches();
    }
}
