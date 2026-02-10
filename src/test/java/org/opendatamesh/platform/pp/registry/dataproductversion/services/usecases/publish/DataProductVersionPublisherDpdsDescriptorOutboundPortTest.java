package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.descriptorvalidator.DescriptorValidatorFactory;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DataProductVersionPublisherDpdsDescriptorOutboundPortTest {

    private static final String DPDS_SPEC = "dpds";
    private static final String DPDS_SPEC_VERSION_1 = "1.0.0";
    private static final String ENRICHMENT_DESCRIPTOR_RESOURCE = "dpds-minimal-for-enrichment-v1.0.0.json";

    private static final String DATA_PRODUCT_FQN = "urn:dpds:testDomain:dataproducts:testDataProduct:1";
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
            Pattern.CASE_INSENSITIVE
    );

    @Mock
    private DescriptorValidatorFactory descriptorValidatorFactory;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private DataProductVersionPublisherDpdsDescriptorOutboundPort outboundPort;

    @BeforeEach
    void setUp() {
        outboundPort = new DataProductVersionPublisherDpdsDescriptorOutboundPort(descriptorValidatorFactory);
    }

    private static String uuidV5FromFqn(String fullyQualifiedName) {
        return UUID.nameUUIDFromBytes(fullyQualifiedName.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private JsonNode loadDescriptor(String resourcePath) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test-data/" + resourcePath);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: test-data/" + resourcePath);
        }
        return objectMapper.readTree(inputStream);
    }

    // ---------- enrichDescriptorContentIfNeeded: auto-generated fields ----------

    @Test
    void whenEnrichDescriptorWithDpds10ThenInfoEntityTypeAndIdAreGenerated() throws IOException {
        // Given
        JsonNode descriptor = loadDescriptor(ENRICHMENT_DESCRIPTOR_RESOURCE);

        // When
        JsonNode result = outboundPort.enrichDescriptorContentIfNeeded(DPDS_SPEC, DPDS_SPEC_VERSION_1, descriptor);

        // Then
        JsonNode info = result.path("info");
        assertThat(info.isMissingNode()).isFalse();
        assertThat(info.path("entityType").asText()).isEqualTo("dataproduct");
        String expectedInfoId = uuidV5FromFqn(DATA_PRODUCT_FQN);
        assertThat(info.path("id").asText()).isEqualTo(expectedInfoId);
        assertThat(info.path("id").asText()).matches(UUID_PATTERN);
    }

    @Test
    void whenEnrichDescriptorWithDpds10ThenOutputPortGetsEntityTypeFqnAndId() throws IOException {
        // Given
        JsonNode descriptor = loadDescriptor(ENRICHMENT_DESCRIPTOR_RESOURCE);

        // When
        JsonNode result = outboundPort.enrichDescriptorContentIfNeeded(DPDS_SPEC, DPDS_SPEC_VERSION_1, descriptor);

        // Then
        JsonNode outputPorts = result.path("interfaceComponents").path("outputPorts");
        assertThat(outputPorts.isArray()).isTrue();
        assertThat(outputPorts.size()).isGreaterThanOrEqualTo(1);
        JsonNode port = outputPorts.get(0);
        assertThat(port.path("entityType").asText()).isEqualTo("outputport");
        String expectedFqn = DATA_PRODUCT_FQN + ":outputports:orders";
        assertThat(port.path("fullyQualifiedName").asText()).isEqualTo(expectedFqn);
        assertThat(port.path("id").asText()).isEqualTo(uuidV5FromFqn(expectedFqn));
        assertThat(port.path("id").asText()).matches(UUID_PATTERN);
    }

    @Test
    void whenEnrichDescriptorWithDpds10ThenInputPortGetsEntityTypeFqnAndId() throws IOException {
        // Given
        JsonNode descriptor = loadDescriptor(ENRICHMENT_DESCRIPTOR_RESOURCE);

        // When
        JsonNode result = outboundPort.enrichDescriptorContentIfNeeded(DPDS_SPEC, DPDS_SPEC_VERSION_1, descriptor);

        // Then
        JsonNode inputPorts = result.path("interfaceComponents").path("inputPorts");
        assertThat(inputPorts.size()).isGreaterThanOrEqualTo(1);
        JsonNode port = inputPorts.get(0);
        assertThat(port.path("entityType").asText()).isEqualTo("inputport");
        String expectedFqn = DATA_PRODUCT_FQN + ":inputports:inbound";
        assertThat(port.path("fullyQualifiedName").asText()).isEqualTo(expectedFqn);
        assertThat(port.path("id").asText()).isEqualTo(uuidV5FromFqn(expectedFqn));
    }

    @Test
    void whenEnrichDescriptorWithDpds10ThenDiscoveryPortGetsEntityTypeFqnAndId() throws IOException {
        // Given
        JsonNode descriptor = loadDescriptor(ENRICHMENT_DESCRIPTOR_RESOURCE);

        // When
        JsonNode result = outboundPort.enrichDescriptorContentIfNeeded(DPDS_SPEC, DPDS_SPEC_VERSION_1, descriptor);

        // Then
        JsonNode discoveryPorts = result.path("interfaceComponents").path("discoveryPorts");
        assertThat(discoveryPorts.size()).isGreaterThanOrEqualTo(1);
        JsonNode port = discoveryPorts.get(0);
        assertThat(port.path("entityType").asText()).isEqualTo("discoveryport");
        String expectedFqn = DATA_PRODUCT_FQN + ":discoveryports:catalog";
        assertThat(port.path("fullyQualifiedName").asText()).isEqualTo(expectedFqn);
        assertThat(port.path("id").asText()).isEqualTo(uuidV5FromFqn(expectedFqn));
    }

    @Test
    void whenEnrichDescriptorWithDpds10ThenObservabilityPortGetsEntityTypeFqnAndId() throws IOException {
        // Given
        JsonNode descriptor = loadDescriptor(ENRICHMENT_DESCRIPTOR_RESOURCE);

        // When
        JsonNode result = outboundPort.enrichDescriptorContentIfNeeded(DPDS_SPEC, DPDS_SPEC_VERSION_1, descriptor);

        // Then
        JsonNode observabilityPorts = result.path("interfaceComponents").path("observabilityPorts");
        assertThat(observabilityPorts.size()).isGreaterThanOrEqualTo(1);
        JsonNode port = observabilityPorts.get(0);
        assertThat(port.path("entityType").asText()).isEqualTo("observabilityport");
        String expectedFqn = DATA_PRODUCT_FQN + ":observabilityports:metrics";
        assertThat(port.path("fullyQualifiedName").asText()).isEqualTo(expectedFqn);
        assertThat(port.path("id").asText()).isEqualTo(uuidV5FromFqn(expectedFqn));
    }

    @Test
    void whenEnrichDescriptorWithDpds10ThenControlPortGetsEntityTypeFqnAndId() throws IOException {
        // Given
        JsonNode descriptor = loadDescriptor(ENRICHMENT_DESCRIPTOR_RESOURCE);

        // When
        JsonNode result = outboundPort.enrichDescriptorContentIfNeeded(DPDS_SPEC, DPDS_SPEC_VERSION_1, descriptor);

        // Then
        JsonNode controlPorts = result.path("interfaceComponents").path("controlPorts");
        assertThat(controlPorts.size()).isGreaterThanOrEqualTo(1);
        JsonNode port = controlPorts.get(0);
        assertThat(port.path("entityType").asText()).isEqualTo("controlport");
        String expectedFqn = DATA_PRODUCT_FQN + ":controlports:admin";
        assertThat(port.path("fullyQualifiedName").asText()).isEqualTo(expectedFqn);
        assertThat(port.path("id").asText()).isEqualTo(uuidV5FromFqn(expectedFqn));
    }

    @Test
    void whenEnrichDescriptorWithDpds10ThenApplicationComponentGetsEntityTypeFqnAndId() throws IOException {
        // Given
        JsonNode descriptor = loadDescriptor(ENRICHMENT_DESCRIPTOR_RESOURCE);

        // When
        JsonNode result = outboundPort.enrichDescriptorContentIfNeeded(DPDS_SPEC, DPDS_SPEC_VERSION_1, descriptor);

        // Then
        JsonNode appComponents = result.path("internalComponents").path("applicationComponents");
        assertThat(appComponents.size()).isGreaterThanOrEqualTo(1);
        JsonNode component = appComponents.get(0);
        assertThat(component.path("entityType").asText()).isEqualTo("application");
        String expectedFqn = DATA_PRODUCT_FQN + ":applications:orderProcessor";
        assertThat(component.path("fullyQualifiedName").asText()).isEqualTo(expectedFqn);
        assertThat(component.path("id").asText()).isEqualTo(uuidV5FromFqn(expectedFqn));
    }

    @Test
    void whenEnrichDescriptorWithDpds10ThenInfrastructuralComponentGetsEntityTypeFqnAndId() throws IOException {
        // Given
        JsonNode descriptor = loadDescriptor(ENRICHMENT_DESCRIPTOR_RESOURCE);

        // When
        JsonNode result = outboundPort.enrichDescriptorContentIfNeeded(DPDS_SPEC, DPDS_SPEC_VERSION_1, descriptor);

        // Then
        JsonNode infraComponents = result.path("internalComponents").path("infrastructuralComponents");
        assertThat(infraComponents.size()).isGreaterThanOrEqualTo(1);
        JsonNode component = infraComponents.get(0);
        assertThat(component.path("entityType").asText()).isEqualTo("infrastructure");
        String expectedFqn = DATA_PRODUCT_FQN + ":infrastructure:db";
        assertThat(component.path("fullyQualifiedName").asText()).isEqualTo(expectedFqn);
        assertThat(component.path("id").asText()).isEqualTo(uuidV5FromFqn(expectedFqn));
    }

    @Test
    void whenEnrichDescriptorWithDpds10AndPortWithPromisesApiThenStandardDefinitionGetsEntityTypeFqnAndId() throws IOException {
        // Given
        JsonNode descriptor = loadDescriptor(ENRICHMENT_DESCRIPTOR_RESOURCE);

        // When
        JsonNode result = outboundPort.enrichDescriptorContentIfNeeded(DPDS_SPEC, DPDS_SPEC_VERSION_1, descriptor);

        // Then - promises.api (StandardDefinition) gets entityType, fullyQualifiedName, id
        JsonNode api = result.path("interfaceComponents").path("outputPorts").get(0).path("promises").path("api");
        assertThat(api.isMissingNode()).isFalse();
        assertThat(api.path("entityType").asText()).isEqualTo("api");
        String expectedStdDefFqn = "urn:dpds:testDomain:apis:orders-api:1.0.0";
        assertThat(api.path("fullyQualifiedName").asText()).isEqualTo(expectedStdDefFqn);
        assertThat(api.path("id").asText()).isEqualTo(uuidV5FromFqn(expectedStdDefFqn));
        assertThat(api.path("id").asText()).matches(UUID_PATTERN);
    }

    // ---------- enrichDescriptorContentIfNeeded: no enrichment when spec/version not DPDS 1.x ----------

    @Test
    void whenEnrichDescriptorWithNullSpecThenDescriptorUnchanged() throws IOException {
        // Given
        JsonNode descriptor = loadDescriptor(ENRICHMENT_DESCRIPTOR_RESOURCE);

        // When
        JsonNode result = outboundPort.enrichDescriptorContentIfNeeded(null, DPDS_SPEC_VERSION_1, descriptor);

        // Then
        assertThat(result).isSameAs(descriptor);
        assertThat(descriptor.path("info").path("entityType").isMissingNode()).isTrue();
    }

    @Test
    void whenEnrichDescriptorWithEmptySpecThenDescriptorUnchanged() throws IOException {
        // Given
        JsonNode descriptor = loadDescriptor(ENRICHMENT_DESCRIPTOR_RESOURCE);

        // When
        JsonNode result = outboundPort.enrichDescriptorContentIfNeeded("", DPDS_SPEC_VERSION_1, descriptor);

        // Then
        assertThat(result).isSameAs(descriptor);
        assertThat(descriptor.path("info").path("entityType").isMissingNode()).isTrue();
    }

    @Test
    void whenEnrichDescriptorWithNonDpdsSpecThenDescriptorUnchanged() throws IOException {
        // Given
        JsonNode descriptor = loadDescriptor(ENRICHMENT_DESCRIPTOR_RESOURCE);

        // When
        JsonNode result = outboundPort.enrichDescriptorContentIfNeeded("other-spec", DPDS_SPEC_VERSION_1, descriptor);

        // Then
        assertThat(result).isSameAs(descriptor);
        assertThat(descriptor.path("info").path("entityType").isMissingNode()).isTrue();
    }

    @Test
    void whenEnrichDescriptorWithDpds20VersionThenDescriptorUnchanged() throws IOException {
        // Given
        JsonNode descriptor = loadDescriptor(ENRICHMENT_DESCRIPTOR_RESOURCE);

        // When
        JsonNode result = outboundPort.enrichDescriptorContentIfNeeded(DPDS_SPEC, "2.0.0", descriptor);

        // Then
        assertThat(result).isSameAs(descriptor);
        assertThat(descriptor.path("info").path("entityType").isMissingNode()).isTrue();
    }

    @Test
    void whenEnrichDescriptorWithEmptySpecVersionThenDescriptorUnchanged() throws IOException {
        // Given
        JsonNode descriptor = loadDescriptor(ENRICHMENT_DESCRIPTOR_RESOURCE);

        // When
        JsonNode result = outboundPort.enrichDescriptorContentIfNeeded(DPDS_SPEC, "", descriptor);

        // Then
        assertThat(result).isSameAs(descriptor);
    }

    // ---------- enrichDescriptorContentIfNeeded: edge cases ----------

    @Test
    void whenEnrichDescriptorWithInvalidJsonThenThrowBadRequestException() {
        // Given - not valid DPDS JSON
        JsonNode invalidDescriptor = objectMapper.createObjectNode()
                .put("dataProductDescriptor", "1.0.0")
                .put("info", "not-an-object");

        // When & Then
        assertThatThrownBy(() -> outboundPort.enrichDescriptorContentIfNeeded(DPDS_SPEC, DPDS_SPEC_VERSION_1, invalidDescriptor))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Failed to parse DPDS descriptor");
    }

    @Test
    void whenEnrichDescriptorWithDescriptorHavingExistingAutoFieldsThenExistingValuesPreserved() throws IOException {
        // Given - descriptor where info already has entityType and id
        JsonNode descriptor = loadDescriptor(ENRICHMENT_DESCRIPTOR_RESOURCE);
        String existingInfoId = "aaaaaaaa-bbbb-5ccc-dddd-eeeeeeeeeeee";
        ((com.fasterxml.jackson.databind.node.ObjectNode) descriptor.path("info"))
                .put("entityType", "dataproduct")
                .put("id", existingInfoId);

        // When
        JsonNode result = outboundPort.enrichDescriptorContentIfNeeded(DPDS_SPEC, DPDS_SPEC_VERSION_1, descriptor);

        // Then - existing values are preserved (visitor only fills when missing)
        assertThat(result.path("info").path("entityType").asText()).isEqualTo("dataproduct");
        assertThat(result.path("info").path("id").asText()).isEqualTo(existingInfoId);
    }

    @Test
    void whenEnrichDescriptorWithMinimalDescriptorWithoutInternalComponentsThenInfoEnriched() throws IOException {
        // Given - descriptor with info and empty outputPorts (no internalComponents in file)
        JsonNode descriptor = loadDescriptor("dpds-minimal-v1.0.0.json");

        // When
        JsonNode result = outboundPort.enrichDescriptorContentIfNeeded(DPDS_SPEC, DPDS_SPEC_VERSION_1, descriptor);

        // Then - info gets entityType and id; no ports so no port enrichment
        assertThat(result.path("info").path("entityType").asText()).isEqualTo("dataproduct");
        assertThat(result.path("info").path("id").asText()).isEqualTo(uuidV5FromFqn(DATA_PRODUCT_FQN));
        assertThat(result.path("interfaceComponents").path("outputPorts").size()).isZero();
    }
}
