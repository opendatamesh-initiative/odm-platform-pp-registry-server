package org.opendatamesh.platform.pp.registry.dataproduct.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepo;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Optional;


@Service
public class DataProductsDescriptorServiceImpl implements DataProductsDescriptorService {

    @Autowired
    private DataProductsService dataProductsService;

    @Autowired
    private GitProviderFactory gitProviderFactory;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public Optional<JsonNode> getDescriptor(String uuid, GitReference referencePointer, Credential credential) {
        DataProductRepo dataProductRepo = dataProductsService.findOne(uuid).getDataProductRepo();
        GitProvider provider = getGitProvider(dataProductRepo, credential);
        RepositoryPointer repositoryPointer = buildRepositoryPointer(provider, dataProductRepo, referencePointer);
        File repoContent = provider.readRepository(repositoryPointer);
        return readDescriptorFile(repoContent, dataProductRepo.getDescriptorRootPath());
    }

    private GitProvider getGitProvider(DataProductRepo repo, Credential credential) {
        return gitProviderFactory.getProvider(
                repo.getProviderType(),
                repo.getProviderBaseUrl(),
                null,
                credential
        ).orElseThrow(() -> new BadRequestException(
                "Unsupported Git provider type: " + repo.getProviderType()));
    }

    private RepositoryPointer buildRepositoryPointer(GitProvider provider, DataProductRepo repo, GitReference pointer) {
        Repository gitRepo = provider.getRepository(repo.getExternalIdentifier())
                .orElseThrow(() -> new BadRequestException(
                        "No remote repository was found for data product with id " + repo.getUuid()));

        return switch (pointer.getType()) {
            case TAG -> new RepositoryPointerTag(gitRepo, pointer.getTag());
            case BRANCH -> new RepositoryPointerBranch(gitRepo, pointer.getBranch());
            case COMMIT -> new RepositoryPointerCommit(gitRepo, pointer.getCommit());
        };
    }

    public Optional<JsonNode> readDescriptorFile(File repository, String descriptorRootPath) {

        if (repository == null || !repository.exists()) {
            return Optional.empty();
        }

        File descriptorFile = null;
        try {
            descriptorFile = new File(repository, descriptorRootPath);
            if (!descriptorFile.exists()) {
                return Optional.empty();
            }
            JsonNode jsonNode = objectMapper.readTree(descriptorFile);
            return Optional.ofNullable(jsonNode);
        } catch (IOException e) {
            logger.warn("Couldn't access file", e);
            return Optional.empty();
        } finally {
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
            logger.warn("Failed to delete temp file/folder: {}", file.getAbsolutePath());
        }
    }
}
