package org.opendatamesh.platform.pp.registry.dataproduct.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepository;
import org.opendatamesh.platform.pp.registry.dataproduct.resources.ProviderType;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Optional;


@Service
public class DataProductDescriptorServiceImpl implements DataProductDescriptorService {

    @Autowired
    DataProductService dataProductService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Optional<JsonNode> getDescriptor(String uuid, VersionPointer pointer, Credential credential) {

        DataProductRepository dataProductRepository = dataProductService.findOne(uuid).getDataProductRepository();
        ProviderType providerType = dataProductRepository.getProviderType();
        String gitRemoteBaseUrl = dataProductRepository.getProviderBaseUrl();
        GitProvider provider = GitProviderFactory.getProvider(providerType, gitRemoteBaseUrl, null, credential);
        Repository gitRepo = provider.getRepository(dataProductRepository.getExternalIdentifier()).orElseThrow(() -> new IllegalArgumentException("Repository not found"));

        RepositoryPointer repositoryPointer = switch (pointer.getType()) {
            case TAG -> new RepositoryPointerTag(gitRepo, pointer.getTag());
            case BRANCH -> new RepositoryPointerBranch(gitRepo, pointer.getBranch());
            case COMMIT -> new RepositoryPointerCommit(gitRepo, pointer.getCommit());
        };

        return readDescriptorFile(provider.readRepository(repositoryPointer), dataProductRepository.getDescriptorRootPath());
    }

    public Optional<JsonNode> readDescriptorFile(File repository, String descriptorRootPath) {

        if (repository == null || !repository.exists()) {
            return Optional.empty();
        }

        File descriptorFile = new File(repository, descriptorRootPath);
        if (!descriptorFile.exists()) {
            return Optional.empty();
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(descriptorFile);
            return Optional.ofNullable(jsonNode);
        } catch (IOException e) {
            return Optional.empty();
        } finally {
            if (!descriptorFile.delete()) {
                System.err.println("Failed to delete descriptor file: " + descriptorFile.getAbsolutePath());
            }
            deleteRecursively(repository);
        }
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        if (!file.delete()) {
            System.err.println("Failed to delete temp file/folder: " + file.getAbsolutePath());
        }
    }
}
