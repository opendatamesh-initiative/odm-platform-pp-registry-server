package org.opendatamesh.platform.pp.registry.dataproduct.services;

import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.BranchRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.CommitRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.TagRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.UserRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.OrganizationRes;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DataProductUtilsService {
    
    /**
     * List commits for a data product's repository
     *
     * @param dataProductUuid the data product UUID
     * @param userRes the user information
     * @param organizationRes the organization information (optional, for user repositories)
     * @param credential PAT credentials for authentication
     * @param pageable pagination information
     * @return page of commits
     */
    Page<CommitRes> listCommits(String dataProductUuid, UserRes userRes, OrganizationRes organizationRes, Credential credential, Pageable pageable);
    
    /**
     * List branches for a data product's repository
     *
     * @param dataProductUuid the data product UUID
     * @param userRes the user information
     * @param organizationRes the organization information (optional, for user repositories)
     * @param credential PAT credentials for authentication
     * @param pageable pagination information
     * @return page of branches
     */
    Page<BranchRes> listBranches(String dataProductUuid, UserRes userRes, OrganizationRes organizationRes, Credential credential, Pageable pageable);
    
    /**
     * List tags for a data product's repository
     *
     * @param dataProductUuid the data product UUID
     * @param userRes the user information
     * @param organizationRes the organization information (optional, for user repositories)
     * @param credential PAT credentials for authentication
     * @param pageable pagination information
     * @return page of tags
     */
    Page<TagRes> listTags(String dataProductUuid, UserRes userRes, OrganizationRes organizationRes, Credential credential, Pageable pageable);
}
