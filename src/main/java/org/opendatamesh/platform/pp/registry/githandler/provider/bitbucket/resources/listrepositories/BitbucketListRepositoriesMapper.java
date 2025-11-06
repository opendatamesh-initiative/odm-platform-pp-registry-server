package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listrepositories;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatamesh.platform.pp.registry.githandler.model.ProviderCustomResourceProperty;
import org.opendatamesh.platform.pp.registry.githandler.model.OwnerType;
import org.opendatamesh.platform.pp.registry.githandler.model.Repository;
import org.opendatamesh.platform.pp.registry.githandler.model.Visibility;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.modelextensions.BitbucketRepositoryExtension;

import java.util.ArrayList;
import java.util.List;

public abstract class BitbucketListRepositoriesMapper {

    /**
     * Maps BitbucketListRepositoriesRepositoryRes to internal Repository model
     */
    public static Repository toInternalModel(BitbucketListRepositoriesRepositoryRes repoRes, OwnerType ownerType) {
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
                    .map(BitbucketListRepositoriesLinkRes::getHref)
                    .orElse(null);
            cloneUrlSsh = repoRes.getLinks().getClone().stream()
                    .filter(link -> link.getName() != null && link.getName().equals("ssh"))
                    .findFirst()
                    .map(BitbucketListRepositoriesLinkRes::getHref)
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
        List<ProviderCustomResourceProperty> additionalProperties = buildAdditionalProperties(repoRes);
        repository.setProviderCustomResourceProperties(additionalProperties);

        return repository;
    }

    /**
     * Creates BitbucketListRepositoriesRepositoryRes from internal Repository model
     */
    public static BitbucketListRepositoriesRepositoryRes fromInternalModel(Repository repository) {
        if (repository == null) {
            return null;
        }
        
        BitbucketListRepositoriesRepositoryRes repoRes = new BitbucketListRepositoriesRepositoryRes();
        repoRes.setUuid(repository.getId());
        repoRes.setName(repository.getName());
        repoRes.setDescription(repository.getDescription());
        repoRes.setIsPrivate(repository.getVisibility() == Visibility.PRIVATE);
        
        // Extract project from additionalProperties if available
        if (repository.getProviderCustomResourceProperties() != null) {
            for (ProviderCustomResourceProperty prop : repository.getProviderCustomResourceProperties()) {
                if (BitbucketRepositoryExtension.PROJECT.equals(prop.getName()) && prop.getValue() != null) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        repoRes.setProject(mapper.treeToValue(prop.getValue(), BitbucketListRepositoriesRepositoryProjectRes.class));
                    } catch (Exception e) {
                        // Ignore if conversion fails
                    }
                    break;
                }
            }
        }
        
        // Set mainbranch
        if (repository.getDefaultBranch() != null) {
            repoRes.setMainbranch(new BitbucketListRepositoriesMainBranchRes(repository.getDefaultBranch()));
        }
        
        // Set owner (minimal - only UUID)
        if (repository.getOwnerId() != null) {
            BitbucketListRepositoriesUserRes owner = new BitbucketListRepositoriesUserRes();
            owner.setUuid(repository.getOwnerId());
            repoRes.setOwner(owner);
        }
        
        // Build links from clone URLs
        List<BitbucketListRepositoriesLinkRes> cloneLinks = new ArrayList<>();
        if (repository.getCloneUrlHttp() != null) {
            cloneLinks.add(new BitbucketListRepositoriesLinkRes("https", repository.getCloneUrlHttp()));
        }
        if (repository.getCloneUrlSsh() != null) {
            cloneLinks.add(new BitbucketListRepositoriesLinkRes("ssh", repository.getCloneUrlSsh()));
        }
        if (!cloneLinks.isEmpty()) {
            repoRes.setLinks(new BitbucketListRepositoriesLinksRes(null, null, cloneLinks));
        }
        
        return repoRes;
    }

    /**
     * Builds the additionalProviderProperties list
     */
    private static List<ProviderCustomResourceProperty> buildAdditionalProperties(BitbucketListRepositoriesRepositoryRes repoRes) {
        List<ProviderCustomResourceProperty> properties = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Add project information if available
            if (repoRes.getProject() != null) {
                JsonNode projectNode = objectMapper.valueToTree(repoRes.getProject());
                ProviderCustomResourceProperty projectProp = new ProviderCustomResourceProperty();
                projectProp.setName(BitbucketRepositoryExtension.PROJECT);
                projectProp.setValue(projectNode);
                properties.add(projectProp);
            }

            // Add uuid
            if (repoRes.getUuid() != null) {
                ProviderCustomResourceProperty uuidProp = new ProviderCustomResourceProperty();
                uuidProp.setName("uuid");
                uuidProp.setValue(objectMapper.valueToTree(repoRes.getUuid()));
                properties.add(uuidProp);
            }

            // Add owner details if available
            if (repoRes.getOwner() != null) {
                JsonNode ownerNode = objectMapper.valueToTree(repoRes.getOwner());
                ProviderCustomResourceProperty ownerProp = new ProviderCustomResourceProperty();
                ownerProp.setName("owner");
                ownerProp.setValue(ownerNode);
                properties.add(ownerProp);
            }

            // Add mainbranch details if available
            if (repoRes.getMainbranch() != null) {
                JsonNode mainbranchNode = objectMapper.valueToTree(repoRes.getMainbranch());
                ProviderCustomResourceProperty mainbranchProp = new ProviderCustomResourceProperty();
                mainbranchProp.setName("mainbranch");
                mainbranchProp.setValue(mainbranchNode);
                properties.add(mainbranchProp);
            }

            // Add links if available
            if (repoRes.getLinks() != null) {
                JsonNode linksNode = objectMapper.valueToTree(repoRes.getLinks());
                ProviderCustomResourceProperty linksProp = new ProviderCustomResourceProperty();
                linksProp.setName("links");
                linksProp.setValue(linksNode);
                properties.add(linksProp);
            }

            // Add is_private flag
            ProviderCustomResourceProperty isPrivateProp = new ProviderCustomResourceProperty();
            isPrivateProp.setName("is_private");
            isPrivateProp.setValue(objectMapper.valueToTree(repoRes.getIsPrivate()));
            properties.add(isPrivateProp);

        } catch (Exception e) {
            // If serialization fails, continue without that property
        }

        return properties;
    }
}

