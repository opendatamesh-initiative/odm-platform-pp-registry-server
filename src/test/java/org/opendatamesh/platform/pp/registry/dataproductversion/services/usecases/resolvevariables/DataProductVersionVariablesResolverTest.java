package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.resolvevariables;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.descriptorvariable.entities.DescriptorVariable;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataProductVersionVariablesResolverTest {

    @Mock
    private DataProductVersionVariablesResolverPresenter presenter;

    @Mock
    private DataProductVersionVariablesResolverPersistenceOutboundPort persistencePort;

    @Mock
    private DataProductVersionVariablesResolverDescriptorOutboundPort descriptorPort;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void whenCommandIsNullThenThrowBadRequestException() {
        // Given
        DataProductVersionVariablesResolver resolver = new DataProductVersionVariablesResolver(
                null, presenter, persistencePort, descriptorPort);

        // When & Then
        assertThatThrownBy(resolver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProductVersionVariablesResolverCommand cannot be null");

        verifyNoInteractions(persistencePort, descriptorPort, presenter);
    }

    static Stream<Arguments> invalidUuidProvider() {
        return Stream.of(
                Arguments.of((String) null),
                Arguments.of(""),
                Arguments.of("   ")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidUuidProvider")
    void whenDataProductVersionUuidIsInvalidThenThrowBadRequestException(String invalidUuid) {
        // Given
        DataProductVersionVariablesResolverCommand command = new DataProductVersionVariablesResolverCommand(invalidUuid);
        DataProductVersionVariablesResolver resolver = new DataProductVersionVariablesResolver(
                command, presenter, persistencePort, descriptorPort);

        // When & Then
        assertThatThrownBy(resolver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProductVersion UUID is required for resolving variables");

        verifyNoInteractions(persistencePort, descriptorPort, presenter);
    }

    @Test
    void whenDataProductVersionNotFoundThenThrowBadRequestException() {
        // Given
        String uuid = "dpv-uuid-123";
        DataProductVersionVariablesResolverCommand command = new DataProductVersionVariablesResolverCommand(uuid);
        when(persistencePort.findByUuid(uuid)).thenReturn(null);

        DataProductVersionVariablesResolver resolver = new DataProductVersionVariablesResolver(
                command, presenter, persistencePort, descriptorPort);

        // When & Then
        assertThatThrownBy(resolver::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("DataProductVersion not found: " + uuid);

        verify(persistencePort).findByUuid(uuid);
        verifyNoInteractions(descriptorPort, presenter);
        verify(persistencePort, never()).findDescriptorVariables(anyString());
    }

    static Stream<Arguments> descriptorVariablesProvider() {
        String uuid = "dpv-uuid-123";
        return Stream.of(
                Arguments.of(createDescriptorVariablesStatic(uuid), "with variables"),
                Arguments.of(new ArrayList<>(), "with empty list")
        );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("descriptorVariablesProvider")
    void whenResolveVariablesThenResolveSuccessfully(List<DescriptorVariable> descriptorVariables, String description) {
        // Given
        String uuid = "dpv-uuid-123";
        DataProductVersionVariablesResolverCommand command = new DataProductVersionVariablesResolverCommand(uuid);
        DataProductVersion dataProductVersion = createDataProductVersion(uuid);
        when(persistencePort.findByUuid(uuid)).thenReturn(dataProductVersion);
        when(persistencePort.findDescriptorVariables(uuid)).thenReturn(descriptorVariables);

        JsonNode resolvedContent = objectMapper.createObjectNode()
                .put("name", "Resolved Product")
                .put("version", "1.0.0");
        when(descriptorPort.resolveDescriptor(dataProductVersion, descriptorVariables)).thenReturn(resolvedContent);

        DataProductVersionVariablesResolver resolver = new DataProductVersionVariablesResolver(
                command, presenter, persistencePort, descriptorPort);

        // When
        resolver.execute();

        // Then
        verify(persistencePort).findByUuid(uuid);
        verify(persistencePort).findDescriptorVariables(uuid);
        verify(descriptorPort).resolveDescriptor(dataProductVersion, descriptorVariables);
        verify(presenter).presentDataProductVersionResolvedContent(dataProductVersion, resolvedContent);
    }

    @Test
    void whenResolveVariablesThenPresenterReceivesCorrectArguments() {
        // Given
        String uuid = "dpv-uuid-123";
        DataProductVersionVariablesResolverCommand command = new DataProductVersionVariablesResolverCommand(uuid);
        DataProductVersion dataProductVersion = createDataProductVersion(uuid);
        when(persistencePort.findByUuid(uuid)).thenReturn(dataProductVersion);

        List<DescriptorVariable> descriptorVariables = createDescriptorVariables(uuid);
        when(persistencePort.findDescriptorVariables(uuid)).thenReturn(descriptorVariables);

        JsonNode resolvedContent = objectMapper.createObjectNode()
                .put("name", "Resolved Product")
                .put("version", "1.0.0");
        when(descriptorPort.resolveDescriptor(dataProductVersion, descriptorVariables)).thenReturn(resolvedContent);

        DataProductVersionVariablesResolver resolver = new DataProductVersionVariablesResolver(
                command, presenter, persistencePort, descriptorPort);

        // When
        resolver.execute();

        // Then
        ArgumentCaptor<DataProductVersion> dataProductVersionCaptor = ArgumentCaptor.forClass(DataProductVersion.class);
        ArgumentCaptor<JsonNode> resolvedContentCaptor = ArgumentCaptor.forClass(JsonNode.class);

        verify(presenter).presentDataProductVersionResolvedContent(
                dataProductVersionCaptor.capture(),
                resolvedContentCaptor.capture());

        DataProductVersion presentedDataProductVersion = dataProductVersionCaptor.getValue();
        JsonNode presentedResolvedContent = resolvedContentCaptor.getValue();

        assertThat(presentedDataProductVersion)
                .usingRecursiveComparison()
                .isEqualTo(dataProductVersion);
        assertThat(presentedResolvedContent)
                .usingRecursiveComparison()
                .isEqualTo(resolvedContent);
    }

    @Test
    void whenResolveVariablesThenDescriptorPortReceivesCorrectArguments() {
        // Given
        String uuid = "dpv-uuid-123";
        DataProductVersionVariablesResolverCommand command = new DataProductVersionVariablesResolverCommand(uuid);
        DataProductVersion dataProductVersion = createDataProductVersion(uuid);
        when(persistencePort.findByUuid(uuid)).thenReturn(dataProductVersion);

        List<DescriptorVariable> descriptorVariables = createDescriptorVariables(uuid);
        when(persistencePort.findDescriptorVariables(uuid)).thenReturn(descriptorVariables);

        JsonNode resolvedContent = objectMapper.createObjectNode()
                .put("name", "Resolved Product")
                .put("version", "1.0.0");
        when(descriptorPort.resolveDescriptor(any(DataProductVersion.class), anyList())).thenReturn(resolvedContent);

        DataProductVersionVariablesResolver resolver = new DataProductVersionVariablesResolver(
                command, presenter, persistencePort, descriptorPort);

        // When
        resolver.execute();

        // Then
        ArgumentCaptor<DataProductVersion> dataProductVersionCaptor = ArgumentCaptor.forClass(DataProductVersion.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DescriptorVariable>> descriptorVariablesCaptor = ArgumentCaptor.forClass(List.class);

        verify(descriptorPort).resolveDescriptor(
                dataProductVersionCaptor.capture(),
                descriptorVariablesCaptor.capture());

        DataProductVersion capturedDataProductVersion = dataProductVersionCaptor.getValue();
        List<DescriptorVariable> capturedDescriptorVariables = descriptorVariablesCaptor.getValue();

        assertThat(capturedDataProductVersion)
                .usingRecursiveComparison()
                .isEqualTo(dataProductVersion);
        assertThat(capturedDescriptorVariables)
                .usingRecursiveComparison()
                .isEqualTo(descriptorVariables);
    }

    @Test
    void whenResolveVariablesThenPersistencePortCalledInCorrectOrder() {
        // Given
        String uuid = "dpv-uuid-123";
        DataProductVersionVariablesResolverCommand command = new DataProductVersionVariablesResolverCommand(uuid);
        DataProductVersion dataProductVersion = createDataProductVersion(uuid);
        when(persistencePort.findByUuid(uuid)).thenReturn(dataProductVersion);

        List<DescriptorVariable> descriptorVariables = createDescriptorVariables(uuid);
        when(persistencePort.findDescriptorVariables(uuid)).thenReturn(descriptorVariables);

        JsonNode resolvedContent = objectMapper.createObjectNode()
                .put("name", "Resolved Product")
                .put("version", "1.0.0");
        when(descriptorPort.resolveDescriptor(dataProductVersion, descriptorVariables)).thenReturn(resolvedContent);

        DataProductVersionVariablesResolver resolver = new DataProductVersionVariablesResolver(
                command, presenter, persistencePort, descriptorPort);

        // When
        resolver.execute();

        // Then
        var inOrder = inOrder(persistencePort, descriptorPort, presenter);
        inOrder.verify(persistencePort).findByUuid(uuid);
        inOrder.verify(persistencePort).findDescriptorVariables(uuid);
        inOrder.verify(descriptorPort).resolveDescriptor(dataProductVersion, descriptorVariables);
        inOrder.verify(presenter).presentDataProductVersionResolvedContent(dataProductVersion, resolvedContent);
    }

    private DataProductVersion createDataProductVersion(String uuid) {
        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid(uuid);
        dataProductVersion.setName("Test Product Version");
        dataProductVersion.setTag("v1.0.0");
        dataProductVersion.setSpec("ODM");
        dataProductVersion.setSpecVersion("1.0.0");
        JsonNode content = objectMapper.createObjectNode()
                .put("name", "Test Product")
                .put("version", "1.0.0");
        dataProductVersion.setContent(content);
        return dataProductVersion;
    }

    private List<DescriptorVariable> createDescriptorVariables(String dataProductVersionUuid) {
        return createDescriptorVariablesStatic(dataProductVersionUuid);
    }

    private static List<DescriptorVariable> createDescriptorVariablesStatic(String dataProductVersionUuid) {
        DescriptorVariable variable1 = new DescriptorVariable();
        variable1.setSequenceId(1L);
        variable1.setDataProductVersionUuid(dataProductVersionUuid);
        variable1.setVariableKey("key1");
        variable1.setVariableValue("value1");

        DescriptorVariable variable2 = new DescriptorVariable();
        variable2.setSequenceId(2L);
        variable2.setDataProductVersionUuid(dataProductVersionUuid);
        variable2.setVariableKey("key2");
        variable2.setVariableValue("value2");

        return List.of(variable1, variable2);
    }
}
