package org.opendatamesh.platform.pp.registry.dataproduct.services;

import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository.BranchRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository.CommitRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository.CommitSearchOptions;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository.TagRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;

/**
 * This service is used to interact with Git Providers when the repository
 * configuration is stored inside a DataProduct.
 * When {@code repositoryKey} is blank, operations target the root repository;
 * when set, they target the additional repository with that key.
 */
public interface DataProductRepositoryUtilsService {
    Page<CommitRes> listCommits(String dataProductUuid, String repositoryKey, HttpHeaders headers, CommitSearchOptions searchOptions, Pageable pageable);

    Page<BranchRes> listBranches(String dataProductUuid, String repositoryKey, HttpHeaders headers, Pageable pageable);

    Page<TagRes> listTags(String dataProductUuid, String repositoryKey, HttpHeaders headers, Pageable pageable);

    TagRes addTag(String dataProductUuid, String repositoryKey, TagRes tagRes, HttpHeaders headers);
}
