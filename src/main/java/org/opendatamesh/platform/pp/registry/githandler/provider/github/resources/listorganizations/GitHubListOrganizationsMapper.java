package org.opendatamesh.platform.pp.registry.githandler.provider.github.resources.listorganizations;

import org.opendatamesh.platform.pp.registry.githandler.model.Organization;

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

