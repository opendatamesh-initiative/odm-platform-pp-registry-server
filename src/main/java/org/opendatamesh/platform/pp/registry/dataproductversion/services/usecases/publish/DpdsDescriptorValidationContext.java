package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;

import java.util.*;
import java.util.regex.Pattern;

class DpdsDescriptorValidationContext {
    private final List<DpdsDescriptorValidationErrorMessage> errors = new ArrayList<>();
    
    // Track names for uniqueness validation
    private final List<String> inputPortNames = new ArrayList<>();
    private final List<String> outputPortNames = new ArrayList<>();
    private final List<String> discoveryPortNames = new ArrayList<>();
    private final List<String> observabilityPortNames = new ArrayList<>();
    private final List<String> controlPortNames = new ArrayList<>();
    private final List<String> applicationComponentNames = new ArrayList<>();
    private final List<String> infrastructuralComponentNames = new ArrayList<>();
    
    // Track component keys for uniqueness and format validation
    private final Map<String, Set<String>> componentKeys = new HashMap<>();
    
    // Regex pattern for component key validation: ^[a-zA-Z0-9\.\-_]+$
    private static final Pattern COMPONENT_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9\\.\\-_]+$");

    void addError(String fieldPath, String message) {
        errors.add(new DpdsDescriptorValidationErrorMessage(fieldPath, message));
    }

    boolean hasErrors() {
        return !errors.isEmpty();
    }

    List<DpdsDescriptorValidationErrorMessage> getErrors() {
        return new ArrayList<>(errors);
    }

    void throwIfHasErrors() {
        if (hasErrors()) {
            StringBuilder messageBuilder = new StringBuilder("DPDS descriptor validation failed:");
            for (DpdsDescriptorValidationErrorMessage error : errors) {
                messageBuilder.append(" ").append(error.format()).append(";");
            }
            // Remove the last semicolon
            String errorMessage = messageBuilder.toString();
            if (errorMessage.endsWith(";")) {
                errorMessage = errorMessage.substring(0, errorMessage.length() - 1);
            }
            throw new BadRequestException(errorMessage);
        }
    }
    
    // Methods to add names and check for duplicates
    boolean addInputPortName(String name, String fieldPath) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        if (inputPortNames.contains(name)) {
            addError(fieldPath, String.format("Input port name '%s' is not unique within inputPorts", name));
            return true;
        }
        inputPortNames.add(name);
        return false;
    }
    
    boolean addOutputPortName(String name, String fieldPath) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        if (outputPortNames.contains(name)) {
            addError(fieldPath, String.format("Output port name '%s' is not unique within outputPorts", name));
            return true;
        }
        outputPortNames.add(name);
        return false;
    }
    
    boolean addDiscoveryPortName(String name, String fieldPath) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        if (discoveryPortNames.contains(name)) {
            addError(fieldPath, String.format("Discovery port name '%s' is not unique within discoveryPorts", name));
            return true;
        }
        discoveryPortNames.add(name);
        return false;
    }
    
    boolean addObservabilityPortName(String name, String fieldPath) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        if (observabilityPortNames.contains(name)) {
            addError(fieldPath, String.format("Observability port name '%s' is not unique within observabilityPorts", name));
            return true;
        }
        observabilityPortNames.add(name);
        return false;
    }
    
    boolean addControlPortName(String name, String fieldPath) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        if (controlPortNames.contains(name)) {
            addError(fieldPath, String.format("Control port name '%s' is not unique within controlPorts", name));
            return true;
        }
        controlPortNames.add(name);
        return false;
    }
    
    boolean addApplicationComponentName(String name, String fieldPath) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        if (applicationComponentNames.contains(name)) {
            addError(fieldPath, String.format("Application component name '%s' is not unique within applicationComponents", name));
            return true;
        }
        applicationComponentNames.add(name);
        return false;
    }
    
    boolean addInfrastructuralComponentName(String name, String fieldPath) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        if (infrastructuralComponentNames.contains(name)) {
            addError(fieldPath, String.format("Infrastructural component name '%s' is not unique within infrastructuralComponents", name));
            return true;
        }
        infrastructuralComponentNames.add(name);
        return false;
    }
    
    boolean addComponentKey(String mapType, String key, String fieldPath) {
        if (key == null || key.isEmpty()) {
            addError(fieldPath, "Component key cannot be null or empty");
            return true;
        }
        
        // Validate key format
        if (!COMPONENT_KEY_PATTERN.matcher(key).matches()) {
            addError(fieldPath, String.format("Component key '%s' does not match required format (^[a-zA-Z0-9\\.\\-_]+$)", key));
            return true;
        }
        
        // Check uniqueness
        componentKeys.putIfAbsent(mapType, new HashSet<>());
        Set<String> keys = componentKeys.get(mapType);
        if (keys.contains(key)) {
            addError(fieldPath, String.format("Component key '%s' is not unique within %s", key, mapType));
            return true;
        }
        keys.add(key);
        return false;
    }
    
    /**
     * Add an error if the actual entity type doesn't match the expected entity type.
     * 
     * @param actual The actual entity type value
     * @param expected The expected entity type value
     * @param fieldPath The field path for error reporting
     * @return true if an error was added, false otherwise
     */
    boolean addErrorIfInvalidEntityType(String actual, String expected, String fieldPath) {
        if (actual != null && !actual.isEmpty() && !expected.equals(actual)) {
            addError(fieldPath, String.format("Invalid entityType: expected '%s' but found '%s'", expected, actual));
            return true;
        }
        return false;
    }
}
