package org.opendatamesh.platform.pp.registry.dataproduct.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProductRepo;
import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.exceptions.InternalException;
import org.opendatamesh.platform.pp.registry.exceptions.ResourceConflictException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.descriptor.GetDescriptorOptionsRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.descriptor.InitDescriptorCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.descriptor.UpdateDescriptorCommandRes;
import org.opendatamesh.platform.pp.registry.utils.git.exceptions.GitOperationException;
import org.opendatamesh.platform.pp.registry.utils.git.model.*;
import org.opendatamesh.platform.pp.registry.utils.git.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.utils.git.provider.GitProviderFactory;
import org.opendatamesh.platform.pp.registry.utils.git.provider.GitProviderIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;


@Service
public class DataProductsDescriptorServiceImpl implements DataProductsDescriptorService {

    private final DataProductsService dataProductsService;
    private final GitProviderFactory gitProviderFactory;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public DataProductsDescriptorServiceImpl(DataProductsService dataProductsService,
                                             GitProviderFactory gitProviderFactory) {
        this.dataProductsService = dataProductsService;
        this.gitProviderFactory = gitProviderFactory;
    }

    @Override
    public JsonNode getDescriptor(String dataProductUuid, GetDescriptorOptionsRes options, HttpHeaders headers) {
        GetDescriptorOptionsRes.GitReference optRef = options.getGitReference();
        GitReference referencePointer = new GitReference(optRef.tag(), optRef.branch(), optRef.commit());
        logger.info("Getting descriptor for dataProductUuid={}, referencePointer={}", dataProductUuid,
                referencePointer);
        DataProductRepo dataProductRepo = dataProductsService.findOne(dataProductUuid).getDataProductRepo();
        logger.info(
                "Resolved data product repo: providerType={}, providerBaseUrl={}, descriptorRootPath={}, remoteUrl={}",
                dataProductRepo.getProviderType(), dataProductRepo.getProviderBaseUrl(),
                dataProductRepo.getDescriptorRootPath(), dataProductRepo.getRemoteUrlHttp());
        GitProvider provider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(dataProductRepo.getProviderType().name(), dataProductRepo.getProviderBaseUrl()),
                headers
        );
        Repository gitRepo = provider
                .getRepository(dataProductRepo.getExternalIdentifier(), dataProductRepo.getOwnerId())
                .orElseThrow(() -> new BadRequestException(
                        "No remote repository was found for data product with id " + dataProductRepo.getUuid()));
        RepositoryPointer repositoryPointer = buildRepositoryPointer(referencePointer);
        logger.info("Built repository pointer for externalId={}, ownerId={}", dataProductRepo.getExternalIdentifier(),
                dataProductRepo.getOwnerId());
        AtomicReference<JsonNode> descriptor = new AtomicReference<>();
        try {
            provider.gitOperation().readRepository(gitRepo, repositoryPointer, repository -> {
                logger.info("Repository cloned/checked out at {} for dataProductUuid={}", repository.getAbsolutePath(),
                        dataProductUuid);
                try {
                    File descriptorFile = new File(repository, dataProductRepo.getDescriptorRootPath());
                    if (!descriptorFile.exists()) {
                        logger.info("Descriptor file does not exist at: {} for repository: {}",
                                dataProductRepo.getDescriptorRootPath(), dataProductRepo.getRemoteUrlHttp());
                        return;
                    }
                    descriptor.set(new ObjectMapper().readTree(descriptorFile));
                    logger.info("Successfully read and parsed descriptor from {} for dataProductUuid={}",
                            dataProductRepo.getDescriptorRootPath(), dataProductUuid);
                } catch (JsonProcessingException e) {
                    logger.warn("Descriptor file is malformed at: {}, for repository: {}",
                            dataProductRepo.getDescriptorRootPath(), dataProductRepo.getRemoteUrlHttp(), e);
                    throw new ResourceConflictException("Unable to process descriptor file: " + e.getMessage(), e);
                } catch (IOException e) {
                    logger.warn("Could not access descriptor file at: {}, for repository: {}",
                            dataProductRepo.getDescriptorRootPath(), dataProductRepo.getRemoteUrlHttp(), e);
                    throw new ResourceConflictException("Unable to process descriptor file: " + e.getMessage(), e);
                }
            });
        } catch (GitOperationException e) {
            logger.warn("Failed to get repository content for data product {}: {}", dataProductUuid, e.getMessage(), e);
            throw new BadRequestException(
                    "Failed to get repository content for data product " + dataProductUuid + ": " + e.getMessage(), e);
        }
        if (descriptor.get() == null) {
            logger.info("No descriptor content returned for dataProductUuid={} (e.g. file missing)", dataProductUuid);
        }
        return descriptor.get();
    }

    @Override
    public void initDescriptor(String dataProductUuid, JsonNode content, InitDescriptorCommandRes options, HttpHeaders headers) {
        String branch = options.getBranch();
        String authorName = options.getAuthorName();
        String authorEmail = options.getAuthorEmail();
        logger.info("Initializing descriptor for dataProductUuid={}, branch={}, authorName={}", dataProductUuid, branch,
                authorName);
        DataProductRepo dataProductRepo = dataProductsService.findOne(dataProductUuid).getDataProductRepo();
        logger.info(
                "Resolved data product repo: providerType={}, providerBaseUrl={}, descriptorRootPath={}, defaultBranch={}",
                dataProductRepo.getProviderType(), dataProductRepo.getProviderBaseUrl(),
                dataProductRepo.getDescriptorRootPath(), dataProductRepo.getDefaultBranch());
        GitProvider provider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(dataProductRepo.getProviderType().name(), dataProductRepo.getProviderBaseUrl()),
                headers
        );
        String targetBranch = StringUtils.hasText(branch) ? branch : dataProductRepo.getDefaultBranch();
        logger.info("Using target branch: {}", targetBranch);
        Repository gitRepo = provider
                .getRepository(dataProductRepo.getExternalIdentifier(), dataProductRepo.getOwnerId())
                .orElseThrow(() -> new BadRequestException(
                        "No remote repository was found for data product with id " + dataProductRepo.getUuid()));
        RepositoryPointer repositoryPointer = buildRepositoryPointer(new GitReference(null, targetBranch, null));

        try {
            provider.gitOperation().readRepository(gitRepo, repositoryPointer, repository -> {
                logger.info("Repository checked out at {} for init descriptor, dataProductUuid={}",
                        repository.getAbsolutePath(), dataProductUuid);
                try {
                    Path descriptorPath = Paths.get(repository.getAbsolutePath(),
                            dataProductRepo.getDescriptorRootPath());
                    Files.createDirectories(Optional.ofNullable(descriptorPath.getParent()).orElse(Paths.get("")));
                    logger.info("Created parent directories for descriptor path: {}", descriptorPath);
                    Files.writeString(descriptorPath, content.toPrettyString(), StandardCharsets.UTF_8);
                    logger.info("Wrote descriptor content to {}", descriptorPath);

                    File descriptorFile = new File(repository, dataProductRepo.getDescriptorRootPath());
                    provider.gitOperation().addFiles(repository, List.of(descriptorFile));
                    logger.info("Staged descriptor file for commit");
                    boolean committed = provider.gitOperation().commit(repository,
                            new Commit("Init Commit", authorName, authorEmail));
                    if (committed) {
                        provider.gitOperation().push(repository, false);
                        logger.info("Init descriptor committed and pushed to branch {} for dataProductUuid={}",
                                targetBranch, dataProductUuid);
                    } else {
                        logger.info("No commit created (no changes) for dataProductUuid={}", dataProductUuid);
                    }
                } catch (GitOperationException e) {
                    logger.warn("Git operation failed during descriptor initialization for dataProductUuid={}: {}",
                            dataProductUuid, e.getMessage(), e);
                    throw new BadRequestException(
                            "Git operation failed during descriptor initialization: " + e.getMessage(), e);
                } catch (IOException e) {
                    logger.warn("I/O error during descriptor initialization for dataProductUuid={}: {}",
                            dataProductUuid, e.getMessage(), e);
                    throw new InternalException("Failed to write or create descriptor file: " + e.getMessage(), e);
                }
            });
        } catch (GitOperationException e) {
            logger.warn("Failed to read repository for descriptor init, dataProductUuid={}: {}", dataProductUuid,
                    e.getMessage(), e);
            throw new BadRequestException("Failed to access repository (e.g. branch not found): " + e.getMessage(), e);
        }
    }

    @Override
    public void updateDescriptor(String dataProductUuid, JsonNode content, UpdateDescriptorCommandRes options, HttpHeaders headers) {
        String branch = options.getBranch();
        String commitMessage = options.getCommitMessage();
        String baseCommit = options.getBaseCommit();
        String authorName = options.getAuthorName();
        String authorEmail = options.getAuthorEmail();
        logger.info(
                "Updating descriptor for dataProductUuid={}, branch={}, commitMessage={}, baseCommit={}, authorName={}",
                dataProductUuid, branch, commitMessage, baseCommit, authorName);
        DataProductRepo dataProductRepo = dataProductsService.findOne(dataProductUuid).getDataProductRepo();
        logger.info("Resolved data product repo: providerType={}, providerBaseUrl={}, descriptorRootPath={}",
                dataProductRepo.getProviderType(), dataProductRepo.getProviderBaseUrl(),
                dataProductRepo.getDescriptorRootPath());
        GitProvider provider = gitProviderFactory.buildGitProvider(
                new GitProviderIdentifier(dataProductRepo.getProviderType().name(), dataProductRepo.getProviderBaseUrl()),
                headers
        );
        Repository gitRepo = provider
                .getRepository(dataProductRepo.getExternalIdentifier(), dataProductRepo.getOwnerId())
                .orElseThrow(() -> new BadRequestException(
                        "No remote repository was found for data product with id " + dataProductRepo.getUuid()));
        RepositoryPointer repositoryPointer = buildRepositoryPointer(new GitReference(null, branch, null));

        try {
            provider.gitOperation().readRepository(gitRepo, repositoryPointer, repository -> {
                logger.info("Repository checked out at {} for update descriptor, dataProductUuid={}",
                        repository.getAbsolutePath(), dataProductUuid);
                try {
                    validateBaseCommit(branch, baseCommit, repository, provider);
                    if (StringUtils.hasText(baseCommit)) {
                        logger.info("Base commit validated: {}", baseCommit);
                    }

                    Path descriptorPath = Paths.get(repository.getAbsolutePath(), dataProductRepo.getDescriptorRootPath());
                    Files.writeString(descriptorPath, content.toPrettyString(), StandardCharsets.UTF_8);
                    logger.info("Wrote updated descriptor content to {}", descriptorPath);

                    File descriptorFile = new File(repository, dataProductRepo.getDescriptorRootPath());
                    provider.gitOperation().addFiles(repository, List.of(descriptorFile));
                    logger.info("Staged descriptor file for commit");

                    boolean committed = provider.gitOperation().commit(repository,
                            new Commit(commitMessage, authorName, authorEmail));

                    if (committed) {
                        provider.gitOperation().push(repository, false);
                        logger.info("Descriptor update committed and pushed to branch {} for dataProductUuid={}",
                                branch, dataProductUuid);
                    } else {
                        throw new BadRequestException(
                                "No changes to commit. The descriptor content is identical to the current version.");
                    }

                } catch (ResourceConflictException e) {
                    logger.warn("Base commit conflict during descriptor update for dataProductUuid={}: {}",
                            dataProductUuid, e.getMessage(), e);
                    throw new ResourceConflictException(
                            "Base commit conflict during descriptor update: " + e.getMessage(), e);
                } catch (GitOperationException e) {
                    logger.warn("Git operation failed during descriptor update for dataProductUuid={}: {}",
                            dataProductUuid, e.getMessage(), e);
                    throw new BadRequestException("Git operation failed during descriptor update: " + e.getMessage(),
                            e);
                } catch (IOException e) {
                    logger.warn("I/O error during descriptor update for dataProductUuid={}: {}", dataProductUuid,
                            e.getMessage(), e);
                    throw new InternalException("Failed to write descriptor file: " + e.getMessage(), e);
                }
            });
        } catch (GitOperationException e) {
            logger.warn("Failed to get repository content for data product {}: {}", dataProductUuid, e.getMessage(), e);
            throw new BadRequestException("Failed to get repository content: " + e.getMessage(), e);
        }
    }

    private void validateBaseCommit(String branch, String baseCommit, File repository, GitProvider provider) {
        if (StringUtils.hasText(baseCommit)) {
            String headCommit = provider.gitOperation().getHeadSha(repository, branch);
            if (!baseCommit.equals(headCommit)) {
                throw new ResourceConflictException(
                        "Conflict detected: base commit " + baseCommit + " does not match latest commit " + headCommit
                );
            }
        }
    }

    private RepositoryPointer buildRepositoryPointer(GitReference pointer) {
        return switch (pointer.type()) {
            case TAG -> new RepositoryPointerTag(pointer.tag());
            case BRANCH -> new RepositoryPointerBranch(pointer.branch());
            case COMMIT -> new RepositoryPointerCommit(pointer.commit());
        };
    }

    private record GitReference(String tag, String branch, String commit) {
        enum VersionType {TAG, BRANCH, COMMIT}

        VersionType type() {
            if (tag != null) return VersionType.TAG;
            if (branch != null) return VersionType.BRANCH;
            if (commit != null) return VersionType.COMMIT;
            return VersionType.BRANCH;
        }
    }

}
