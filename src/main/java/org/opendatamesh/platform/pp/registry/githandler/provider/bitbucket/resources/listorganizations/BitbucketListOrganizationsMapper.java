package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listorganizations;

import org.opendatamesh.platform.pp.registry.githandler.model.Organization;

public abstract class BitbucketListOrganizationsMapper {

    /**
     * Maps BitbucketListOrganizationsWorkspaceRes to internal Organization model
     */
    public static Organization toInternalModel(BitbucketListOrganizationsWorkspaceRes workspaceRes) {
        if (workspaceRes == null) {
            return null;
        }

        String htmlUrl = null;
        if (workspaceRes.getLinks() != null && workspaceRes.getLinks().getHtml() != null) {
            htmlUrl = workspaceRes.getLinks().getHtml().getHref();
        }

        return new Organization(workspaceRes.getUuid(), workspaceRes.getName(), htmlUrl);
    }

    /**
     * Creates BitbucketListOrganizationsWorkspaceRes from internal Organization model
     */
    public static BitbucketListOrganizationsWorkspaceRes fromInternalModel(Organization organization) {
        if (organization == null) {
            return null;
        }

        BitbucketListOrganizationsWorkspaceRes workspaceRes = new BitbucketListOrganizationsWorkspaceRes();
        workspaceRes.setUuid(organization.getId());
        workspaceRes.setName(organization.getName());

        // Build links from organization URL
        if (organization.getUrl() != null) {
            BitbucketListOrganizationsLinkRes html = new BitbucketListOrganizationsLinkRes(null, organization.getUrl());
            workspaceRes.setLinks(new BitbucketListOrganizationsLinksRes(null, html, null));
        }

        return workspaceRes;
    }
}

