package org.opendatamesh.platform.pp.registry.utils.git.provider.github.resources.getorganization;

import org.opendatamesh.platform.pp.registry.utils.git.model.Organization;

public abstract class GitHubGetOrganizationMapper {

    public static Organization toInternalModel(GitHubGetOrganizationOrganizationRes orgRes) {
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

