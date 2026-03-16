package org.opendatamesh.platform.pp.registry.utils.git.provider.gitlab.resources.getorganization;

import org.opendatamesh.platform.pp.registry.utils.git.model.Organization;

public abstract class GitLabGetOrganizationMapper {

    public static Organization toInternalModel(GitLabGetOrganizationGroupRes groupRes) {
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

