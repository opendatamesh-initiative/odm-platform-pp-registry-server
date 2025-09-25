package org.opendatamesh.platform.pp.registry.dataproduct.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.opendatamesh.platform.pp.registry.rest.v2.controllers.DataProductDescriptorController;
import org.springframework.http.HttpHeaders;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataProductDescriptorServiceTest {

    @Mock
    private DataProductsDescriptorService dataProductsDescriptorService;

    @InjectMocks
    private DataProductDescriptorController controller;

    private ObjectMapper objectMapper;
    private String testUuid;
    private JsonNode testDescriptor;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        testUuid = "test-uuid-123";
        testDescriptor = objectMapper.readTree("{\"name\":\"test-product\",\"version\":\"1.0.0\"}");
    }

    @Test
    void whenGetDescriptorWithValidUuidAndTagThenShouldReturnDescriptor() {
        // Given
        String tag = "v1.0.0";
        HttpHeaders headers = createValidHeaders();
        when(dataProductsDescriptorService.getDescriptor(eq(testUuid), any(GitReference.class), any(Credential.class)))
                .thenReturn(Optional.of(testDescriptor));

        // When
        Optional<JsonNode> response = controller.getDescriptor(testUuid, tag, null, null, headers);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get()).isEqualTo(testDescriptor);

        verify(dataProductsDescriptorService).getDescriptor(
                eq(testUuid),
                argThat(pointer -> pointer.getType() == GitReference.VersionType.TAG &&
                                 pointer.getValue().equals(tag)),
                any(Credential.class)
        );
    }

    @Test
    void whenGetDescriptorWithValidUuidAndBranchThenShouldReturnDescriptor() {
        // Given
        String branch = "main";
        HttpHeaders headers = createValidHeaders();
        when(dataProductsDescriptorService.getDescriptor(eq(testUuid), any(GitReference.class), any(Credential.class)))
                .thenReturn(Optional.of(testDescriptor));

        // When
        Optional<JsonNode> response = controller.getDescriptor(testUuid, null, branch, null, headers);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get()).isEqualTo(testDescriptor);

        verify(dataProductsDescriptorService).getDescriptor(
                eq(testUuid),
                argThat(pointer -> pointer.getType() == GitReference.VersionType.BRANCH &&
                                 pointer.getValue().equals(branch)),
                any(Credential.class)
        );
    }

    @Test
    void whenGetDescriptorWithValidUuidAndCommitThenShouldReturnDescriptor() {
        // Given
        String commit = "abc123def456";
        HttpHeaders headers = createValidHeaders();
        when(dataProductsDescriptorService.getDescriptor(eq(testUuid), any(GitReference.class), any(Credential.class)))
                .thenReturn(Optional.of(testDescriptor));

        // When
        Optional<JsonNode> response = controller.getDescriptor(testUuid, null, null, commit, headers);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get()).isEqualTo(testDescriptor);

        verify(dataProductsDescriptorService).getDescriptor(
                eq(testUuid),
                argThat(pointer -> pointer.getType() == GitReference.VersionType.COMMIT &&
                                 pointer.getValue().equals(commit)),
                any(Credential.class)
        );
    }

    @Test
    void whenGetDescriptorWithValidUuidAndNoVersionParamsThenShouldUseDefaultBranch() {
        // Given
        HttpHeaders headers = createValidHeaders();
        when(dataProductsDescriptorService.getDescriptor(eq(testUuid), any(GitReference.class), any(Credential.class)))
                .thenReturn(Optional.of(testDescriptor));

        // When
        Optional<JsonNode> response = controller.getDescriptor(testUuid, null, null, null, headers);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get()).isEqualTo(testDescriptor);

        verify(dataProductsDescriptorService).getDescriptor(
                eq(testUuid),
                argThat(pointer -> pointer.getType() == GitReference.VersionType.BRANCH &&
                                 pointer.getValue().equals("main")),
                any(Credential.class)
        );
    }

    @Test
    void whenGetDescriptorWithValidUuidButNoDescriptorFoundThenShouldReturnEmpty() {
        // Given
        String tag = "v1.0.0";
        HttpHeaders headers = createValidHeaders();
        when(dataProductsDescriptorService.getDescriptor(eq(testUuid), any(GitReference.class), any(Credential.class)))
                .thenReturn(Optional.empty());

        // When
        Optional<JsonNode> response = controller.getDescriptor(testUuid, tag, null, null, headers);

        // Then
        assertThat(response).isEmpty();

        verify(dataProductsDescriptorService).getDescriptor(
                eq(testUuid),
                any(GitReference.class),
                any(Credential.class)
        );
    }

    @Test
    void whenGetDescriptorWithValidUuidAndTagPriorityThenShouldUseTagOverBranch() {
        // Given
        String tag = "v1.0.0";
        String branch = "main";
        HttpHeaders headers = createValidHeaders();
        when(dataProductsDescriptorService.getDescriptor(eq(testUuid), any(GitReference.class), any(Credential.class)))
                .thenReturn(Optional.of(testDescriptor));

        // When
        Optional<JsonNode> response = controller.getDescriptor(testUuid, tag, branch, null, headers);

        // Then
        assertThat(response).isPresent();

        verify(dataProductsDescriptorService).getDescriptor(
                eq(testUuid),
                argThat(pointer -> pointer.getType() == GitReference.VersionType.TAG &&
                                 pointer.getValue().equals(tag)),
                any(Credential.class)
        );
    }

    @Test
    void whenGetDescriptorWithValidUuidAndTagPriorityOverCommitThenShouldUseTagOverCommit() {
        // Given
        String tag = "v1.0.0";
        String commit = "abc123def456";
        HttpHeaders headers = createValidHeaders();
        when(dataProductsDescriptorService.getDescriptor(eq(testUuid), any(GitReference.class), any(Credential.class)))
                .thenReturn(Optional.of(testDescriptor));

        // When
        Optional<JsonNode> response = controller.getDescriptor(testUuid, tag, null, commit, headers);

        // Then
        assertThat(response).isPresent();

        verify(dataProductsDescriptorService).getDescriptor(
                eq(testUuid),
                argThat(pointer -> pointer.getType() == GitReference.VersionType.TAG &&
                                 pointer.getValue().equals(tag)),
                any(Credential.class)
        );
    }

    @Test
    void whenGetDescriptorWithValidUuidAndBranchPriorityOverCommitThenShouldUseBranchOverCommit() {
        // Given
        String branch = "main";
        String commit = "abc123def456";
        HttpHeaders headers = createValidHeaders();
        when(dataProductsDescriptorService.getDescriptor(eq(testUuid), any(GitReference.class), any(Credential.class)))
                .thenReturn(Optional.of(testDescriptor));

        // When
        Optional<JsonNode> response = controller.getDescriptor(testUuid, null, branch, commit, headers);

        // Then
        assertThat(response).isPresent();

        verify(dataProductsDescriptorService).getDescriptor(
                eq(testUuid),
                argThat(pointer -> pointer.getType() == GitReference.VersionType.BRANCH &&
                                 pointer.getValue().equals(branch)),
                any(Credential.class)
        );
    }

    @Test
    void whenGetDescriptorWithValidUuidAndAllVersionParamsThenShouldUseTagAsHighestPriority() {
        // Given
        String tag = "v1.0.0";
        String branch = "main";
        String commit = "abc123def456";
        HttpHeaders headers = createValidHeaders();
        when(dataProductsDescriptorService.getDescriptor(eq(testUuid), any(GitReference.class), any(Credential.class)))
                .thenReturn(Optional.of(testDescriptor));

        // When
        Optional<JsonNode> response = controller.getDescriptor(testUuid, tag, branch, commit, headers);

        // Then
        assertThat(response).isPresent();

        verify(dataProductsDescriptorService).getDescriptor(
                eq(testUuid),
                argThat(pointer -> pointer.getType() == GitReference.VersionType.TAG &&
                                 pointer.getValue().equals(tag)),
                any(Credential.class)
        );
    }

    @Test
    void whenGetDescriptorWithValidUuidAndPatCredentialThenShouldCreateCorrectCredential() {
        // Given
        String tag = "v1.0.0";
        HttpHeaders headers = createValidHeaders();
        when(dataProductsDescriptorService.getDescriptor(eq(testUuid), any(GitReference.class), any(Credential.class)))
                .thenReturn(Optional.of(testDescriptor));

        // When
        controller.getDescriptor(testUuid, tag, null, null, headers);

        // Then
        verify(dataProductsDescriptorService).getDescriptor(
                eq(testUuid),
                any(GitReference.class),
                argThat(credential -> credential instanceof PatCredential)
        );
    }

    @Test
    void whenGetDescriptorWithValidUuidAndPatCredentialWithUsernameThenShouldCreateCorrectCredential() {
        // Given
        String tag = "v1.0.0";
        HttpHeaders headers = createValidHeadersWithUsername();
        when(dataProductsDescriptorService.getDescriptor(eq(testUuid), any(GitReference.class), any(Credential.class)))
                .thenReturn(Optional.of(testDescriptor));

        // When
        controller.getDescriptor(testUuid, tag, null, null, headers);

        // Then
        verify(dataProductsDescriptorService).getDescriptor(
                eq(testUuid),
                any(GitReference.class),
                argThat(credential -> credential instanceof PatCredential)
        );
    }

    @Test
    void whenGetDescriptorWithMissingAuthTypeHeaderThenShouldThrowException() {
        // Given
        String tag = "v1.0.0";
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-odm-gpauth-param-token", "test-token");

        // When & Then
        assertThatThrownBy(() -> controller.getDescriptor(testUuid, tag, null, null, headers))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Missing or invalid credentials in headers");

        verify(dataProductsDescriptorService, never()).getDescriptor(any(), any(), any());
    }

    @Test
    void whenGetDescriptorWithUnsupportedAuthTypeThenShouldThrowException() {
        // Given
        String tag = "v1.0.0";
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-odm-gpauth-type", "UNSUPPORTED");
        headers.add("x-odm-gpauth-param-token", "test-token");

        // When & Then
        assertThatThrownBy(() -> controller.getDescriptor(testUuid, tag, null, null, headers))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Missing or invalid credentials in headers");

        verify(dataProductsDescriptorService, never()).getDescriptor(any(), any(), any());
    }

    @Test
    void whenGetDescriptorWithServiceThrowingExceptionThenShouldPropagateException() {
        // Given
        String tag = "v1.0.0";
        HttpHeaders headers = createValidHeaders();
        when(dataProductsDescriptorService.getDescriptor(eq(testUuid), any(GitReference.class), any(Credential.class)))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThatThrownBy(() -> controller.getDescriptor(testUuid, tag, null, null, headers))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Service error");
    }

    private HttpHeaders createValidHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-odm-gpauth-type", "PAT");
        headers.add("x-odm-gpauth-param-token", "test-token");
        return headers;
    }

    private HttpHeaders createValidHeadersWithUsername() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-odm-gpauth-type", "PAT");
        headers.add("x-odm-gpauth-param-username", "test-user");
        headers.add("x-odm-gpauth-param-token", "test-token");
        return headers;
    }
}