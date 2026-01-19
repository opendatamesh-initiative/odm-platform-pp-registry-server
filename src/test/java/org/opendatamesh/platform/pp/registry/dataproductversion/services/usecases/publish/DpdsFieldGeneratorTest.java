package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DpdsFieldGeneratorTest {

    private static final String VALID_DATA_PRODUCT_FQN = "urn:dpds:it.quantyca:dataproducts:tripExecution:1";
    private static final String VALID_MESH_NAMESPACE = "urn:dpds:it.quantyca";
    private static final String VALID_COMPONENT_NAME = "testPort";
    private static final String VALID_COMPONENT_TYPE_SEGMENT = "inputports";
    private static final String VALID_VERSION = "1.0.0";
    private static final String VALID_ENTITY_TYPE_API = "api";
    private static final String VALID_ENTITY_TYPE_TEMPLATE = "template";
    
    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", 
        Pattern.CASE_INSENSITIVE
    );

    private final DpdsFieldGenerator fieldGenerator = new DpdsFieldGenerator();

    // ========== generateComponentFqn Tests ==========

    @Test
    void whenAllParametersAreValidThenGenerateComponentFqn() {
        // Given
        String dataProductFqn = VALID_DATA_PRODUCT_FQN;
        String componentTypeSegment = VALID_COMPONENT_TYPE_SEGMENT;
        String componentName = VALID_COMPONENT_NAME;

        // When
        String result = fieldGenerator.generateComponentFqn(dataProductFqn, componentTypeSegment, componentName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("urn:dpds:it.quantyca:dataproducts:tripExecution:1:inputports:testPort");
    }

    @Test
    void whenDataProductFqnIsNullThenThrowIllegalArgumentException() {
        // Given
        String dataProductFqn = null;
        String componentTypeSegment = VALID_COMPONENT_TYPE_SEGMENT;
        String componentName = VALID_COMPONENT_NAME;

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.generateComponentFqn(dataProductFqn, componentTypeSegment, componentName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data product FQN cannot be null or empty");
    }

    @Test
    void whenDataProductFqnIsEmptyThenThrowIllegalArgumentException() {
        // Given
        String dataProductFqn = "";
        String componentTypeSegment = VALID_COMPONENT_TYPE_SEGMENT;
        String componentName = VALID_COMPONENT_NAME;

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.generateComponentFqn(dataProductFqn, componentTypeSegment, componentName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data product FQN cannot be null or empty");
    }

    @Test
    void whenDataProductFqnIsBlankThenThrowIllegalArgumentException() {
        // Given
        String dataProductFqn = "   ";
        String componentTypeSegment = VALID_COMPONENT_TYPE_SEGMENT;
        String componentName = VALID_COMPONENT_NAME;

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.generateComponentFqn(dataProductFqn, componentTypeSegment, componentName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data product FQN cannot be null or empty");
    }

    @Test
    void whenComponentTypeSegmentIsNullThenThrowIllegalArgumentException() {
        // Given
        String dataProductFqn = VALID_DATA_PRODUCT_FQN;
        String componentTypeSegment = null;
        String componentName = VALID_COMPONENT_NAME;

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.generateComponentFqn(dataProductFqn, componentTypeSegment, componentName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Component type segment cannot be null or empty");
    }

    @Test
    void whenComponentTypeSegmentIsEmptyThenThrowIllegalArgumentException() {
        // Given
        String dataProductFqn = VALID_DATA_PRODUCT_FQN;
        String componentTypeSegment = "";
        String componentName = VALID_COMPONENT_NAME;

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.generateComponentFqn(dataProductFqn, componentTypeSegment, componentName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Component type segment cannot be null or empty");
    }

    @Test
    void whenComponentNameIsNullThenThrowIllegalArgumentException() {
        // Given
        String dataProductFqn = VALID_DATA_PRODUCT_FQN;
        String componentTypeSegment = VALID_COMPONENT_TYPE_SEGMENT;
        String componentName = null;

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.generateComponentFqn(dataProductFqn, componentTypeSegment, componentName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Component name cannot be null or empty");
    }

    @Test
    void whenComponentNameIsEmptyThenThrowIllegalArgumentException() {
        // Given
        String dataProductFqn = VALID_DATA_PRODUCT_FQN;
        String componentTypeSegment = VALID_COMPONENT_TYPE_SEGMENT;
        String componentName = "";

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.generateComponentFqn(dataProductFqn, componentTypeSegment, componentName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Component name cannot be null or empty");
    }

    @Test
    void whenComponentNameIsBlankThenThrowIllegalArgumentException() {
        // Given
        String dataProductFqn = VALID_DATA_PRODUCT_FQN;
        String componentTypeSegment = VALID_COMPONENT_TYPE_SEGMENT;
        String componentName = "   ";

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.generateComponentFqn(dataProductFqn, componentTypeSegment, componentName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Component name cannot be null or empty");
    }

    @Test
    void whenAllParametersAreValidThenReturnCorrectFormat() {
        // Given
        String dataProductFqn = VALID_DATA_PRODUCT_FQN;
        String componentTypeSegment = "applications";
        String componentName = "testApp";

        // When
        String result = fieldGenerator.generateComponentFqn(dataProductFqn, componentTypeSegment, componentName);

        // Then
        assertThat(result).isEqualTo("urn:dpds:it.quantyca:dataproducts:tripExecution:1:applications:testApp");
        assertThat(result).startsWith(dataProductFqn);
        assertThat(result).contains(":" + componentTypeSegment + ":");
        assertThat(result).endsWith(":" + componentName);
    }

    // ========== extractMeshNamespaceFromInfoFqn Tests ==========

    @Test
    void whenInfoFqnIsValidThenExtractMeshNamespace() {
        // Given
        String infoFqn = VALID_DATA_PRODUCT_FQN;

        // When
        String result = fieldGenerator.extractMeshNamespaceFromInfoFqn(infoFqn);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(VALID_MESH_NAMESPACE);
    }

    @Test
    void whenInfoFqnIsNullThenThrowIllegalArgumentException() {
        // Given
        String infoFqn = null;

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.extractMeshNamespaceFromInfoFqn(infoFqn))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Info FQN cannot be null or empty");
    }

    @Test
    void whenInfoFqnIsEmptyThenThrowIllegalArgumentException() {
        // Given
        String infoFqn = "";

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.extractMeshNamespaceFromInfoFqn(infoFqn))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Info FQN cannot be null or empty");
    }

    @Test
    void whenInfoFqnIsBlankThenThrowIllegalArgumentException() {
        // Given
        String infoFqn = "   ";

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.extractMeshNamespaceFromInfoFqn(infoFqn))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Info FQN cannot be null or empty");
    }

    @Test
    void whenInfoFqnMissingDataproductsSegmentThenThrowIllegalArgumentException() {
        // Given
        String infoFqn = "urn:dpds:it.quantyca:something:else";

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.extractMeshNamespaceFromInfoFqn(infoFqn))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid info FQN format: missing ':dataproducts:' segment");
    }

    @Test
    void whenInfoFqnHasValidFormatThenReturnCorrectMeshNamespace() {
        // Given
        String infoFqn = "urn:dpds:test.domain:dataproducts:productName:2";

        // When
        String result = fieldGenerator.extractMeshNamespaceFromInfoFqn(infoFqn);

        // Then
        assertThat(result).isEqualTo("urn:dpds:test.domain");
        assertThat(result).startsWith("urn:dpds:");
    }

    // ========== generateStandardDefinitionFqn Tests ==========

    @Test
    void whenAllParametersAreValidThenGenerateStandardDefinitionFqn() {
        // Given
        String meshNamespace = VALID_MESH_NAMESPACE;
        String entityType = VALID_ENTITY_TYPE_API;
        String name = "testApi";
        String version = VALID_VERSION;

        // When
        String result = fieldGenerator.generateStandardDefinitionFqn(meshNamespace, entityType, name, version);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("urn:dpds:it.quantyca:apis:testApi:1.0.0");
    }

    @Test
    void whenMeshNamespaceIsNullThenThrowIllegalArgumentException() {
        // Given
        String meshNamespace = null;
        String entityType = VALID_ENTITY_TYPE_API;
        String name = "testApi";
        String version = VALID_VERSION;

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.generateStandardDefinitionFqn(meshNamespace, entityType, name, version))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mesh namespace cannot be null or empty");
    }

    @Test
    void whenMeshNamespaceIsEmptyThenThrowIllegalArgumentException() {
        // Given
        String meshNamespace = "";
        String entityType = VALID_ENTITY_TYPE_API;
        String name = "testApi";
        String version = VALID_VERSION;

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.generateStandardDefinitionFqn(meshNamespace, entityType, name, version))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mesh namespace cannot be null or empty");
    }

    @Test
    void whenEntityTypeIsNullThenThrowIllegalArgumentException() {
        // Given
        String meshNamespace = VALID_MESH_NAMESPACE;
        String entityType = null;
        String name = "testApi";
        String version = VALID_VERSION;

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.generateStandardDefinitionFqn(meshNamespace, entityType, name, version))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Entity type cannot be null or empty");
    }

    @Test
    void whenEntityTypeIsEmptyThenThrowIllegalArgumentException() {
        // Given
        String meshNamespace = VALID_MESH_NAMESPACE;
        String entityType = "";
        String name = "testApi";
        String version = VALID_VERSION;

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.generateStandardDefinitionFqn(meshNamespace, entityType, name, version))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Entity type cannot be null or empty");
    }

    @Test
    void whenNameIsNullThenThrowIllegalArgumentException() {
        // Given
        String meshNamespace = VALID_MESH_NAMESPACE;
        String entityType = VALID_ENTITY_TYPE_API;
        String name = null;
        String version = VALID_VERSION;

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.generateStandardDefinitionFqn(meshNamespace, entityType, name, version))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name cannot be null or empty");
    }

    @Test
    void whenNameIsEmptyThenThrowIllegalArgumentException() {
        // Given
        String meshNamespace = VALID_MESH_NAMESPACE;
        String entityType = VALID_ENTITY_TYPE_API;
        String name = "";
        String version = VALID_VERSION;

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.generateStandardDefinitionFqn(meshNamespace, entityType, name, version))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name cannot be null or empty");
    }

    @Test
    void whenVersionIsNullThenThrowIllegalArgumentException() {
        // Given
        String meshNamespace = VALID_MESH_NAMESPACE;
        String entityType = VALID_ENTITY_TYPE_API;
        String name = "testApi";
        String version = null;

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.generateStandardDefinitionFqn(meshNamespace, entityType, name, version))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Version cannot be null or empty");
    }

    @Test
    void whenVersionIsEmptyThenThrowIllegalArgumentException() {
        // Given
        String meshNamespace = VALID_MESH_NAMESPACE;
        String entityType = VALID_ENTITY_TYPE_API;
        String name = "testApi";
        String version = "";

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.generateStandardDefinitionFqn(meshNamespace, entityType, name, version))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Version cannot be null or empty");
    }

    @Test
    void whenEntityTypeIsApiThenPluralizeToApis() {
        // Given
        String meshNamespace = VALID_MESH_NAMESPACE;
        String entityType = VALID_ENTITY_TYPE_API;
        String name = "testApi";
        String version = VALID_VERSION;

        // When
        String result = fieldGenerator.generateStandardDefinitionFqn(meshNamespace, entityType, name, version);

        // Then
        assertThat(result).contains(":apis:");
        assertThat(result).doesNotContain(":api:");
    }

    @Test
    void whenEntityTypeIsTemplateThenPluralizeToTemplates() {
        // Given
        String meshNamespace = VALID_MESH_NAMESPACE;
        String entityType = VALID_ENTITY_TYPE_TEMPLATE;
        String name = "testTemplate";
        String version = VALID_VERSION;

        // When
        String result = fieldGenerator.generateStandardDefinitionFqn(meshNamespace, entityType, name, version);

        // Then
        assertThat(result).contains(":templates:");
        assertThat(result).doesNotContain(":template:");
    }

    @Test
    void whenAllParametersAreValidForStandardDefinitionThenReturnCorrectFormat() {
        // Given
        String meshNamespace = VALID_MESH_NAMESPACE;
        String entityType = VALID_ENTITY_TYPE_API;
        String name = "testApi";
        String version = VALID_VERSION;

        // When
        String result = fieldGenerator.generateStandardDefinitionFqn(meshNamespace, entityType, name, version);

        // Then
        assertThat(result).isEqualTo("urn:dpds:it.quantyca:apis:testApi:1.0.0");
        assertThat(result).startsWith(meshNamespace);
        assertThat(result).contains(":apis:");
        assertThat(result).contains(":" + name + ":");
        assertThat(result).endsWith(":" + version);
    }

    // ========== generateIdFromFqn Tests ==========

    @Test
    void whenFqnIsValidThenGenerateUuidV5() {
        // Given
        String fqn = VALID_DATA_PRODUCT_FQN;

        // When
        String result = fieldGenerator.generateIdFromFqn(fqn);

        // Then
        assertThat(result).isNotNull();
        assertThat(UUID_PATTERN.matcher(result).matches()).isTrue();
    }

    @Test
    void whenFqnIsNullThenThrowIllegalArgumentException() {
        // Given
        String fqn = null;

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.generateIdFromFqn(fqn))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Fully qualified name cannot be null or empty");
    }

    @Test
    void whenFqnIsEmptyThenThrowIllegalArgumentException() {
        // Given
        String fqn = "";

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.generateIdFromFqn(fqn))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Fully qualified name cannot be null or empty");
    }

    @Test
    void whenFqnIsBlankThenThrowIllegalArgumentException() {
        // Given
        String fqn = "   ";

        // When & Then
        assertThatThrownBy(() -> fieldGenerator.generateIdFromFqn(fqn))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Fully qualified name cannot be null or empty");
    }

    @Test
    void whenSameFqnIsUsedThenGenerateSameUuid() {
        // Given
        String fqn = VALID_DATA_PRODUCT_FQN;

        // When
        String result1 = fieldGenerator.generateIdFromFqn(fqn);
        String result2 = fieldGenerator.generateIdFromFqn(fqn);

        // Then
        assertThat(result1).isEqualTo(result2);
    }

    @Test
    void whenDifferentFqnsAreUsedThenGenerateDifferentUuids() {
        // Given
        String fqn1 = VALID_DATA_PRODUCT_FQN;
        String fqn2 = "urn:dpds:it.quantyca:dataproducts:differentProduct:1";

        // When
        String result1 = fieldGenerator.generateIdFromFqn(fqn1);
        String result2 = fieldGenerator.generateIdFromFqn(fqn2);

        // Then
        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    void whenFqnIsValidThenReturnUuidStringFormat() {
        // Given
        String fqn = VALID_DATA_PRODUCT_FQN;

        // When
        String result = fieldGenerator.generateIdFromFqn(fqn);

        // Then
        assertThat(result).matches(UUID_PATTERN);
        // Verify it's a valid UUID by parsing it
        UUID.fromString(result); // Should not throw
    }

    // ========== validateEntityType Tests ==========

    @Test
    void whenActualEntityTypeMatchesExpectedThenReturnFalse() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        String actualEntityType = "dataproduct";
        String expectedEntityType = "dataproduct";
        String fieldPath = "info.entityType";

        // When
        boolean result = fieldGenerator.validateEntityType(actualEntityType, expectedEntityType, fieldPath, context);

        // Then
        assertThat(result).isFalse();
        assertThat(context.hasErrors()).isFalse();
    }

    @Test
    void whenActualEntityTypeDoesNotMatchExpectedThenAddErrorAndReturnTrue() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        String actualEntityType = "wrongType";
        String expectedEntityType = "dataproduct";
        String fieldPath = "info.entityType";

        // When
        boolean result = fieldGenerator.validateEntityType(actualEntityType, expectedEntityType, fieldPath, context);

        // Then
        assertThat(result).isTrue();
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().equals(fieldPath) &&
            error.getMessage().contains("Invalid entityType") &&
            error.getMessage().contains("expected 'dataproduct'") &&
            error.getMessage().contains("found 'wrongType'"));
    }

    @Test
    void whenActualEntityTypeIsNullThenReturnFalse() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        String actualEntityType = null;
        String expectedEntityType = "dataproduct";
        String fieldPath = "info.entityType";

        // When
        boolean result = fieldGenerator.validateEntityType(actualEntityType, expectedEntityType, fieldPath, context);

        // Then
        assertThat(result).isFalse();
        assertThat(context.hasErrors()).isFalse();
    }

    @Test
    void whenActualEntityTypeIsEmptyThenReturnFalse() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        String actualEntityType = "";
        String expectedEntityType = "dataproduct";
        String fieldPath = "info.entityType";

        // When
        boolean result = fieldGenerator.validateEntityType(actualEntityType, expectedEntityType, fieldPath, context);

        // Then
        assertThat(result).isFalse();
        assertThat(context.hasErrors()).isFalse();
    }

    @Test
    void whenActualEntityTypeIsBlankThenReturnFalse() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        String actualEntityType = "   ";
        String expectedEntityType = "dataproduct";
        String fieldPath = "info.entityType";

        // When
        boolean result = fieldGenerator.validateEntityType(actualEntityType, expectedEntityType, fieldPath, context);

        // Then
        assertThat(result).isFalse();
        assertThat(context.hasErrors()).isFalse();
    }

    @Test
    void whenErrorIsAddedThenContextContainsCorrectError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        String actualEntityType = "inputport";
        String expectedEntityType = "outputport";
        String fieldPath = "interfaceComponents.outputPorts[0].entityType";

        // When
        fieldGenerator.validateEntityType(actualEntityType, expectedEntityType, fieldPath, context);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).hasSize(1);
        DpdsDescriptorValidationErrorMessage error = context.getErrors().get(0);
        assertThat(error.getFieldPath()).isEqualTo(fieldPath);
        assertThat(error.getMessage()).contains("Invalid entityType");
        assertThat(error.getMessage()).contains("expected 'outputport'");
        assertThat(error.getMessage()).contains("found 'inputport'");
    }

    // ========== getPortEntityType Tests ==========

    @Test
    void whenPortTypeIsInputPortThenReturnInputport() {
        // Given
        String portType = "inputPort";

        // When
        String result = fieldGenerator.getPortEntityType(portType);

        // Then
        assertThat(result).isEqualTo("inputport");
    }

    @Test
    void whenPortTypeIsOutputPortThenReturnOutputport() {
        // Given
        String portType = "outputPort";

        // When
        String result = fieldGenerator.getPortEntityType(portType);

        // Then
        assertThat(result).isEqualTo("outputport");
    }

    @Test
    void whenPortTypeIsDiscoveryPortThenReturnDiscoveryport() {
        // Given
        String portType = "discoveryPort";

        // When
        String result = fieldGenerator.getPortEntityType(portType);

        // Then
        assertThat(result).isEqualTo("discoveryport");
    }

    @Test
    void whenPortTypeIsObservabilityPortThenReturnObservabilityport() {
        // Given
        String portType = "observabilityPort";

        // When
        String result = fieldGenerator.getPortEntityType(portType);

        // Then
        assertThat(result).isEqualTo("observabilityport");
    }

    @Test
    void whenPortTypeIsControlPortThenReturnControlport() {
        // Given
        String portType = "controlPort";

        // When
        String result = fieldGenerator.getPortEntityType(portType);

        // Then
        assertThat(result).isEqualTo("controlport");
    }

    @Test
    void whenPortTypeIsNullForEntityTypeThenReturnNull() {
        // Given
        String portType = null;

        // When
        String result = fieldGenerator.getPortEntityType(portType);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void whenPortTypeIsUnknownForEntityTypeThenReturnNull() {
        // Given
        String portType = "unknownPort";

        // When
        String result = fieldGenerator.getPortEntityType(portType);

        // Then
        assertThat(result).isNull();
    }

    // ========== getPortFqnSegment Tests ==========

    @Test
    void whenPortTypeIsInputPortThenReturnInputports() {
        // Given
        String portType = "inputPort";

        // When
        String result = fieldGenerator.getPortFqnSegment(portType);

        // Then
        assertThat(result).isEqualTo("inputports");
    }

    @Test
    void whenPortTypeIsOutputPortThenReturnOutputports() {
        // Given
        String portType = "outputPort";

        // When
        String result = fieldGenerator.getPortFqnSegment(portType);

        // Then
        assertThat(result).isEqualTo("outputports");
    }

    @Test
    void whenPortTypeIsDiscoveryPortThenReturnDiscoveryports() {
        // Given
        String portType = "discoveryPort";

        // When
        String result = fieldGenerator.getPortFqnSegment(portType);

        // Then
        assertThat(result).isEqualTo("discoveryports");
    }

    @Test
    void whenPortTypeIsObservabilityPortThenReturnObservabilityports() {
        // Given
        String portType = "observabilityPort";

        // When
        String result = fieldGenerator.getPortFqnSegment(portType);

        // Then
        assertThat(result).isEqualTo("observabilityports");
    }

    @Test
    void whenPortTypeIsControlPortThenReturnControlports() {
        // Given
        String portType = "controlPort";

        // When
        String result = fieldGenerator.getPortFqnSegment(portType);

        // Then
        assertThat(result).isEqualTo("controlports");
    }

    @Test
    void whenPortTypeIsNullForFqnSegmentThenReturnNull() {
        // Given
        String portType = null;

        // When
        String result = fieldGenerator.getPortFqnSegment(portType);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void whenPortTypeIsUnknownForFqnSegmentThenReturnNull() {
        // Given
        String portType = "unknownPort";

        // When
        String result = fieldGenerator.getPortFqnSegment(portType);

        // Then
        assertThat(result).isNull();
    }
}