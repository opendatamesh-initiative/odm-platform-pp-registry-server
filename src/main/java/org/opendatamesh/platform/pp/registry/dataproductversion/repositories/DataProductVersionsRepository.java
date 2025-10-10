package org.opendatamesh.platform.pp.registry.dataproductversion.repositories;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.utils.repositories.PagingAndSortingAndSpecificationExecutorRepository;

public interface DataProductVersionsRepository extends PagingAndSortingAndSpecificationExecutorRepository<DataProductVersion, String> {

    // JPA named methods for uniqueness validation
    
    /**
     * Check if a DataProductVersion exists by tag and dataProductUuid (case-insensitive)
     */
    boolean existsByTagIgnoreCaseAndDataProductUuid(String tag, String dataProductUuid);
    
    /**
     * Check if a DataProductVersion exists by tag and dataProductUuid excluding a specific UUID (case-insensitive)
     */
    boolean existsByTagIgnoreCaseAndDataProductUuidAndUuidNot(String tag, String dataProductUuid, String excludeUuid);
}
