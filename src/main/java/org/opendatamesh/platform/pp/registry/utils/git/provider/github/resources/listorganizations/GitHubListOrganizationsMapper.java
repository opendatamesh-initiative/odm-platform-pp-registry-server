package org.opendatamesh.platform.pp.registry.utils.git.provider.github.resources.listorganizations;

import org.opendatamesh.platform.pp.registry.utils.git.model.Organization;

public abstract class GitHubListOrganizationsMapper {

    public static Organization toInternalModel(GitHubListOrganizationsOrganizationRes orgRes) {
        if (orgRes == null) {
            return null;
        }

        return new Organization(
                String.valueOf(orgRes.getId()),
                orgRes.getLogin(),
                orgRes.getHtmlUrl()
        );
    }
}

