package org.opendatamesh.platform.pp.registry.rest.v2;

public enum RoutesV2 {
    
    DATA_PRODUCTS("/api/v2/pp/registry/products");
    
    private final String path;
    
    RoutesV2(String path) {
        this.path = path;
    }
    
    public String getPath() {
        return path;
    }
}