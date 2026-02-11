package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DpdsDescriptorValidatorTest {

    private static final String MINIMAL_DESCRIPTOR_RESOURCE = "dpds-minimal-v1.0.0.json";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DpdsDescriptorValidator validator = new DpdsDescriptorValidator();

    @Test
    void whenValidDescriptorThenNoException() throws IOException {
        // Given
        JsonNode descriptor = loadDescriptor(MINIMAL_DESCRIPTOR_RESOURCE);

        // When & Then
        validator.validateDescriptor(descriptor);
    }

    @Test
    void whenDescriptorIsNullJsonThenThrowBadRequestException() throws IOException {
        // Given
        JsonNode descriptor = objectMapper.readTree("null");

        // When & Then
        assertThatThrownBy(() -> validator.validateDescriptor(descriptor))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Descriptor root is null");
    }

    @Test
    void whenDataProductDescriptorMissingThenThrowBadRequestException() throws IOException {
        // Given
        ObjectNode descriptor = loadMinimalDescriptorMutable();
        descriptor.remove("dataProductDescriptor");

        // When & Then
        assertThatThrownBy(() -> validator.validateDescriptor(descriptor))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("DPDS descriptor validation failed:")
                .hasMessageContaining("dataProductDescriptor");
    }

    @Test
    void whenDataProductDescriptorEmptyThenThrowBadRequestException() throws IOException {
        // Given
        ObjectNode descriptor = loadMinimalDescriptorMutable();
        descriptor.put("dataProductDescriptor", "");

        // When & Then
        assertThatThrownBy(() -> validator.validateDescriptor(descriptor))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("DPDS descriptor validation failed:")
                .hasMessageContaining("dataProductDescriptor");
    }

    @Test
    void whenDataProductDescriptorInvalidSemverThenThrowBadRequestException() throws IOException {
        // Given
        ObjectNode descriptor = loadMinimalDescriptorMutable();
        descriptor.put("dataProductDescriptor", "not-a-version");

        // When & Then
        assertThatThrownBy(() -> validator.validateDescriptor(descriptor))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("DPDS descriptor validation failed:")
                .hasMessageContaining("dataProductDescriptor")
                .hasMessageContaining("semantic versioning");
    }

    @Test
    void whenInfoMissingThenThrowBadRequestException() throws IOException {
        // Given
        ObjectNode descriptor = loadMinimalDescriptorMutable();
        descriptor.remove("info");

        // When & Then
        assertThatThrownBy(() -> validator.validateDescriptor(descriptor))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("DPDS descriptor validation failed:")
                .hasMessageContaining("info");
    }

    @Test
    void whenInterfaceComponentsMissingThenThrowBadRequestException() throws IOException {
        // Given
        ObjectNode descriptor = loadMinimalDescriptorMutable();
        descriptor.remove("interfaceComponents");

        // When & Then
        assertThatThrownBy(() -> validator.validateDescriptor(descriptor))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("DPDS descriptor validation failed:")
                .hasMessageContaining("interfaceComponents");
    }

    @Test
    void whenInfoFullyQualifiedNameMissingThenThrowBadRequestException() throws IOException {
        // Given
        ObjectNode descriptor = loadMinimalDescriptorMutable();
        ObjectNode info = (ObjectNode) descriptor.get("info");
        if (info != null) {
            info.remove("fullyQualifiedName");
        }

        // When & Then
        assertThatThrownBy(() -> validator.validateDescriptor(descriptor))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("DPDS descriptor validation failed:")
                .hasMessageContaining("info.fullyQualifiedName");
    }

    @Test
    void whenInfoNameMissingThenThrowBadRequestException() throws IOException {
        // Given
        ObjectNode descriptor = loadMinimalDescriptorMutable();
        ObjectNode info = (ObjectNode) descriptor.get("info");
        if (info != null) {
            info.remove("name");
        }

        // When & Then
        assertThatThrownBy(() -> validator.validateDescriptor(descriptor))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("DPDS descriptor validation failed:")
                .hasMessageContaining("info.name");
    }

    @Test
    void whenInfoOwnerMissingThenThrowBadRequestException() throws IOException {
        // Given
        ObjectNode descriptor = loadMinimalDescriptorMutable();
        ObjectNode info = (ObjectNode) descriptor.get("info");
        if (info != null) {
            info.remove("owner");
        }

        // When & Then
        assertThatThrownBy(() -> validator.validateDescriptor(descriptor))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("DPDS descriptor validation failed:")
                .hasMessageContaining("info.owner");
    }

    @Test
    void whenDescriptorHasWrongTypeForDataProductDescriptorThenThrowBadRequestException() throws IOException {
        // Given - number instead of string can cause parse or validation issues
        ObjectNode descriptor = loadMinimalDescriptorMutable();
        descriptor.put("dataProductDescriptor", 123);

        // When & Then - parser may throw or validation may fail
        assertThatThrownBy(() -> validator.validateDescriptor(descriptor))
                .isInstanceOf(BadRequestException.class);
    }

    private JsonNode loadDescriptor(String resourcePath) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test-data/" + resourcePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: test-data/" + resourcePath);
        }
        return objectMapper.readTree(inputStream);
    }

    private ObjectNode loadMinimalDescriptorMutable() throws IOException {
        JsonNode loaded = loadDescriptor(MINIMAL_DESCRIPTOR_RESOURCE);
        return (ObjectNode) objectMapper.readTree(objectMapper.writeValueAsString(loaded));
    }
}
