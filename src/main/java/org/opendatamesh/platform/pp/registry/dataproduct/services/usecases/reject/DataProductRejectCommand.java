package org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.reject;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;

public record DataProductRejectCommand(DataProduct dataProduct) {
}
