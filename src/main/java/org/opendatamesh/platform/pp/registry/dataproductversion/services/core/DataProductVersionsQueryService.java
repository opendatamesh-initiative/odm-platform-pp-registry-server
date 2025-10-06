package org.opendatamesh.platform.pp.registry.dataproductversion.services.core;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionShort;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionSearchOptions;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionShortRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service for querying and retrieving multiple DataProductVersion entities.
 * <p>
 * This service is optimized for read operations that involve multiple DataProductVersion instances,
 * such as listing, searching, and filtering. It returns lightweight resources without descriptor
 * content to provide better performance when dealing with large datasets.
 *
 * <p><strong>Supported Operations:</strong></p>
 * <ul>
 *   <li>Paginated search and listing of data product versions</li>
 *   <li>Filtering by various criteria (data product UUID, name, tag, validation state)</li>
 *   <li>Sorting by multiple fields</li>
 * </ul>
 *
 * <p><strong>Performance Benefits:</strong></p>
 * <ul>
 *   <li>Excludes descriptor content to reduce payload size</li>
 *   <li>Optimized for pagination and large result sets</li>
 *   <li>Returns {@link DataProductVersionShortRes} for faster serialization</li>
 * </ul>
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li>Data product version listings in UI</li>
 *   <li>Search functionality</li>
 *   <li>Bulk operations that don't require full entity data</li>
 * </ul>
 *
 * @see DataProductVersionCrudService for individual entity operations
 * @see DataProductVersionShortRes for the lightweight resource format
 */
public interface DataProductVersionsQueryService {

    /**
     * Find all data product versions with pagination, returning short resources (without descriptor)
     * for better performance when listing multiple versions.
     *
     * @param pageable      pagination and sorting parameters
     * @param searchOptions filtering criteria for the search
     * @return paginated list of data product version short resources
     */
    Page<DataProductVersionShortRes> findAllResourcesShort(Pageable pageable, DataProductVersionSearchOptions searchOptions);

    Page<DataProductVersionShort> findAllShort(Pageable pageable, DataProductVersionSearchOptions searchOptions);
}
