package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Utility class for generating and validating DPDS component fields (entityType, fullyQualifiedName, id)
 * according to the DPDS 1.0.0 specification.
 */
class DpdsFieldGenerator {

    /**
     * Generate a component's fully qualified name by appending the component type segment and name
     * to the data product's FQN.
     * 
     * @param dataProductFqn The data product's fullyQualifiedName (from info.fullyQualifiedName)
     * @param componentTypeSegment The component type segment (e.g., "inputports", "outputports", "applications")
     * @param componentName The name of the component
     * @return The generated FQN (e.g., "urn:dpds:it.quantyca:dataproducts:tripExecution:1:inputports:tmsTripCDC")
     */
    String generateComponentFqn(String dataProductFqn, String componentTypeSegment, String componentName) {
        if (!StringUtils.hasText(dataProductFqn)) {
            throw new IllegalArgumentException("Data product FQN cannot be null or empty");
        }
        if (!StringUtils.hasText(componentTypeSegment)) {
            throw new IllegalArgumentException("Component type segment cannot be null or empty");
        }
        if (!StringUtils.hasText(componentName)) {
            throw new IllegalArgumentException("Component name cannot be null or empty");
        }
        
        return dataProductFqn + ":" + componentTypeSegment + ":" + componentName;
    }

    /**
     * Extract the mesh namespace from an info FQN for generating StandardDefinition FQNs.
     * Info FQN format: urn:dpds:{mesh-namespace}:dataproducts:{product-name}:{product-major-version}
     * Returns: urn:dpds:{mesh-namespace}
     * 
     * @param infoFqn The info fullyQualifiedName
     * @return The mesh namespace portion (e.g., "urn:dpds:it.quantyca")
     */
    String extractMeshNamespaceFromInfoFqn(String infoFqn) {
        if (!StringUtils.hasText(infoFqn)) {
            throw new IllegalArgumentException("Info FQN cannot be null or empty");
        }
        
        // Expected format: urn:dpds:{mesh-namespace}:dataproducts:{product-name}:{product-major-version}
        // Find the ":dataproducts:" marker and extract everything before it
        int dataproductsIndex = infoFqn.indexOf(":dataproducts:");
        if (dataproductsIndex == -1) {
            throw new IllegalArgumentException("Invalid info FQN format: missing ':dataproducts:' segment");
        }
        
        return infoFqn.substring(0, dataproductsIndex);
    }

    /**
     * Generate a StandardDefinition FQN using the mesh namespace, entity type, name, and version.
     * Format: urn:dpds:{mesh-namespace}:{entity-type}s:{name}:{version}
     * 
     * @param meshNamespace The mesh namespace (e.g., "urn:dpds:it.quantyca")
     * @param entityType The entity type ("api" or "template")
     * @param name The name of the standard definition
     * @param version The version of the standard definition
     * @return The generated FQN
     */
    String generateStandardDefinitionFqn(String meshNamespace, String entityType, String name, String version) {
        if (!StringUtils.hasText(meshNamespace)) {
            throw new IllegalArgumentException("Mesh namespace cannot be null or empty");
        }
        if (!StringUtils.hasText(entityType)) {
            throw new IllegalArgumentException("Entity type cannot be null or empty");
        }
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (!StringUtils.hasText(version)) {
            throw new IllegalArgumentException("Version cannot be null or empty");
        }
        
        // Pluralize entity type (api -> apis, template -> templates)
        String entityTypeSegment = entityType + "s";
        
        return meshNamespace + ":" + entityTypeSegment + ":" + name + ":" + version;
    }

    /**
     * Generate a UUID v5 from a fully qualified name using SHA-1 hash as per DPDS specification.
     * 
     * @param fullyQualifiedName The fully qualified name to hash
     * @return The generated UUID as a string
     */
    String generateIdFromFqn(String fullyQualifiedName) {
        if (!StringUtils.hasText(fullyQualifiedName)) {
            throw new IllegalArgumentException("Fully qualified name cannot be null or empty");
        }
        
        // Generate UUID v5 using SHA-1 hash of the FQN
        UUID uuid = UUID.nameUUIDFromBytes(fullyQualifiedName.getBytes(StandardCharsets.UTF_8));
        return uuid.toString();
    }

    /**
     * Validate that the entity type is correct, or report an error if it's incorrect.
     * If the entity type is missing, this method does not set it - that's done by the caller.
     * 
     * @param actualEntityType The actual entity type from the component
     * @param expectedEntityType The expected entity type
     * @param fieldPath The field path for error reporting
     * @param context The validation context
     * @return true if the entity type is present and incorrect (error reported), false otherwise
     */
    boolean validateEntityType(String actualEntityType, String expectedEntityType, String fieldPath, 
                               DpdsDescriptorValidationContext context) {
        if (StringUtils.hasText(actualEntityType)) {
            // Entity type is present - validate it
            if (!expectedEntityType.equals(actualEntityType)) {
                context.addError(fieldPath, 
                    String.format("Invalid entityType: expected '%s' but found '%s'", expectedEntityType, actualEntityType));
                return true; // Error reported
            }
        }
        return false; // No error
    }

    /**
     * Get the entity type constant for a port based on the port type string.
     * 
     * @param portType The port type ("inputPort", "outputPort", etc.)
     * @return The entity type constant ("inputport", "outputport", etc.)
     */
    String getPortEntityType(String portType) {
        if (portType == null) {
            return null;
        }
        
        switch (portType) {
            case "inputPort":
                return "inputport";
            case "outputPort":
                return "outputport";
            case "discoveryPort":
                return "discoveryport";
            case "observabilityPort":
                return "observabilityport";
            case "controlPort":
                return "controlport";
            default:
                return null;
        }
    }

    /**
     * Get the FQN segment for a port based on the port type string (plural form).
     * 
     * @param portType The port type ("inputPort", "outputPort", etc.)
     * @return The FQN segment ("inputports", "outputports", etc.)
     */
    String getPortFqnSegment(String portType) {
        if (portType == null) {
            return null;
        }
        
        switch (portType) {
            case "inputPort":
                return "inputports";
            case "outputPort":
                return "outputports";
            case "discoveryPort":
                return "discoveryports";
            case "observabilityPort":
                return "observabilityports";
            case "controlPort":
                return "controlports";
            default:
                return null;
        }
    }
}
