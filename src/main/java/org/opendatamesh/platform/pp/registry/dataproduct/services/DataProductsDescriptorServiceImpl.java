package org.opendatamesh.platform.pp.registry.dataproduct.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepo;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.exceptions.ResourceConflictException;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.GitOperationException;
import org.opendatamesh.platform.pp.registry.githandler.git.GitOperation;
import org.opendatamesh.platform.pp.registry.githandler.git.GitOperationFactory;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderFactory;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderIdentifier;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.TagRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;


@Service
public class DataProductsDescriptorServiceImpl implements DataProductsDescriptorService {

    @Autowired
    private DataProductsService dataProductsService;

    @Autowired
    private GitProviderFactory gitProviderFactory;

    @Autowired
    private GitOperationFactory gitOperationFactory;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public Optional<JsonNode> getDescriptor(String dataProductUuid, GitReference referencePointer, HttpHeaders headers) {
        DataProductRepo dataProductRepo = dataProductsService.findOne(dataProductUuid).getDataProductRepo();
        GitProvider provider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(dataProductRepo.getProviderType().name(), dataProductRepo.getProviderBaseUrl()),
                headers
        );
        RepositoryPointer repositoryPointer = buildRepositoryPointer(provider, dataProductRepo, referencePointer);

        // Create GitAuthContext and GitOperation directly
        var authContext = provider.createGitAuthContext();
        GitOperation gitOperation = gitOperationFactory.createGitOperation(authContext);

        try {
            File repoContent = gitOperation.getRepositoryContent(repositoryPointer);
            return readDescriptorFile(repoContent, dataProductRepo.getDescriptorRootPath());
        } catch (GitOperationException e) {
            logger.warn("Failed to get repository content for data product {}: {}", dataProductUuid, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public void initDescriptor(String dataProductUuid, JsonNode content, HttpHeaders headers) {
        DataProductRepo dataProductRepo = dataProductsService.findOne(dataProductUuid).getDataProductRepo();
        GitProvider provider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(dataProductRepo.getProviderType().name(), dataProductRepo.getProviderBaseUrl()),
                headers
        );
        RepositoryPointer repositoryPointer = buildRepositoryPointer(provider, dataProductRepo, new GitReference(null, dataProductRepo.getDefaultBranch(), null));

        // Create GitAuthContext and GitOperation directly
        var authContext = provider.createGitAuthContext();
        GitOperation gitOperation = gitOperationFactory.createGitOperation(authContext);

        File repoContent;
        try {
            repoContent = gitOperation.getRepositoryContent(repositoryPointer);
        } catch (GitOperationException e) {
            logger.warn("Provided repo does not have a default starting branch, creating one...", e);
            repoContent = null;
        }

        if (repoContent == null) {
            try {
                // No remote branch / repository missing or read failed
                repoContent = gitOperation.initRepository(
                        dataProductRepo.getName(),
                        dataProductRepo.getDefaultBranch(),
                        new URI(dataProductRepo.getRemoteUrlHttp()).toURL()
                );
            } catch (GitOperationException e) {
                logger.warn("Failed to initialize repository for data product {}: {}", dataProductUuid, e.getMessage(), e);
                return; // Exit method gracefully
            } catch (MalformedURLException | java.net.URISyntaxException e) {
                logger.warn("Error with remote URL for data product {}: {}", dataProductUuid, e.getMessage(), e);
                return; // Exit method gracefully
            }
        }

        try {
            initAndSaveDescriptor(gitOperation, repoContent, dataProductRepo, content);
        } finally {
            deleteRecursively(repoContent);
        }
    }

    @Override
    public void updateDescriptor(
            String dataProductUuid,
            String branch,
            String commitMessage,
            String baseCommit,
            JsonNode content,
            HttpHeaders headers) {

        DataProductRepo dataProductRepo = dataProductsService.findOne(dataProductUuid).getDataProductRepo();
        GitProvider provider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(dataProductRepo.getProviderType().name(), dataProductRepo.getProviderBaseUrl()),
                headers
        );
        RepositoryPointer repositoryPointer = buildRepositoryPointer(provider, dataProductRepo, new GitReference(null, branch, null));

        // Create GitAuthContext and GitOperation directly
        var authContext = provider.createGitAuthContext();
        GitOperation gitOperation = gitOperationFactory.createGitOperation(authContext);

        try {
            File repoContent = gitOperation.getRepositoryContent(repositoryPointer);
            writeAndSaveDescriptor(gitOperation, repoContent, dataProductRepo, commitMessage, baseCommit, branch, content);
        } catch (GitOperationException e) {
            logger.warn("Failed to get repository content for data product {}: {}", dataProductUuid, e.getMessage(), e);
        }
    }

    @Override
    public TagRes addTag(String dataProductUuid, TagRes tagReq, HttpHeaders headers) {
        if (!StringUtils.hasText(tagReq.getTagName())) {
            throw new BadRequestException("Missing tag name");
        }
        DataProductRepo dataProductRepo = dataProductsService.findOne(dataProductUuid).getDataProductRepo();
        if (dataProductRepo == null) {
            throw new BadRequestException("No repository configured for data product " + dataProductUuid);
        }
        GitProvider provider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(dataProductRepo.getProviderType().name(), dataProductRepo.getProviderBaseUrl()),
                headers
        );
        String branchName = StringUtils.hasText(tagReq.getBranchName()) ? tagReq.getBranchName() : dataProductRepo.getDefaultBranch();
        // Always clone the default branch (safe fallback)
        RepositoryPointer repositoryPointer = buildRepositoryPointer(
                provider,
                dataProductRepo,
                new GitReference(null, branchName, null)
        );

