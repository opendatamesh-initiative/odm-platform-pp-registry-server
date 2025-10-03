package org.opendatamesh.platform.pp.registry.dataproductversion.services.core;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionSearchOptions;
import org.opendatamesh.platform.pp.registry.utils.services.GenericMappedAndFilteredCrudService;

/**
 * Service for CRUD operations on individual DataProductVersion entities.
 * 
 * This service provides full CRUD functionality for managing single DataProductVersion instances,
 * including all descriptor content. It is designed for operations that require complete entity
 * data such as creating, updating, or retrieving individual versions.
 * 
 * <p><strong>Supported Operations:</strong></p>
 * <ul>
 *   <li>Create new data product versions</li>
 *   <li>Read individual data product versions by UUID</li>
 *   <li>Update existing data product versions</li>
 *   <li>Delete data product versions</li>
 * </ul>
 * 
 * <p><strong>Note:</strong> Paginated reads are disabled for this service to encourage
 * the use of {@link DataProductVersionsQueryService} for listing operations, which provides
 * better performance by excluding descriptor content.</p>
 * 
 * @see DataProductVersionsQueryService for paginated read operations
 */
public interface DataProductVersionCrudService extends GenericMappedAndFilteredCrudService<DataProductVersionSearchOptions, DataProductVersionRes, DataProductVersion, String> {
    
}
