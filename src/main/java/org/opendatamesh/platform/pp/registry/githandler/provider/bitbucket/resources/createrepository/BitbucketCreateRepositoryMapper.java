package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.createrepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.platform.pp.registry.githandler.model.AdditionalProviderProperty;
import org.opendatamesh.platform.pp.registry.githandler.model.OwnerType;
import org.opendatamesh.platform.pp.registry.githandler.model.Repository;
import org.opendatamesh.platform.pp.registry.githandler.model.Visibility;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.modelextensions.BitbucketRepositoryExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class BitbucketCreateRepositoryMapper {
    private static final Logger logger = LoggerFactory.getLogger(BitbucketCreateRepositoryMapper.class);

    /**
     * Maps BitbucketCreateRepositoryRepositoryRes to internal Repository model
     */
    public static Repository toInternalModel(BitbucketCreateRepositoryRepositoryRes repoRes, OwnerType ownerType) {
        if (repoRes == null) {
            return null;
        }

        // Extract clone URLs
        String cloneUrlHttp = null;
        String cloneUrlSsh = null;
        if (repoRes.getLinks() != null && repoRes.getLinks().getClone() != null) {
            cloneUrlHttp = repoRes.getLinks().getClone().stream()
                    .filter(link -> link.getName() != null && link.getName().equals("https"))
                    .findFirst()
                    .map(BitbucketCreateRepositoryLinkRes::getHref)
                    .orElse(null);
            cloneUrlSsh = repoRes.getLinks().getClone().stream()
                    .filter(link -> link.getName() != null && link.getName().equals("ssh"))
                    .findFirst()
                    .map(BitbucketCreateRepositoryLinkRes::getHref)
                    .orElse(null);
        }

        // Extract default branch
        String defaultBranch = null;
        if (repoRes.getMainbranch() != null) {
            defaultBranch = repoRes.getMainbranch().getName();
        }

        // Extract owner ID
        String ownerId = null;
        if (repoRes.getOwner() != null) {
            ownerId = repoRes.getOwner().getUuid();
        }

        // Determine visibility
        Visibility visibility = repoRes.getIsPrivate() ? Visibility.PRIVATE : Visibility.PUBLIC;

        // Create repository with core fields
        Repository repository = new Repository(
                repoRes.getUuid(),
                repoRes.getName(),
                repoRes.getDescription(),
                cloneUrlHttp,
                cloneUrlSsh,
                defaultBranch,
                ownerType,
                ownerId,
                visibility
        );

        // Build additionalProviderProperties
        List<AdditionalProviderProperty> additionalProperties = buildAdditionalProperties(repoRes);
        repository.setAdditionalProviderProperties(additionalProperties);

        return repository;
    }

    /**
     * Creates BitbucketCreateRepositoryRepositoryRes from internal Repository model
     */
    public static BitbucketCreateRepositoryRepositoryRes fromInternalModel(Repository repository) {
        if (repository == null) {
            return null;
        }

        BitbucketCreateRepositoryRepositoryRes repoRes = new BitbucketCreateRepositoryRepositoryRes();
        repoRes.setUuid(repository.getId());
        repoRes.setName(repository.getName());
        repoRes.setDescription(repository.getDescription());
        repoRes.setIsPrivate(repository.getVisibility() == Visibility.PRIVATE);

        // Extract project from additionalProperties if available
        if (repository.getAdditionalProviderProperties() != null) {
            for (AdditionalProviderProperty prop : repository.getAdditionalProviderProperties()) {
                if (BitbucketRepositoryExtension.PROJECT.equals(prop.getName()) && prop.getValue() != null) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        repoRes.setProject(mapper.treeToValue(prop.getValue(), BitbucketCreateRepositoryRepositoryProjectRes.class));
                    } catch (Exception e) {
                        // Ignore if conversion fails
                    }
                    break;
                }
            }
        }

        // Set mainbranch
        if (repository.getDefaultBranch() != null) {
            repoRes.setMainbranch(new BitbucketCreateRepositoryMainBranchRes(repository.getDefaultBranch()));
        }

        // Set owner (minimal - only UUID)
        if (repository.getOwnerId() != null) {
            BitbucketCreateRepositoryUserRes owner = new BitbucketCreateRepositoryUserRes();
            owner.setUuid(repository.getOwnerId());
            repoRes.setOwner(owner);
        }

        // Build links from clone URLs
        List<BitbucketCreateRepositoryLinkRes> cloneLinks = new ArrayList<>();
        if (repository.getCloneUrlHttp() != null) {
            cloneLinks.add(new BitbucketCreateRepositoryLinkRes("https", repository.getCloneUrlHttp()));
        }
        if (repository.getCloneUrlSsh() != null) {
            cloneLinks.add(new BitbucketCreateRepositoryLinkRes("ssh", repository.getCloneUrlSsh()));
        }
        if (!cloneLinks.isEmpty()) {
            repoRes.setLinks(new BitbucketCreateRepositoryLinksRes(null, null, cloneLinks));
        }

        return repoRes;
    }

    /**
     * Builds the additionalProviderProperties list
     */
    private static List<AdditionalProviderProperty> buildAdditionalProperties(BitbucketCreateRepositoryRepositoryRes repoRes) {
        List<AdditionalProviderProperty> properties = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Add project information if available
            if (repoRes.getProject() != null) {
                JsonNode projectNode = objectMapper.valueToTree(repoRes.getProject());
                AdditionalProviderProperty projectProp = new AdditionalProviderProperty();
                projectProp.setName(BitbucketRepositoryExtension.PROJECT);
                projectProp.setValue(projectNode);
                properties.add(projectProp);
            }

            // Add uuid
            if (repoRes.getUuid() != null) {
                AdditionalProviderProperty uuidProp = new AdditionalProviderProperty();
                uuidProp.setName("uuid");
                uuidProp.setValue(objectMapper.valueToTree(repoRes.getUuid()));
                properties.add(uuidProp);
            }

            // Add owner details if available
            if (repoRes.getOwner() != null) {
                JsonNode ownerNode = objectMapper.valueToTree(repoRes.getOwner());
                AdditionalProviderProperty ownerProp = new AdditionalProviderProperty();
                ownerProp.setName("owner");
                ownerProp.setValue(ownerNode);
                properties.add(ownerProp);
            }

            // Add mainbranch details if available
            if (repoRes.getMainbranch() != null) {
                JsonNode mainbranchNode = objectMapper.valueToTree(repoRes.getMainbranch());
                AdditionalProviderProperty mainbranchProp = new AdditionalProviderProperty();
                mainbranchProp.setName("mainbranch");
                mainbranchProp.setValue(mainbranchNode);
                properties.add(mainbranchProp);
            }

            // Add links if available
            if (repoRes.getLinks() != null) {
                JsonNode linksNode = objectMapper.valueToTree(repoRes.getLinks());
                AdditionalProviderProperty linksProp = new AdditionalProviderProperty();
                linksProp.setName("links");
                linksProp.setValue(linksNode);
                properties.add(linksProp);
            }

            // Add is_private flag
            AdditionalProviderProperty isPrivateProp = new AdditionalProviderProperty();
            isPrivateProp.setName("is_private");
            isPrivateProp.setValue(objectMapper.valueToTree(repoRes.getIsPrivate()));
            properties.add(isPrivateProp);

        } catch (Exception e) {
            // If serialization fails, continue without that property
        }

        return properties;
    }

    /**
     * Creates BitbucketCreateRepositoryReq from internal Repository model
     */
    public static BitbucketCreateRepositoryReq createRequestFromInternalModel(Repository repository) {
        if (repository == null) {
            return null;
        }

        BitbucketCreateRepositoryReq request = new BitbucketCreateRepositoryReq(
                "git",
                repository.getVisibility() == Visibility.PRIVATE,
                repository.getName(),
                repository.getDescription()
        );

        // Extract project from additionalProperties if available
        BitbucketCreateRepositoryProjectReq projectReq = extractProjectFromAdditionalProperties(repository);
        if (projectReq != null) {
            request.setProject(projectReq);
        }

        return request;
    }

    /**
     * Extracts project information from repository's additionalProviderProperties
     */
    private static BitbucketCreateRepositoryProjectReq extractProjectFromAdditionalProperties(Repository repository) {
        if (repository.getAdditionalProviderProperties() == null) {
            return null;
        }

        for (AdditionalProviderProperty prop : repository.getAdditionalProviderProperties()) {
            if (BitbucketRepositoryExtension.PROJECT.equals(prop.getName()) && prop.getValue() != null) {
                try {
                    JsonNode projectNode = prop.getValue();

                    // Extract key and uuid from the project node
                    String key = projectNode.has("key") ? projectNode.get("key").asText() : null;
                    String uuid = projectNode.has("uuid") ? projectNode.get("uuid").asText() : null;

                    // At least one of key or uuid must be present
                    if (key != null || uuid != null) {
                        return new BitbucketCreateRepositoryProjectReq(key, uuid);
                    }
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
                break;
            }
        }

        return null;
    }
}

