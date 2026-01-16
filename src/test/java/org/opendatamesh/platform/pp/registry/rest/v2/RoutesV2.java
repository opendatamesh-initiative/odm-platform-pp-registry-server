package org.opendatamesh.platform.pp.registry.rest.v2;

public enum RoutesV2 {
    
    DATA_PRODUCTS("/api/v2/pp/registry/products"),
    DATA_PRODUCT_VERSIONS("/api/v2/pp/registry/products-versions"),
    GIT_PROVIDERS("/api/v2/pp/registry/git-providers"),
    DESCRIPTOR_VARIABLES("/api/v2/pp/registry/descriptor-variables");
    
    private final String path;
    
    RoutesV2(String path) {
        this.path = path;
    }
    
    public String getPath() {
        return path;
    }
}