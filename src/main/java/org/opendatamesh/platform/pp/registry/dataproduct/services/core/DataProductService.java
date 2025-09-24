package org.opendatamesh.platform.pp.registry.dataproduct.services.core;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.BranchRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.CommitRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductSearchOptions;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.TagRes;
import org.opendatamesh.platform.pp.registry.utils.services.GenericMappedAndFilteredCrudService;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DataProductService extends GenericMappedAndFilteredCrudService<DataProductSearchOptions, DataProductRes, DataProduct, String> {
    
    /**
     * List commits for a data product's repository
     *
     * @param dataProductUuid the data product UUID
     * @param userId the user ID making the request
     * @param username the username making the request
     * @param organizationId the organization ID (optional, for user repositories)
     * @param organizationName the organization name (optional, for user repositories)
     * @param pageable pagination information
     * @param credential PAT credentials for authentication
     * @return page of commits
     */
    Page<CommitRes> listCommits(String dataProductUuid, String userId, String username, String organizationId, String organizationName, PatCredential credential, Pageable pageable);
    
    /**
     * List branches for a data product's repository
     *
     * @param dataProductUuid the data product UUID
     * @param userId the user ID making the request
     * @param username the username making the request
     * @param organizationId the organization ID (optional, for user repositories)
     * @param organizationName the organization name (optional, for user repositories)
     * @param pageable pagination information
     * @param credential PAT credentials for authentication
     * @return page of branches
     */
    Page<BranchRes> listBranches(String dataProductUuid, String userId, String username, String organizationId, String organizationName, PatCredential credential, Pageable pageable);
    
    /**
     * List tags for a data product's repository
     *
     * @param dataProductUuid the data product UUID
     * @param userId the user ID making the request
     * @param username the username making the request
     * @param organizationId the organization ID (optional, for user repositories)
     * @param organizationName the organization name (optional, for user repositories)
     * @param pageable pagination information
     * @param credential PAT credentials for authentication
     * @return page of tags
     */
    Page<TagRes> listTags(String dataProductUuid, String userId, String username, String organizationId, String organizationName, PatCredential credential, Pageable pageable);
}