        var authContext = provider.createGitAuthContext();
        GitOperation gitOperation = gitOperationFactory.createGitOperation(authContext);

        File repoContent = null;
        try {
            // Clone the repository into a temporary directory
            repoContent = gitOperation.getRepositoryContent(repositoryPointer);
            // Determine which commit SHA to use
            String targetSha;
            if (StringUtils.hasText(tagReq.getTarget())) {
                // CASE 1 → Tag on explicit commit SHA
                targetSha = tagReq.getTarget();
            } else if (StringUtils.hasText(tagReq.getBranchName())) {
                // CASE 2 → Tag latest commit on specified branch
                targetSha = gitOperation.getLatestCommitSha(repoContent, tagReq.getBranchName());
            } else {
                // CASE 3 → Tag latest commit on default branch
                targetSha = gitOperation.getLatestCommitSha(repoContent, dataProductRepo.getDefaultBranch());
            }

            // Create the tag (annotated if message provided)
            gitOperation.addTag(
                    repoContent,
                    tagReq.getTagName(),
                    targetSha,
                    tagReq.getMessage()
            );
            gitOperation.push(repoContent, true);
        } catch (GitOperationException e) {
            logger.warn("Failed to create tag for data product {}: {}", dataProductUuid, e.getMessage(), e);
            throw new BadRequestException("Failed to create tag: " + e.getMessage());
        } finally {
            if (repoContent != null) {
                deleteRecursively(repoContent);
            }
        }
        return tagReq;
    }

    private void initAndSaveDescriptor(GitOperation gitOperation,
                                       File repoContent,
                                       DataProductRepo dataProductRepo,
                                       JsonNode content) {
        try {
            Path descriptorPath = Paths.get(repoContent.getAbsolutePath(), dataProductRepo.getDescriptorRootPath());
            Files.createDirectories(Optional.ofNullable(descriptorPath.getParent()).orElse(Paths.get("")));
            Files.writeString(descriptorPath, content.toPrettyString(), StandardCharsets.UTF_8);

            File descriptorFile = new File(repoContent, dataProductRepo.getDescriptorRootPath());
            gitOperation.addFiles(repoContent, List.of(descriptorFile));
            boolean committed = gitOperation.commit(repoContent, "Init Commit");
            if (committed) {
                gitOperation.push(repoContent, false);
            }
        } catch (GitOperationException e) {
            logger.warn("Git operation failed during descriptor initialization: {}", e.getMessage(), e);
            // Continue execution - the operation failed but we don't want to crash
        } catch (IOException e) {
            logger.warn("Problem while reading descriptor repo content", e);
            // Continue execution - the operation failed but we don't want to crash
        }
    }

    private void writeAndSaveDescriptor(GitOperation gitOperation,
                                        File repoContent,
                                        DataProductRepo dataProductRepo,
                                        String commitMessage,
                                        String baseCommit,
                                        String branch,
                                        JsonNode content) {
        try {
            verifyConflict(repoContent, branch, baseCommit);
            Path descriptorPath = Paths.get(repoContent.getAbsolutePath(), dataProductRepo.getDescriptorRootPath());
            Files.writeString(descriptorPath, content.toPrettyString(), StandardCharsets.UTF_8);

            File descriptorFile = new File(repoContent, dataProductRepo.getDescriptorRootPath());
            gitOperation.addFiles(repoContent, List.of(descriptorFile));
            boolean committed = gitOperation.commit(repoContent, commitMessage);
            if (committed) {
                gitOperation.push(repoContent, false);
            }
        } catch (GitOperationException e) {
            logger.warn("Git operation failed during descriptor save: {}", e.getMessage(), e);
            // Continue execution - the operation failed but we don't want to crash
        } catch (IOException e) {
            logger.warn("Problem while reading descriptor repo content", e);
            // Continue execution - the operation failed but we don't want to crash
        } finally {
            deleteRecursively(repoContent);
        }
    }


    private void verifyConflict(File repoContent, String branch, String baseCommit) {
        if (baseCommit == null || baseCommit.isEmpty()) {
            return;
        }

        try (Git git = Git.open(repoContent)) {
            ObjectId latestCommitId = git.getRepository()
                    .resolve("refs/heads/" + branch);

            if (latestCommitId == null) {
                throw new BadRequestException("Branch " + branch + " does not exist");
            }

            String latestCommit = latestCommitId.getName();
            if (!baseCommit.equals(latestCommit)) {
                throw new ResourceConflictException(
                        "Conflict detected: base commit " + baseCommit + " does not match latest commit " + latestCommit
                );
            }
        } catch (IOException e) {
            logger.warn("Problem while reading descriptor repo content", e);
        }
    }


    private RepositoryPointer buildRepositoryPointer(GitProvider provider, DataProductRepo repo, GitReference pointer) {
        Repository gitRepo = provider.getRepository(repo.getExternalIdentifier(), repo.getOwnerId())
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
