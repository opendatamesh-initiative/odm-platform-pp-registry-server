package org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.resources.listorganizations;

import org.opendatamesh.platform.pp.registry.githandler.model.Organization;

public abstract class GitLabListOrganizationsMapper {

    public static Organization toInternalModel(GitLabListOrganizationsGroupRes groupRes) {
        if (groupRes == null) {
            return null;
        }

        return new Organization(
                String.valueOf(groupRes.getId()),
                groupRes.getName(),
                groupRes.getWebUrl()
        );
    }
}

