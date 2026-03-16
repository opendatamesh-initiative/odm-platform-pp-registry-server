package org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.repository;

import org.mapstruct.Mapper;
import org.opendatamesh.platform.pp.registry.utils.git.model.Branch;

@Mapper(componentModel = "spring")
public interface BranchMapper {
    BranchRes toRes(Branch branch);
}
