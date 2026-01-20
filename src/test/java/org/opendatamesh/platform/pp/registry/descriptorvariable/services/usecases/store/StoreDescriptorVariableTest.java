package org.opendatamesh.platform.pp.registry.descriptorvariable.services.usecases.store;

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
import org.opendatamesh.platform.pp.registry.exceptions.NotFoundException;
import org.opendatamesh.platform.pp.registry.utils.usecases.TransactionalOutboundPort;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreDescriptorVariableTest {

    @Mock
    private StoreDescriptorVariablePresenter presenter;

    @Mock
    private StoreDescriptorVariablePersistenceOutboundPort persistencePort;

    @Mock
    private StoreDescriptorVariableValidationOutboundPort validationPort;

    @Mock
    private TransactionalOutboundPort transactionalPort;

    static Stream<Arguments> nullOrEmptyCommandProvider() {
        return Stream.of(
                Arguments.of((Supplier<StoreDescriptorVariableCommand>) () -> null, "StoreDescriptorVariableCommand cannot be null"),
                Arguments.of((Supplier<StoreDescriptorVariableCommand>) () -> new StoreDescriptorVariableCommand(null), "DescriptorVariables list cannot be null or empty"),
                Arguments.of((Supplier<StoreDescriptorVariableCommand>) () -> new StoreDescriptorVariableCommand(new ArrayList<>()), "DescriptorVariables list cannot be null or empty"),
                Arguments.of((Supplier<StoreDescriptorVariableCommand>) () -> {
                    List<DescriptorVariable> variables = new ArrayList<>();
                    variables.add(null);
                    return new StoreDescriptorVariableCommand(variables);
                }, "DescriptorVariable in list cannot be null")
        );
    }

    @ParameterizedTest
    @MethodSource("nullOrEmptyCommandProvider")
    void whenCommandOrListIsInvalidThenThrowBadRequestException(Supplier<StoreDescriptorVariableCommand> commandSupplier, String expectedMessage) {
        // Given
        StoreDescriptorVariableCommand command = commandSupplier.get();
        StoreDescriptorVariable useCase = new StoreDescriptorVariable(
                command, presenter, persistencePort, validationPort, transactionalPort);

        // When & Then
        assertThatThrownBy(useCase::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage(expectedMessage);

        verifyNoInteractions(persistencePort, validationPort, presenter, transactionalPort);
    }


    static Stream<Arguments> invalidFieldValueProvider() {
        return Stream.of(
                Arguments.of(null, "test-key", "Missing DataProductVersionUuid on DescriptorVariable"),
                Arguments.of("", "test-key", "Missing DataProductVersionUuid on DescriptorVariable"),
                Arguments.of("   ", "test-key", "Missing DataProductVersionUuid on DescriptorVariable"),
                Arguments.of("dpv-uuid-123", null, "Missing variable key on DescriptorVariable"),
                Arguments.of("dpv-uuid-123", "", "Missing variable key on DescriptorVariable"),
                Arguments.of("dpv-uuid-123", "   ", "Missing variable key on DescriptorVariable")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidFieldValueProvider")
    void whenFieldValueIsInvalidThenThrowBadRequestException(String dataProductVersionUuid, String variableKey, String expectedMessage) {
        // Given
        DescriptorVariable variable = new DescriptorVariable();
        variable.setDataProductVersionUuid(dataProductVersionUuid);
        variable.setVariableKey(variableKey);
        List<DescriptorVariable> variables = List.of(variable);
        StoreDescriptorVariableCommand command = new StoreDescriptorVariableCommand(variables);
        StoreDescriptorVariable useCase = new StoreDescriptorVariable(
                command, presenter, persistencePort, validationPort, transactionalPort);

        // When & Then
        assertThatThrownBy(useCase::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage(expectedMessage);

        verifyNoInteractions(persistencePort, validationPort, presenter, transactionalPort);
    }

    @Test
    void whenDataProductVersionNotFoundThenThrowNotFoundException() {
        // Given
        DescriptorVariable variable = new DescriptorVariable();
        variable.setDataProductVersionUuid("dpv-uuid-123");
        variable.setVariableKey("test-key");
        variable.setVariableValue("test-value");
        List<DescriptorVariable> variables = List.of(variable);
        StoreDescriptorVariableCommand command = new StoreDescriptorVariableCommand(variables);
        StoreDescriptorVariable useCase = new StoreDescriptorVariable(
                command, presenter, persistencePort, validationPort, transactionalPort);

        when(persistencePort.findDataProductVersionByUuid("dpv-uuid-123"))
                .thenThrow(new NotFoundException("DataProductVersion with uuid=dpv-uuid-123 not found"));

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        // When & Then
        assertThatThrownBy(useCase::execute)
                .isInstanceOf(NotFoundException.class)
                .hasMessage("DataProductVersion with uuid=dpv-uuid-123 not found");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findDataProductVersionByUuid("dpv-uuid-123");
        verifyNoInteractions(validationPort, presenter);
    }

    @Test
    void whenStoreSingleVariableThenStoreSuccessfully() {
        // Given
        DescriptorVariable variable = new DescriptorVariable();
        variable.setDataProductVersionUuid("dpv-uuid-123");
        variable.setVariableKey("test-key");
        variable.setVariableValue("test-value");
        List<DescriptorVariable> variables = List.of(variable);
        StoreDescriptorVariableCommand command = new StoreDescriptorVariableCommand(variables);

        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("dpv-uuid-123");

        DescriptorVariable savedVariable = new DescriptorVariable();
        savedVariable.setSequenceId(123L);
        savedVariable.setDataProductVersionUuid("dpv-uuid-123");
        savedVariable.setVariableKey("test-key");
        savedVariable.setVariableValue("test-value");

        when(persistencePort.findDataProductVersionByUuid("dpv-uuid-123")).thenReturn(dataProductVersion);
        when(persistencePort.createOrOverride(any(DescriptorVariable.class))).thenReturn(savedVariable);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        StoreDescriptorVariable useCase = new StoreDescriptorVariable(
                command, presenter, persistencePort, validationPort, transactionalPort);

        // When
        useCase.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findDataProductVersionByUuid("dpv-uuid-123");
        verify(validationPort).validateVariablesCanBeAppliedToDescriptor(dataProductVersion, variables);
        verify(persistencePort).createOrOverride(any(DescriptorVariable.class));
        verify(presenter).presentDescriptorVariableStored(savedVariable);
    }

    @Test
    void whenStoreMultipleVariablesForSameDataProductVersionThenStoreAllSuccessfully() {
        // Given
        DescriptorVariable variable1 = new DescriptorVariable();
        variable1.setDataProductVersionUuid("dpv-uuid-123");
        variable1.setVariableKey("key1");
        variable1.setVariableValue("value1");

        DescriptorVariable variable2 = new DescriptorVariable();
        variable2.setDataProductVersionUuid("dpv-uuid-123");
        variable2.setVariableKey("key2");
        variable2.setVariableValue("value2");

        List<DescriptorVariable> variables = List.of(variable1, variable2);
        StoreDescriptorVariableCommand command = new StoreDescriptorVariableCommand(variables);

        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("dpv-uuid-123");

        DescriptorVariable savedVariable1 = new DescriptorVariable();
        savedVariable1.setSequenceId(1L);
        savedVariable1.setDataProductVersionUuid("dpv-uuid-123");
        savedVariable1.setVariableKey("key1");
        savedVariable1.setVariableValue("value1");

        DescriptorVariable savedVariable2 = new DescriptorVariable();
        savedVariable2.setSequenceId(2L);
        savedVariable2.setDataProductVersionUuid("dpv-uuid-123");
        savedVariable2.setVariableKey("key2");
        savedVariable2.setVariableValue("value2");

        when(persistencePort.findDataProductVersionByUuid("dpv-uuid-123")).thenReturn(dataProductVersion);
        when(persistencePort.createOrOverride(any(DescriptorVariable.class)))
                .thenReturn(savedVariable1)
                .thenReturn(savedVariable2);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        StoreDescriptorVariable useCase = new StoreDescriptorVariable(
                command, presenter, persistencePort, validationPort, transactionalPort);

        // When
        useCase.execute();

        // Then
        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findDataProductVersionByUuid("dpv-uuid-123");
        verify(validationPort).validateVariablesCanBeAppliedToDescriptor(dataProductVersion, variables);
        verify(persistencePort, times(2)).createOrOverride(any(DescriptorVariable.class));
        verify(presenter).presentDescriptorVariableStored(savedVariable1);
        verify(presenter).presentDescriptorVariableStored(savedVariable2);
    }

    @Test
    void whenStoreVariablesForMultipleDataProductVersionsThenGroupAndStoreSuccessfully() {
        // Given
        DescriptorVariable variable1 = new DescriptorVariable();
        variable1.setDataProductVersionUuid("dpv-uuid-123");
        variable1.setVariableKey("key1");
        variable1.setVariableValue("value1");

        DescriptorVariable variable2 = new DescriptorVariable();
        variable2.setDataProductVersionUuid("dpv-uuid-456");
        variable2.setVariableKey("key2");
        variable2.setVariableValue("value2");

        List<DescriptorVariable> variables = List.of(variable1, variable2);
        StoreDescriptorVariableCommand command = new StoreDescriptorVariableCommand(variables);

        DataProductVersion dataProductVersion1 = new DataProductVersion();
        dataProductVersion1.setUuid("dpv-uuid-123");

        DataProductVersion dataProductVersion2 = new DataProductVersion();
        dataProductVersion2.setUuid("dpv-uuid-456");

        DescriptorVariable savedVariable1 = new DescriptorVariable();
        savedVariable1.setSequenceId(1L);
        savedVariable1.setDataProductVersionUuid("dpv-uuid-123");
        savedVariable1.setVariableKey("key1");
        savedVariable1.setVariableValue("value1");

        DescriptorVariable savedVariable2 = new DescriptorVariable();
        savedVariable2.setSequenceId(2L);
        savedVariable2.setDataProductVersionUuid("dpv-uuid-456");
        savedVariable2.setVariableKey("key2");
        savedVariable2.setVariableValue("value2");

        when(persistencePort.findDataProductVersionByUuid("dpv-uuid-123")).thenReturn(dataProductVersion1);
        when(persistencePort.findDataProductVersionByUuid("dpv-uuid-456")).thenReturn(dataProductVersion2);
        when(persistencePort.createOrOverride(any(DescriptorVariable.class)))
                .thenReturn(savedVariable1)
                .thenReturn(savedVariable2);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        StoreDescriptorVariable useCase = new StoreDescriptorVariable(
                command, presenter, persistencePort, validationPort, transactionalPort);

        // When
        useCase.execute();

        // Then
        verify(transactionalPort, times(2)).doInTransaction(any(Runnable.class));
        verify(persistencePort).findDataProductVersionByUuid("dpv-uuid-123");
        verify(persistencePort).findDataProductVersionByUuid("dpv-uuid-456");
        verify(validationPort).validateVariablesCanBeAppliedToDescriptor(dataProductVersion1, List.of(variable1));
        verify(validationPort).validateVariablesCanBeAppliedToDescriptor(dataProductVersion2, List.of(variable2));
        verify(persistencePort, times(2)).createOrOverride(any(DescriptorVariable.class));
        verify(presenter).presentDescriptorVariableStored(savedVariable1);
        verify(presenter).presentDescriptorVariableStored(savedVariable2);
    }

    @Test
    void whenValidationFailsThenThrowBadRequestException() {
        // Given
        DescriptorVariable variable = new DescriptorVariable();
        variable.setDataProductVersionUuid("dpv-uuid-123");
        variable.setVariableKey("test-key");
        variable.setVariableValue("test-value");
        List<DescriptorVariable> variables = List.of(variable);
        StoreDescriptorVariableCommand command = new StoreDescriptorVariableCommand(variables);

        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("dpv-uuid-123");

        when(persistencePort.findDataProductVersionByUuid("dpv-uuid-123")).thenReturn(dataProductVersion);
        doThrow(new BadRequestException("Variable key 'test-key' is not valid for this descriptor"))
                .when(validationPort).validateVariablesCanBeAppliedToDescriptor(any(), any());

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        StoreDescriptorVariable useCase = new StoreDescriptorVariable(
                command, presenter, persistencePort, validationPort, transactionalPort);

        // When & Then
        assertThatThrownBy(useCase::execute)
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Variable key 'test-key' is not valid for this descriptor");

        verify(transactionalPort).doInTransaction(any(Runnable.class));
        verify(persistencePort).findDataProductVersionByUuid("dpv-uuid-123");
        verify(validationPort).validateVariablesCanBeAppliedToDescriptor(dataProductVersion, variables);
        verifyNoInteractions(presenter);
        verify(persistencePort, never()).createOrOverride(any(DescriptorVariable.class));
    }

    @Test
    void whenStoreSuccessfullyThenPresenterReceivesCorrectDescriptorVariable() {
        // Given
        DescriptorVariable variable = new DescriptorVariable();
        variable.setDataProductVersionUuid("dpv-uuid-123");
        variable.setVariableKey("test-key");
        variable.setVariableValue("test-value");
        List<DescriptorVariable> variables = List.of(variable);
        StoreDescriptorVariableCommand command = new StoreDescriptorVariableCommand(variables);

        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("dpv-uuid-123");

        DescriptorVariable savedVariable = new DescriptorVariable();
        savedVariable.setSequenceId(123L);
        savedVariable.setDataProductVersionUuid("dpv-uuid-123");
        savedVariable.setVariableKey("test-key");
        savedVariable.setVariableValue("test-value");

        when(persistencePort.findDataProductVersionByUuid("dpv-uuid-123")).thenReturn(dataProductVersion);
        when(persistencePort.createOrOverride(any(DescriptorVariable.class))).thenReturn(savedVariable);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        StoreDescriptorVariable useCase = new StoreDescriptorVariable(
                command, presenter, persistencePort, validationPort, transactionalPort);

        // When
        useCase.execute();

        // Then
        ArgumentCaptor<DescriptorVariable> captor = ArgumentCaptor.forClass(DescriptorVariable.class);
        verify(presenter).presentDescriptorVariableStored(captor.capture());
        DescriptorVariable presentedVariable = captor.getValue();
        assertThat(presentedVariable)
                .usingRecursiveComparison()
                .isEqualTo(savedVariable);
    }

    @Test
    void whenStoreSuccessfullyThenSaveReceivesCorrectDescriptorVariable() {
        // Given
        DescriptorVariable variable = new DescriptorVariable();
        variable.setDataProductVersionUuid("dpv-uuid-123");
        variable.setVariableKey("test-key");
        variable.setVariableValue("test-value");
        List<DescriptorVariable> variables = List.of(variable);
        StoreDescriptorVariableCommand command = new StoreDescriptorVariableCommand(variables);

        DataProductVersion dataProductVersion = new DataProductVersion();
        dataProductVersion.setUuid("dpv-uuid-123");

        DescriptorVariable savedVariable = new DescriptorVariable();
        savedVariable.setSequenceId(123L);
        savedVariable.setDataProductVersionUuid("dpv-uuid-123");
        savedVariable.setVariableKey("test-key");
        savedVariable.setVariableValue("test-value");

        when(persistencePort.findDataProductVersionByUuid("dpv-uuid-123")).thenReturn(dataProductVersion);
        when(persistencePort.createOrOverride(any(DescriptorVariable.class))).thenReturn(savedVariable);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionalPort).doInTransaction(any(Runnable.class));

        StoreDescriptorVariable useCase = new StoreDescriptorVariable(
                command, presenter, persistencePort, validationPort, transactionalPort);

        // When
        useCase.execute();

        // Then
        ArgumentCaptor<DescriptorVariable> captor = ArgumentCaptor.forClass(DescriptorVariable.class);
        verify(persistencePort).createOrOverride(captor.capture());
        DescriptorVariable savedVariableArg = captor.getValue();
        assertThat(savedVariableArg.getDataProductVersionUuid()).isEqualTo("dpv-uuid-123");
        assertThat(savedVariableArg.getVariableKey()).isEqualTo("test-key");
        assertThat(savedVariableArg.getVariableValue()).isEqualTo("test-value");
    }
}
