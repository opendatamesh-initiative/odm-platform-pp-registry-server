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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Override
    public void initDescriptor(String uuid, JsonNode content, Credential credential) {
        DataProductRepo dataProductRepo = dataProductsService.findOne(uuid).getDataProductRepo();
        GitProvider provider = getGitProvider(dataProductRepo, credential);
        
        // Initialize the repository (creates tmp folder and sets up remote)
        File repoContent = provider.initRepository(dataProductRepo.getName(), dataProductRepo.getRemoteUrlHttp());
        
        try {
            initAndSaveDescriptor(provider, repoContent, dataProductRepo, content);
        } finally {
            // Clean up the temporary directory
            deleteRecursively(repoContent);
        }
    }

    @Override
    public void updateDescriptor(
            String uuid,
            String branch,
            String commitMessage,
            String baseCommit,
            JsonNode content,
            Credential credential) {

        DataProductRepo dataProductRepo = dataProductsService.findOne(uuid).getDataProductRepo();
        GitProvider provider = getGitProvider(dataProductRepo, credential);
        RepositoryPointer repositoryPointer = buildRepositoryPointer(provider, dataProductRepo, new GitReference(null, branch, null));
        File repoContent = provider.readRepository(repositoryPointer);
        writeAndSaveDescriptor(provider, repoContent, dataProductRepo, commitMessage, baseCommit, content);
    }

    private void initAndSaveDescriptor(GitProvider provider,
                           File repoContent,
                           DataProductRepo dataProductRepo,
                           JsonNode content) {
        try {
            Path descriptorPath = Paths.get(repoContent.getAbsolutePath(), dataProductRepo.getDescriptorRootPath());
            Files.createDirectories(Optional.ofNullable(descriptorPath.getParent()).orElse(Paths.get("")));
            Files.writeString(descriptorPath, content.toPrettyString(), StandardCharsets.UTF_8);
            provider.saveDescriptor(repoContent, String.valueOf(dataProductRepo.getDescriptorRootPath()), "Init Commit");
        } catch (IOException e) {
            throw new RuntimeException("Error updating descriptor", e);
        }
    }

    private void writeAndSaveDescriptor(GitProvider provider,
                                        File repoContent,
                                        DataProductRepo dataProductRepo,
                                        String commitMessage,
                                        String baseCommit,
                                        JsonNode content) {
        try {
            verifyConflict(baseCommit);
            Path descriptorPath = Paths.get(repoContent.getAbsolutePath(), dataProductRepo.getDescriptorRootPath());
            Files.writeString(descriptorPath, content.toPrettyString(), StandardCharsets.UTF_8);
            provider.saveDescriptor(repoContent, String.valueOf(dataProductRepo.getDescriptorRootPath()), commitMessage);
        } catch (IOException e) {
            throw new RuntimeException("Error updating descriptor", e);
        } finally {
            deleteRecursively(repoContent);
        }
    }


    private void verifyConflict(String baseCommit) {
        /*ObjectId currentHead = repository.resolve("refs/heads/" + branch);
        if (!currentHead.getName().equals(baseCommit)) {
            throw new ConflictException("Branch has moved since base commit");
        }*/
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
