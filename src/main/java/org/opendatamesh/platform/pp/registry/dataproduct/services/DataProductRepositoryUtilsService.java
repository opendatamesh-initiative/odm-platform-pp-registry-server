package org.opendatamesh.platform.pp.registry.dataproduct.services;

import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository.BranchRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository.CommitRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository.CommitSearchOptions;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository.TagRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;

public interface DataProductRepositoryUtilsService {
    
    /**
     * List commits for a data product's repository
     *
     * @param dataProductUuid the data product UUID
     * @param credential PAT credentials for authentication
     * @param searchOptions search options for filtering commits (e.g., by tag names)
     * @param pageable pagination information
     * @return page of commits
     */
    Page<CommitRes> listCommits(String dataProductUuid, HttpHeaders headers, CommitSearchOptions searchOptions,Pageable pageable);
    
    /**
     * List branches for a data product's repository
     *
     * @param dataProductUuid the data product UUID
     * @param credential PAT credentials for authentication
     * @param pageable pagination information
     * @return page of branches
     */
    Page<BranchRes> listBranches(String dataProductUuid, HttpHeaders headers, Pageable pageable);
    
    /**
     * List tags for a data product's repository
     *
     * @param dataProductUuid the data product UUID
     * @param credential PAT credentials for authentication
     * @param pageable pagination information
     * @return page of tags
     */
    Page<TagRes> listTags(String dataProductUuid, HttpHeaders headers, Pageable pageable);

    /**
     * Add a tag to a data product's repository
     *
     * @param dataProductUuid the data product UUID
     * @param tagRes          the tag resource containing tag details to add
     * @param headers         HTTP headers containing authentication or provider
     *                        info
     * @return the added tag resource
     */
    TagRes addTag(String dataProductUuid, TagRes tagRes, HttpHeaders headers);
}
