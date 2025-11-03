package org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.getcurrentuser;

import org.opendatamesh.platform.pp.registry.githandler.model.User;

public abstract class AzureGetCurrentUserMapper {

    public static User toInternalModel(AzureGetCurrentUserUserRes authenticatedUser, String baseUrl) {
        if (authenticatedUser == null) {
            return null;
        }

        // Extract email from descriptor if available
        String email = null;
        if (authenticatedUser.getDescriptor() != null && authenticatedUser.getDescriptor().contains("\\")) {
            String[] parts = authenticatedUser.getDescriptor().split("\\\\");
            if (parts.length > 1) {
                email = parts[1];
            }
        }

        // Use the providerDisplayName as the display name, fallback to email or subject descriptor
        String displayName = authenticatedUser.getProviderDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = email != null ? email : authenticatedUser.getSubjectDescriptor();
        }

        // Use email as username if available, otherwise use subject descriptor
        String username = email != null ? email : authenticatedUser.getSubjectDescriptor();

        return new User(
                authenticatedUser.getId(),
                username,
                displayName,
                null, // No avatar URL available from connectionData
                baseUrl + "/_usersSettings/about"
        );
    }
}

