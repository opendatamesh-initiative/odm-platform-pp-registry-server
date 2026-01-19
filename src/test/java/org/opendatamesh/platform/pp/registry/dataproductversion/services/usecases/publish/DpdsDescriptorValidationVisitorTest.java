package org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.dpds.model.components.Components;
import org.opendatamesh.dpds.model.core.StandardDefinition;
import org.opendatamesh.dpds.model.info.Info;
import org.opendatamesh.dpds.model.info.Owner;
import org.opendatamesh.dpds.model.interfaces.Expectations;
import org.opendatamesh.dpds.model.interfaces.InterfaceComponents;
import org.opendatamesh.dpds.model.interfaces.Obligations;
import org.opendatamesh.dpds.model.interfaces.Port;
import org.opendatamesh.dpds.model.interfaces.Promises;
import org.opendatamesh.dpds.model.internals.ApplicationComponent;
import org.opendatamesh.dpds.model.internals.InfrastructuralComponent;
import org.opendatamesh.dpds.model.internals.InternalComponents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DpdsDescriptorValidationVisitorTest {

    private static final String VALID_DATA_PRODUCT_FQN = "urn:dpds:it.quantyca:dataproducts:tripExecution:1";
    private static final String VALID_NAME = "testComponent";
    private static final String VALID_VERSION = "1.0.0";
    private static final String VALID_DOMAIN = "testDomain";
    private static final String VALID_OWNER_ID = "owner@example.com";

    // ========== Info Object Validation Tests ==========

    @Test
    void whenInfoIsNullThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Info info = null;

        // When
        visitor.visit(info);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().equals("info") && 
            error.getMessage().equals("Info section is null"));
    }

    @Test
    void whenInfoFullyQualifiedNameIsMissingThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Info info = createInfo();
        info.setFullyQualifiedName(null);

        // When
        info.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().equals("info.fullyQualifiedName"));
    }

    @Test
    void whenInfoFullyQualifiedNameIsPresentThenCacheIt() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Info info = createInfo();
        String fqn = VALID_DATA_PRODUCT_FQN;
        info.setFullyQualifiedName(fqn);

        // When
        info.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isFalse();
        // Verify FQN is cached by checking that a port can use it
        Port port = createPort("testPort");
        port.setFullyQualifiedName(null);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setOutputPorts(List.of(port));
        interfaceComponents.accept(visitor);
        
        // Port should have generated FQN using cached dataProductFqn
        assertThat(port.getFullyQualifiedName()).isNotNull();
        assertThat(port.getFullyQualifiedName()).contains(VALID_DATA_PRODUCT_FQN);
    }

    @Test
    void whenInfoEntityTypeIsMissingThenAutoSetToDataproduct() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Info info = createInfo();
        info.setEntityType(null);

        // When
        info.accept(visitor);

        // Then
        assertThat(info.getEntityType()).isEqualTo("dataproduct");
        assertThat(context.hasErrors()).isFalse();
    }

    @Test
    void whenInfoEntityTypeIsIncorrectThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Info info = createInfo();
        info.setEntityType("wrongType");

        // When
        info.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().equals("info.entityType") &&
            error.getMessage().contains("Invalid entityType"));
    }

    @Test
    void whenInfoEntityTypeIsCorrectThenNoError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Info info = createInfo();
        info.setEntityType("dataproduct");

        // When
        info.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isFalse();
        assertThat(info.getEntityType()).isEqualTo("dataproduct");
    }

    @Test
    void whenInfoIdIsMissingThenGenerateFromFqn() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Info info = createInfo();
        info.setId(null);

        // When
        info.accept(visitor);

        // Then
        assertThat(info.getId()).isNotNull();
        // Verify ID is generated from FQN (UUID v5)
        UUID expectedUuid = UUID.nameUUIDFromBytes(VALID_DATA_PRODUCT_FQN.getBytes());
        assertThat(info.getId()).isEqualTo(expectedUuid.toString());
    }

    @Test
    void whenInfoIdIsPresentThenDoNotModify() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Info info = createInfo();
        String existingId = "existing-id-123";
        info.setId(existingId);

        // When
        info.accept(visitor);

        // Then
        assertThat(info.getId()).isEqualTo(existingId);
    }

    @Test
    void whenInfoNameIsMissingThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Info info = createInfo();
        info.setName(null);

        // When
        info.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().equals("info.name"));
    }

    @Test
    void whenInfoVersionIsMissingThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Info info = createInfo();
        info.setVersion(null);

        // When
        info.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().equals("info.version"));
    }

    @Test
    void whenInfoVersionIsInvalidThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Info info = createInfo();
        info.setVersion("invalid-version");

        // When
        info.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().equals("info.version") &&
            error.getMessage().contains("semantic versioning"));
    }

    @Test
    void whenInfoDomainIsMissingThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Info info = createInfo();
        info.setDomain(null);

        // When
        info.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().equals("info.domain"));
    }

    @Test
    void whenInfoOwnerIsNullThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Info info = createInfo();
        info.setOwner(null);

        // When
        info.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().equals("info.owner"));
    }

    @Test
    void whenInfoOwnerIsPresentThenValidateOwner() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Info info = createInfo();
        Owner owner = new Owner();
        owner.setId(null); // Missing required field
        info.setOwner(owner);

        // When
        info.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().equals("info.owner.id"));
    }

    // ========== Owner Object Validation Tests ==========

    @Test
    void whenOwnerIsNullThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Owner owner = null;

        // When
        visitor.visit(owner);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().equals("info.owner") &&
            error.getMessage().equals("Owner section is null"));
    }

    @Test
    void whenOwnerIdIsMissingThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Owner owner = new Owner();
        owner.setId(null);

        // When
        owner.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().equals("info.owner.id"));
    }

    // ========== Port Object Validation Tests ==========

    @Test
    void whenPortIsNullThenReturnEarly() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Port port = null;

        // When
        visitor.visit(port);

        // Then
        assertThat(context.hasErrors()).isFalse();
    }

    @Test
    void whenPortNameIsMissingThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        Port port = createPort("testPort");
        port.setName(null);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setInputPorts(List.of(port));
        interfaceComponents.setOutputPorts(new ArrayList<>());

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().contains(".name"));
    }

    @Test
    void whenPortVersionIsMissingThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        Port port = createPort("testPort");
        port.setVersion(null);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setInputPorts(List.of(port));
        interfaceComponents.setOutputPorts(new ArrayList<>());

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().contains(".version"));
    }

    @Test
    void whenPortVersionIsInvalidThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        Port port = createPort("testPort");
        port.setVersion("invalid");
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setInputPorts(List.of(port));
        interfaceComponents.setOutputPorts(new ArrayList<>());

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().contains(".version") &&
            error.getMessage().contains("semantic versioning"));
    }

    @Test
    void whenInputPortEntityTypeIsMissingThenAutoSetToInputport() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        Port port = createPort("testPort");
        port.setEntityType(null);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setInputPorts(List.of(port));
        interfaceComponents.setOutputPorts(new ArrayList<>());

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(port.getEntityType()).isEqualTo("inputport");
        assertThat(context.hasErrors()).isFalse();
    }

    @Test
    void whenInputPortEntityTypeIsIncorrectThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        Port port = createPort("testPort");
        port.setEntityType("wrongType");
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setInputPorts(List.of(port));
        interfaceComponents.setOutputPorts(new ArrayList<>());

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().contains(".entityType") &&
            error.getMessage().contains("Invalid entityType"));
    }

    @Test
    void whenOutputPortEntityTypeIsMissingThenAutoSetToOutputport() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        Port port = createPort("testPort");
        port.setEntityType(null);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setOutputPorts(List.of(port));

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(port.getEntityType()).isEqualTo("outputport");
        assertThat(context.hasErrors()).isFalse();
    }

    @Test
    void whenPortFullyQualifiedNameIsMissingThenGenerateFromDataProductFqn() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        Port port = createPort("testPort");
        port.setFullyQualifiedName(null);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setInputPorts(List.of(port));
        interfaceComponents.setOutputPorts(new ArrayList<>());

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(port.getFullyQualifiedName()).isNotNull();
        assertThat(port.getFullyQualifiedName()).isEqualTo(
            VALID_DATA_PRODUCT_FQN + ":inputports:testPort");
    }

    @Test
    void whenPortFullyQualifiedNameIsPresentThenDoNotModify() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        Port port = createPort("testPort");
        String existingFqn = "urn:dpds:existing:fqn";
        port.setFullyQualifiedName(existingFqn);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setInputPorts(List.of(port));
        interfaceComponents.setOutputPorts(new ArrayList<>());

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(port.getFullyQualifiedName()).isEqualTo(existingFqn);
    }

    @Test
    void whenPortIdIsMissingThenGenerateFromFqn() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        Port port = createPort("testPort");
        port.setId(null);
        port.setFullyQualifiedName(null);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setInputPorts(List.of(port));
        interfaceComponents.setOutputPorts(new ArrayList<>());

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(port.getId()).isNotNull();
        String expectedFqn = VALID_DATA_PRODUCT_FQN + ":inputports:testPort";
        UUID expectedUuid = UUID.nameUUIDFromBytes(expectedFqn.getBytes());
        assertThat(port.getId()).isEqualTo(expectedUuid.toString());
    }

    @Test
    void whenPortIdIsPresentThenDoNotModify() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        Port port = createPort("testPort");
        String existingId = "existing-id-123";
        port.setId(existingId);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setInputPorts(List.of(port));
        interfaceComponents.setOutputPorts(new ArrayList<>());

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(port.getId()).isEqualTo(existingId);
    }

    @Test
    void whenPortPromisesIsPresentThenVisitPromises() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        Port port = createPort("testPort");
        Promises promises = new Promises();
        StandardDefinition api = createStandardDefinitionWithDefinition("testApi");
        api.setEntityType(null);
        promises.setApi(api);
        port.setPromises(promises);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setOutputPorts(List.of(port));

        // When
        interfaceComponents.accept(visitor);

        // Then
        // Verify that StandardDefinition was visited (entityType should be set if missing)
        assertThat(api.getEntityType()).isEqualTo("api");
    }

    @Test
    void whenPortExpectationsIsPresentThenVisitExpectations() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        Port port = createPort("testPort");
        Expectations expectations = new Expectations();
        StandardDefinition audience = createStandardDefinitionWithDefinition("testAudience");
        audience.setEntityType(null);
        expectations.setAudience(audience);
        port.setExpectations(expectations);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setOutputPorts(List.of(port));

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(audience.getEntityType()).isEqualTo("api");
    }

    @Test
    void whenPortObligationsIsPresentThenVisitObligations() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        Port port = createPort("testPort");
        Obligations obligations = new Obligations();
        StandardDefinition sla = createStandardDefinitionWithDefinition("testSla");
        sla.setEntityType(null);
        obligations.setSla(sla);
        port.setObligations(obligations);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setOutputPorts(List.of(port));

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(sla.getEntityType()).isEqualTo("api");
    }

    @Test
    void whenDuplicateInputPortNameThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        Port port1 = createPort("duplicateName");
        Port port2 = createPort("duplicateName");
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setInputPorts(List.of(port1, port2));
        interfaceComponents.setOutputPorts(new ArrayList<>());

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getMessage().contains("not unique") &&
            error.getMessage().contains("inputPorts"));
    }

    @Test
    void whenDuplicateOutputPortNameThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        Port port1 = createPort("duplicateName");
        Port port2 = createPort("duplicateName");
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setOutputPorts(List.of(port1, port2));

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getMessage().contains("not unique") &&
            error.getMessage().contains("outputPorts"));
    }

    // ========== ApplicationComponent Validation Tests ==========

    @Test
    void whenApplicationComponentIsNullThenReturnEarly() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        ApplicationComponent component = null;

        // When
        visitor.visit(component);

        // Then
        assertThat(context.hasErrors()).isFalse();
    }

    @Test
    void whenApplicationComponentNameIsMissingThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        ApplicationComponent component = createApplicationComponent("testApp");
        component.setName(null);
        InternalComponents internalComponents = new InternalComponents();
        internalComponents.setApplicationComponents(List.of(component));

        // When
        internalComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().contains(".name"));
    }

    @Test
    void whenApplicationComponentVersionIsMissingThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        ApplicationComponent component = createApplicationComponent("testApp");
        component.setVersion(null);
        InternalComponents internalComponents = new InternalComponents();
        internalComponents.setApplicationComponents(List.of(component));

        // When
        internalComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().contains(".version"));
    }

    @Test
    void whenApplicationComponentVersionIsInvalidThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        ApplicationComponent component = createApplicationComponent("testApp");
        component.setVersion("invalid");
        InternalComponents internalComponents = new InternalComponents();
        internalComponents.setApplicationComponents(List.of(component));

        // When
        internalComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().contains(".version") &&
            error.getMessage().contains("semantic versioning"));
    }

    @Test
    void whenApplicationComponentEntityTypeIsMissingThenAutoSetToApplication() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        ApplicationComponent component = createApplicationComponent("testApp");
        component.setEntityType(null);
        InternalComponents internalComponents = new InternalComponents();
        internalComponents.setApplicationComponents(List.of(component));

        // When
        internalComponents.accept(visitor);

        // Then
        assertThat(component.getEntityType()).isEqualTo("application");
        assertThat(context.hasErrors()).isFalse();
    }

    @Test
    void whenApplicationComponentEntityTypeIsIncorrectThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        ApplicationComponent component = createApplicationComponent("testApp");
        component.setEntityType("wrongType");
        InternalComponents internalComponents = new InternalComponents();
        internalComponents.setApplicationComponents(List.of(component));

        // When
        internalComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().contains(".entityType") &&
            error.getMessage().contains("Invalid entityType"));
    }

    @Test
    void whenApplicationComponentFullyQualifiedNameIsMissingThenGenerate() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        ApplicationComponent component = createApplicationComponent("testApp");
        component.setFullyQualifiedName(null);
        InternalComponents internalComponents = new InternalComponents();
        internalComponents.setApplicationComponents(List.of(component));

        // When
        internalComponents.accept(visitor);

        // Then
        assertThat(component.getFullyQualifiedName()).isNotNull();
        assertThat(component.getFullyQualifiedName()).isEqualTo(
            VALID_DATA_PRODUCT_FQN + ":applications:testApp");
    }

    @Test
    void whenApplicationComponentIdIsMissingThenGenerateFromFqn() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        ApplicationComponent component = createApplicationComponent("testApp");
        component.setId(null);
        component.setFullyQualifiedName(null);
        InternalComponents internalComponents = new InternalComponents();
        internalComponents.setApplicationComponents(List.of(component));

        // When
        internalComponents.accept(visitor);

        // Then
        assertThat(component.getId()).isNotNull();
        String expectedFqn = VALID_DATA_PRODUCT_FQN + ":applications:testApp";
        UUID expectedUuid = UUID.nameUUIDFromBytes(expectedFqn.getBytes());
        assertThat(component.getId()).isEqualTo(expectedUuid.toString());
    }

    @Test
    void whenDuplicateApplicationComponentNameThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        ApplicationComponent component1 = createApplicationComponent("duplicateName");
        ApplicationComponent component2 = createApplicationComponent("duplicateName");
        InternalComponents internalComponents = new InternalComponents();
        internalComponents.setApplicationComponents(List.of(component1, component2));

        // When
        internalComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getMessage().contains("not unique") &&
            error.getMessage().contains("applicationComponents"));
    }

    // ========== InfrastructuralComponent Validation Tests ==========

    @Test
    void whenInfrastructuralComponentIsNullThenReturnEarly() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        InfrastructuralComponent component = null;

        // When
        visitor.visit(component);

        // Then
        assertThat(context.hasErrors()).isFalse();
    }

    @Test
    void whenInfrastructuralComponentNameIsMissingThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        InfrastructuralComponent component = createInfrastructuralComponent("testInfra");
        component.setName(null);
        InternalComponents internalComponents = new InternalComponents();
        internalComponents.setInfrastructuralComponents(List.of(component));

        // When
        internalComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().contains(".name"));
    }

    @Test
    void whenInfrastructuralComponentVersionIsMissingThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        InfrastructuralComponent component = createInfrastructuralComponent("testInfra");
        component.setVersion(null);
        InternalComponents internalComponents = new InternalComponents();
        internalComponents.setInfrastructuralComponents(List.of(component));

        // When
        internalComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().contains(".version"));
    }

    @Test
    void whenInfrastructuralComponentVersionIsInvalidThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        InfrastructuralComponent component = createInfrastructuralComponent("testInfra");
        component.setVersion("invalid");
        InternalComponents internalComponents = new InternalComponents();
        internalComponents.setInfrastructuralComponents(List.of(component));

        // When
        internalComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().contains(".version") &&
            error.getMessage().contains("semantic versioning"));
    }

    @Test
    void whenInfrastructuralComponentEntityTypeIsMissingThenAutoSetToInfrastructure() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        InfrastructuralComponent component = createInfrastructuralComponent("testInfra");
        component.setEntityType(null);
        InternalComponents internalComponents = new InternalComponents();
        internalComponents.setInfrastructuralComponents(List.of(component));

        // When
        internalComponents.accept(visitor);

        // Then
        assertThat(component.getEntityType()).isEqualTo("infrastructure");
        assertThat(context.hasErrors()).isFalse();
    }

    @Test
    void whenInfrastructuralComponentEntityTypeIsIncorrectThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        InfrastructuralComponent component = createInfrastructuralComponent("testInfra");
        component.setEntityType("wrongType");
        InternalComponents internalComponents = new InternalComponents();
        internalComponents.setInfrastructuralComponents(List.of(component));

        // When
        internalComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().contains(".entityType") &&
            error.getMessage().contains("Invalid entityType"));
    }

    @Test
    void whenInfrastructuralComponentFullyQualifiedNameIsMissingThenGenerate() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        InfrastructuralComponent component = createInfrastructuralComponent("testInfra");
        component.setFullyQualifiedName(null);
        InternalComponents internalComponents = new InternalComponents();
        internalComponents.setInfrastructuralComponents(List.of(component));

        // When
        internalComponents.accept(visitor);

        // Then
        assertThat(component.getFullyQualifiedName()).isNotNull();
        assertThat(component.getFullyQualifiedName()).isEqualTo(
            VALID_DATA_PRODUCT_FQN + ":infrastructure:testInfra");
    }

    @Test
    void whenInfrastructuralComponentIdIsMissingThenGenerateFromFqn() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        InfrastructuralComponent component = createInfrastructuralComponent("testInfra");
        component.setId(null);
        component.setFullyQualifiedName(null);
        InternalComponents internalComponents = new InternalComponents();
        internalComponents.setInfrastructuralComponents(List.of(component));

        // When
        internalComponents.accept(visitor);

        // Then
        assertThat(component.getId()).isNotNull();
        String expectedFqn = VALID_DATA_PRODUCT_FQN + ":infrastructure:testInfra";
        UUID expectedUuid = UUID.nameUUIDFromBytes(expectedFqn.getBytes());
        assertThat(component.getId()).isEqualTo(expectedUuid.toString());
    }

    @Test
    void whenDuplicateInfrastructuralComponentNameThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        InfrastructuralComponent component1 = createInfrastructuralComponent("duplicateName");
        InfrastructuralComponent component2 = createInfrastructuralComponent("duplicateName");
        InternalComponents internalComponents = new InternalComponents();
        internalComponents.setInfrastructuralComponents(List.of(component1, component2));

        // When
        internalComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getMessage().contains("not unique") &&
            error.getMessage().contains("infrastructuralComponents"));
    }

    // ========== StandardDefinition Validation Tests ==========

    @Test
    void whenStandardDefinitionIsNullThenReturnEarly() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        StandardDefinition standardDefinition = null;

        // When
        visitor.visit(standardDefinition);

        // Then
        assertThat(context.hasErrors()).isFalse();
    }

    @Test
    void whenStandardDefinitionNameIsMissingThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        StandardDefinition standardDefinition = createStandardDefinition("testApi");
        standardDefinition.setName(null);
        Port port = createPort("testPort");
        Promises promises = new Promises();
        promises.setApi(standardDefinition);
        port.setPromises(promises);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setOutputPorts(List.of(port));

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().contains(".name"));
    }

    @Test
    void whenStandardDefinitionVersionIsMissingThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        StandardDefinition standardDefinition = createStandardDefinition("testApi");
        standardDefinition.setVersion(null);
        Port port = createPort("testPort");
        Promises promises = new Promises();
        promises.setApi(standardDefinition);
        port.setPromises(promises);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setOutputPorts(List.of(port));

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().contains(".version"));
    }

    @Test
    void whenStandardDefinitionSpecificationIsMissingThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        StandardDefinition standardDefinition = createStandardDefinition("testApi");
        standardDefinition.setSpecification(null);
        Port port = createPort("testPort");
        Promises promises = new Promises();
        promises.setApi(standardDefinition);
        port.setPromises(promises);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setOutputPorts(List.of(port));

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().contains(".specification"));
    }

    @Test
    void whenStandardDefinitionDefinitionIsMissingThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        StandardDefinition standardDefinition = createStandardDefinition("testApi");
        standardDefinition.setDefinition(null);
        Port port = createPort("testPort");
        Promises promises = new Promises();
        promises.setApi(standardDefinition);
        port.setPromises(promises);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setOutputPorts(List.of(port));

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().contains(".definition"));
    }

    @Test
    void whenStandardDefinitionEntityTypeIsMissingInApiContextThenAutoSetToApi() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        StandardDefinition standardDefinition = createStandardDefinitionWithDefinition("testApi");
        standardDefinition.setEntityType(null);
        Port port = createPort("testPort");
        Promises promises = new Promises();
        promises.setApi(standardDefinition);
        port.setPromises(promises);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setOutputPorts(List.of(port));

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(standardDefinition.getEntityType()).isEqualTo("api");
        assertThat(context.hasErrors()).isFalse();
    }

    @Test
    void whenStandardDefinitionEntityTypeIsIncorrectThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        StandardDefinition standardDefinition = createStandardDefinitionWithDefinition("testApi");
        standardDefinition.setEntityType("wrongType");
        Port port = createPort("testPort");
        Promises promises = new Promises();
        promises.setApi(standardDefinition);
        port.setPromises(promises);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setOutputPorts(List.of(port));

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().contains(".entityType") &&
            error.getMessage().contains("Invalid entityType"));
    }

    @Test
    void whenStandardDefinitionFullyQualifiedNameIsMissingThenGenerate() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        StandardDefinition standardDefinition = createStandardDefinitionWithDefinition("testApi");
        standardDefinition.setFullyQualifiedName(null);
        Port port = createPort("testPort");
        Promises promises = new Promises();
        promises.setApi(standardDefinition);
        port.setPromises(promises);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setOutputPorts(List.of(port));

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(standardDefinition.getFullyQualifiedName()).isNotNull();
        assertThat(standardDefinition.getFullyQualifiedName()).contains("urn:dpds:it.quantyca:apis:testApi:");
    }

    @Test
    void whenStandardDefinitionIdIsMissingThenGenerateFromFqn() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        StandardDefinition standardDefinition = createStandardDefinitionWithDefinition("testApi");
        standardDefinition.setId(null);
        standardDefinition.setFullyQualifiedName(null);
        Port port = createPort("testPort");
        Promises promises = new Promises();
        promises.setApi(standardDefinition);
        port.setPromises(promises);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setOutputPorts(List.of(port));

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(standardDefinition.getId()).isNotNull();
        // Verify ID matches generated FQN
        String generatedFqn = standardDefinition.getFullyQualifiedName();
        UUID expectedUuid = UUID.nameUUIDFromBytes(generatedFqn.getBytes());
        assertThat(standardDefinition.getId()).isEqualTo(expectedUuid.toString());
    }

    // ========== Promises/Expectations/Obligations Tests ==========

    @Test
    void whenPromisesIsNullThenReturnEarly() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Promises promises = null;

        // When
        visitor.visit(promises);

        // Then
        assertThat(context.hasErrors()).isFalse();
    }

    @Test
    void whenPromisesApiIsPresentThenVisitStandardDefinition() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        Port port = createPort("testPort");
        Promises promises = new Promises();
        StandardDefinition api = createStandardDefinition("testApi");
        api.setEntityType(null);
        promises.setApi(api);
        port.setPromises(promises);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setOutputPorts(List.of(port));

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(api.getEntityType()).isEqualTo("api");
    }

    @Test
    void whenExpectationsIsNullThenReturnEarly() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Expectations expectations = null;

        // When
        visitor.visit(expectations);

        // Then
        assertThat(context.hasErrors()).isFalse();
    }

    @Test
    void whenExpectationsAudienceIsPresentThenVisitStandardDefinition() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        Port port = createPort("testPort");
        Expectations expectations = new Expectations();
        StandardDefinition audience = createStandardDefinition("testAudience");
        audience.setEntityType(null);
        expectations.setAudience(audience);
        port.setExpectations(expectations);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setOutputPorts(List.of(port));

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(audience.getEntityType()).isEqualTo("api");
    }

    @Test
    void whenObligationsIsNullThenReturnEarly() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Obligations obligations = null;

        // When
        visitor.visit(obligations);

        // Then
        assertThat(context.hasErrors()).isFalse();
    }

    @Test
    void whenObligationsSlaIsPresentThenVisitStandardDefinition() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        Port port = createPort("testPort");
        Obligations obligations = new Obligations();
        StandardDefinition sla = createStandardDefinition("testSla");
        sla.setEntityType(null);
        obligations.setSla(sla);
        port.setObligations(obligations);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setOutputPorts(List.of(port));

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(sla.getEntityType()).isEqualTo("api");
    }

    // ========== InterfaceComponents Validation Tests ==========

    @Test
    void whenInterfaceComponentsIsNullThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        InterfaceComponents interfaceComponents = null;

        // When
        visitor.visit(interfaceComponents);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().equals("interfaceComponents") &&
            error.getMessage().equals("InterfaceComponents section is null"));
    }

    @Test
    void whenOutputPortsIsNullThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setOutputPorts(null);

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getFieldPath().equals("interfaceComponents.outputPorts"));
    }

    @Test
    void whenOutputPortsIsEmptyThenNoError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        interfaceComponents.setOutputPorts(new ArrayList<>());

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isFalse();
    }

    @Test
    void whenInputPortsIsPresentThenValidateEachPort() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        Port port1 = createPort("port1");
        Port port2 = createPort("port2");
        interfaceComponents.setInputPorts(List.of(port1, port2));
        interfaceComponents.setOutputPorts(new ArrayList<>());

        // When
        interfaceComponents.accept(visitor);

        // Then
        // Both ports should have entityType set
        assertThat(port1.getEntityType()).isEqualTo("inputport");
        assertThat(port2.getEntityType()).isEqualTo("inputport");
    }

    @Test
    void whenMultiplePortTypesArePresentThenValidateAll() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        InterfaceComponents interfaceComponents = new InterfaceComponents();
        Port inputPort = createPort("inputPort");
        Port outputPort = createPort("outputPort");
        Port discoveryPort = createPort("discoveryPort");
        interfaceComponents.setInputPorts(List.of(inputPort));
        interfaceComponents.setOutputPorts(List.of(outputPort));
        interfaceComponents.setDiscoveryPorts(List.of(discoveryPort));

        // When
        interfaceComponents.accept(visitor);

        // Then
        assertThat(inputPort.getEntityType()).isEqualTo("inputport");
        assertThat(outputPort.getEntityType()).isEqualTo("outputport");
        assertThat(discoveryPort.getEntityType()).isEqualTo("discoveryport");
    }

    // ========== InternalComponents Validation Tests ==========

    @Test
    void whenInternalComponentsIsNullThenReturnEarly() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        InternalComponents internalComponents = null;

        // When
        visitor.visit(internalComponents);

        // Then
        assertThat(context.hasErrors()).isFalse();
    }

    @Test
    void whenApplicationComponentsIsPresentThenValidateEach() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        InternalComponents internalComponents = new InternalComponents();
        ApplicationComponent app1 = createApplicationComponent("app1");
        ApplicationComponent app2 = createApplicationComponent("app2");
        internalComponents.setApplicationComponents(List.of(app1, app2));

        // When
        internalComponents.accept(visitor);

        // Then
        assertThat(app1.getEntityType()).isEqualTo("application");
        assertThat(app2.getEntityType()).isEqualTo("application");
    }

    @Test
    void whenInfrastructuralComponentsIsPresentThenValidateEach() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        setupDataProductFqn(visitor);
        InternalComponents internalComponents = new InternalComponents();
        InfrastructuralComponent infra1 = createInfrastructuralComponent("infra1");
        InfrastructuralComponent infra2 = createInfrastructuralComponent("infra2");
        internalComponents.setInfrastructuralComponents(List.of(infra1, infra2));

        // When
        internalComponents.accept(visitor);

        // Then
        assertThat(infra1.getEntityType()).isEqualTo("infrastructure");
        assertThat(infra2.getEntityType()).isEqualTo("infrastructure");
    }

    // ========== Components Map Validation Tests ==========

    @Test
    void whenComponentsIsNullThenReturnEarly() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Components components = null;

        // When
        visitor.visit(components);

        // Then
        assertThat(context.hasErrors()).isFalse();
    }

    @Test
    void whenComponentKeyIsInvalidFormatThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Components components = new Components();
        Map<String, Port> inputPorts = new HashMap<>();
        inputPorts.put("invalid key with spaces", createPort("testPort"));
        components.setInputPorts(inputPorts);

        // When
        components.accept(visitor);

        // Then
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getMessage().contains("does not match required format"));
    }

    @Test
    void whenDuplicateComponentKeyThenAddError() {
        // Given
        DpdsDescriptorValidationContext context = new DpdsDescriptorValidationContext();
        DpdsDescriptorValidationVisitor visitor = new DpdsDescriptorValidationVisitor(context);
        Components components = new Components();
        Map<String, Port> inputPorts = new HashMap<>();
        inputPorts.put("validKey1", createPort("port1"));
        inputPorts.put("validKey2", createPort("port2"));
        components.setInputPorts(inputPorts);

        // When
        components.accept(visitor);

        // Then
        // Visit again with same key to test uniqueness validation
        Components components2 = new Components();
        Map<String, Port> inputPorts2 = new HashMap<>();
        inputPorts2.put("validKey1", createPort("port3")); // Same key as before
        components2.setInputPorts(inputPorts2);
        components2.accept(visitor);

        // The context tracks keys per map type, so same key in same map type should be caught
        assertThat(context.hasErrors()).isTrue();
        assertThat(context.getErrors()).anyMatch(error -> 
            error.getMessage().contains("not unique") &&
            error.getMessage().contains("components.inputPorts"));
    }

    // ========== Helper Methods ==========

    private Info createInfo() {
        Info info = new Info();
        info.setFullyQualifiedName(VALID_DATA_PRODUCT_FQN);
        info.setName(VALID_NAME);
        info.setVersion(VALID_VERSION);
        info.setDomain(VALID_DOMAIN);
        Owner owner = new Owner();
        owner.setId(VALID_OWNER_ID);
        info.setOwner(owner);
        return info;
    }

    private Port createPort(String name) {
        Port port = new Port();
        port.setName(name);
        port.setVersion(VALID_VERSION);
        return port;
    }

    private ApplicationComponent createApplicationComponent(String name) {
        ApplicationComponent component = new ApplicationComponent();
        component.setName(name);
        component.setVersion(VALID_VERSION);
        return component;
    }

    private InfrastructuralComponent createInfrastructuralComponent(String name) {
        InfrastructuralComponent component = new InfrastructuralComponent();
        component.setName(name);
        component.setVersion(VALID_VERSION);
        return component;
    }

    private StandardDefinition createStandardDefinition(String name) {
        StandardDefinition standardDefinition = new StandardDefinition();
        standardDefinition.setName(name);
        standardDefinition.setVersion(VALID_VERSION);
        standardDefinition.setSpecification("test-spec");
        // Definition is left as null - tests that need a valid definition can set it
        // The visitor only checks if definition is null, so this is fine for most tests
        return standardDefinition;
    }
    
    private StandardDefinition createStandardDefinitionWithDefinition(String name) {
        StandardDefinition standardDefinition = createStandardDefinition(name);
        // Create a mock ComponentBase using a simple approach
        // Since we can't easily create ComponentBase, we'll use a workaround:
        // Create a Port object (which extends ComponentBase) as a placeholder
        // This is a bit of a hack, but it works for testing purposes
        Port definitionPlaceholder = new Port();
        definitionPlaceholder.setName("definition-placeholder");
        standardDefinition.setDefinition(definitionPlaceholder);
        return standardDefinition;
    }

    private void setupDataProductFqn(DpdsDescriptorValidationVisitor visitor) {
        Info info = createInfo();
        // Visit info to cache the FQN
        info.accept(visitor);
    }
}