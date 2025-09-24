package org.opendatamesh.platform.pp.registry.rest.v1.controllers;

import io.swagger.v3.oas.annotations.Hidden;
import org.opendatamesh.platform.pp.registry.dataproduct.services.DataProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Hidden
@RequestMapping(value = "/api/v1/dataproducts", produces = MediaType.APPLICATION_JSON_VALUE)
public class DataProductController {

    @Autowired
    private DataProductService dataProductService;
}
