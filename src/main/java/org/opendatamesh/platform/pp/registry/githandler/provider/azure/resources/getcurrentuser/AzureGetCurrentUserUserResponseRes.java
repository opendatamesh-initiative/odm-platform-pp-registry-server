package org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.getcurrentuser;

public class AzureGetCurrentUserUserResponseRes {
    private AzureGetCurrentUserUserRes authenticatedUser;

    public AzureGetCurrentUserUserRes getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void setAuthenticatedUser(AzureGetCurrentUserUserRes authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }
}

