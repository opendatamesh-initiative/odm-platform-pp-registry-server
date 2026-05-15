package org.opendatamesh.platform.pp.registry.dataproduct.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * Validates client-supplied extension properties: an object whose keys are scope identifiers and
 * whose values are objects mapping property keys to JSON values. Does not interpret blueprint or
 * lineage semantics.
 */
@Component
public class DataProductExtensionPropertiesValidator {

    private static final Logger log = LoggerFactory.getLogger(DataProductExtensionPropertiesValidator.class);

    static final int MAX_SCOPE_ID_LENGTH = 255;
    static final int MAX_INNER_KEY_LENGTH = 255;
    static final int MAX_SCOPES = 200;
    static final int MAX_KEYS_PER_SCOPE = 500;
    static final int MAX_DOCUMENT_BYTES = 256_000;

    public void validateOrThrow(JsonNode root) {
        if (root == null || root.isNull() || root.isMissingNode()) {
            return;
        }
        if (!root.isObject()) {
            log.info("EXTENSION_PROPERTIES_VALIDATION_FAILED reason=not_object");
            throw new BadRequestException("extensionProperties must be a JSON object when provided");
        }
        enforceDocumentSize(root);
        int scopeCount = 0;
        for (Iterator<String> fn = root.fieldNames(); fn.hasNext(); ) {
            String scopeId = fn.next();
            if (++scopeCount > MAX_SCOPES) {
                log.info("EXTENSION_PROPERTIES_VALIDATION_FAILED reason=too_many_scopes");
                throw new BadRequestException("extensionProperties exceeds maximum number of scopes");
            }
            if (!StringUtils.hasText(scopeId) || scopeId.isBlank()) {
                log.info("EXTENSION_PROPERTIES_VALIDATION_FAILED reason=blank_scope");
                throw new BadRequestException("extensionProperties scope identifiers must be non-blank strings");
            }
            if (scopeId.length() > MAX_SCOPE_ID_LENGTH) {
                log.info("EXTENSION_PROPERTIES_VALIDATION_FAILED reason=scope_too_long");
                throw new BadRequestException("extensionProperties scope identifier cannot exceed " + MAX_SCOPE_ID_LENGTH + " characters");
            }
            JsonNode scopeValue = root.get(scopeId);
            if (scopeValue == null || !scopeValue.isObject()) {
                log.info("EXTENSION_PROPERTIES_VALIDATION_FAILED reason=scope_value_not_object");
                throw new BadRequestException("extensionProperties values must be JSON objects keyed by property name");
            }
            int keyCount = 0;
            for (Iterator<String> innerFn = scopeValue.fieldNames(); innerFn.hasNext(); ) {
                String innerKey = innerFn.next();
                if (++keyCount > MAX_KEYS_PER_SCOPE) {
                    log.info("EXTENSION_PROPERTIES_VALIDATION_FAILED reason=too_many_keys_in_scope");
                    throw new BadRequestException("extensionProperties exceeds maximum keys per scope");
                }
                if (!StringUtils.hasText(innerKey) || innerKey.isBlank()) {
                    log.info("EXTENSION_PROPERTIES_VALIDATION_FAILED reason=blank_inner_key");
                    throw new BadRequestException("extensionProperties property keys must be non-blank strings");
                }
                if (innerKey.length() > MAX_INNER_KEY_LENGTH) {
                    log.info("EXTENSION_PROPERTIES_VALIDATION_FAILED reason=inner_key_too_long");
                    throw new BadRequestException("extensionProperties property key cannot exceed " + MAX_INNER_KEY_LENGTH + " characters");
                }
                JsonNode v = scopeValue.get(innerKey);
                if (v == null || v.isNull() || v.isMissingNode()) {
                    continue;
                }
                if (!isAllowedJsonValue(v)) {
                    log.info("EXTENSION_PROPERTIES_VALIDATION_FAILED reason=disallowed_value_type");
                    throw new BadRequestException("extensionProperties values must use supported JSON types only");
                }
            }
        }
    }

    private static void enforceDocumentSize(JsonNode root) {
        int bytes = root.toString().getBytes(StandardCharsets.UTF_8).length;
        if (bytes > MAX_DOCUMENT_BYTES) {
            throw new BadRequestException("extensionProperties document exceeds maximum allowed size");
        }
    }

    private static boolean isAllowedJsonValue(JsonNode v) {
        JsonNodeType t = v.getNodeType();
        return t == JsonNodeType.OBJECT
                || t == JsonNodeType.ARRAY
                || t == JsonNodeType.STRING
                || t == JsonNodeType.NUMBER
                || t == JsonNodeType.BOOLEAN
                || t == JsonNodeType.NULL;
    }
}
