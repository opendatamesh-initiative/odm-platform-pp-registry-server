package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.getorganization;

import org.opendatamesh.platform.pp.registry.githandler.model.Organization;

public abstract class BitbucketGetOrganizationMapper {

    /**
     * Maps BitbucketGetOrganizationWorkspaceRes to internal Organization model
     */
    public static Organization toInternalModel(BitbucketGetOrganizationWorkspaceRes workspaceRes) {
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
     * Creates BitbucketGetOrganizationWorkspaceRes from internal Organization model
     */
    public static BitbucketGetOrganizationWorkspaceRes fromInternalModel(Organization organization) {
        if (organization == null) {
            return null;
        }
        
        BitbucketGetOrganizationWorkspaceRes workspaceRes = new BitbucketGetOrganizationWorkspaceRes();
        workspaceRes.setUuid(organization.getId());
        workspaceRes.setName(organization.getName());
        
        // Build links from organization URL
        if (organization.getUrl() != null) {
            BitbucketGetOrganizationLinkRes html = new BitbucketGetOrganizationLinkRes(null, organization.getUrl());
            workspaceRes.setLinks(new BitbucketGetOrganizationLinksRes(null, html, null));
        }
        
        return workspaceRes;
    }
}

