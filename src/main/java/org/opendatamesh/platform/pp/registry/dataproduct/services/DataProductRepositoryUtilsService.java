package org.opendatamesh.platform.pp.registry.dataproduct.services;

import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository.BranchRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository.CommitRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository.CommitSearchOptions;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository.TagRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;

public interface DataProductRepositoryUtilsService {

    Page<CommitRes> listCommits(String dataProductUuid, HttpHeaders headers, CommitSearchOptions searchOptions, Pageable pageable);

    Page<BranchRes> listBranches(String dataProductUuid, HttpHeaders headers, Pageable pageable);

    Page<TagRes> listTags(String dataProductUuid, HttpHeaders headers, Pageable pageable);

    TagRes addTag(String dataProductUuid, TagRes tagRes, HttpHeaders headers);
}
