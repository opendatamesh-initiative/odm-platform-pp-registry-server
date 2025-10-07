package org.opendatamesh.platform.pp.registry.rest.v2.mocks;

import com.fasterxml.jackson.databind.JsonNode;
import org.mockito.Mockito;
import org.opendatamesh.platform.pp.registry.dataproduct.services.DataProductsDescriptorService;
import org.opendatamesh.platform.pp.registry.dataproduct.services.GitReference;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Primary
public class DataProductsDescriptorServiceMock extends IntegrationMock implements DataProductsDescriptorService {

    private DataProductsDescriptorService mockDescriptorService;

    @Override
    public void reset() {
        mockDescriptorService = Mockito.mock(DataProductsDescriptorService.class);
    }

    @Override
    public Optional<JsonNode> getDescriptor(String dataProductUuid, GitReference referencePointer, Credential credential) {
        return mockDescriptorService.getDescriptor(dataProductUuid, referencePointer, credential);
    }

    @Override
    public void initDescriptor(String dataProductUuid, JsonNode content, Credential credential) {
        mockDescriptorService.initDescriptor(dataProductUuid, content, credential);
    }

    @Override
    public void updateDescriptor(String dataProductUuid, String branch, String commitMessage, String baseCommit, JsonNode content, Credential credential) {
        mockDescriptorService.updateDescriptor(dataProductUuid, branch, commitMessage, baseCommit, content, credential);
    }

    public void setMockDescriptorService(DataProductsDescriptorService mockDescriptorService) {
        this.mockDescriptorService = mockDescriptorService;
    }

    public DataProductsDescriptorService getMockDescriptorService() {
        return mockDescriptorService;
    }
}
