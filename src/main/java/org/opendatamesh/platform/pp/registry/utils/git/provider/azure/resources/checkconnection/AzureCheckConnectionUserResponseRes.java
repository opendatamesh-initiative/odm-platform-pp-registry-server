package org.opendatamesh.platform.pp.registry.utils.git.provider.azure.resources.checkconnection;

public class AzureCheckConnectionUserResponseRes {
    private AzureCheckConnectionUserRes authenticatedUser;

    public AzureCheckConnectionUserRes getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void setAuthenticatedUser(AzureCheckConnectionUserRes authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }
}

