package org.opendatamesh.platform.pp.registry.dataproduct.services.core;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductSearchOptions;
import org.opendatamesh.platform.pp.registry.utils.services.GenericMappedAndFilteredCrudService;

public interface DataProductService extends GenericMappedAndFilteredCrudService<DataProductSearchOptions, DataProductRes, DataProduct, String> {
}
