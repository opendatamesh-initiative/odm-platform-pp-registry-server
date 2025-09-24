package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.OrganizationRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.RepositoryRes;
import org.opendatamesh.platform.pp.registry.gitproviders.services.core.GitProviderService;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/git-providers", produces = MediaType.APPLICATION_JSON_VALUE)
public class GitProviderController {

    @Autowired
    private GitProviderService gitProviderService;

    @GetMapping("/organizations")
    @Operation(summary = "Gets all available organizations/groups/workspaces from a Git provider")
    public Page<OrganizationRes> getOrganizations(
            @Parameter(description = "Type of the Git provider (e.g., github, gitlab, bitbucket)")
            @RequestParam(value = "providerType") String providerType,
            @Parameter(description = "Base URL of the Git provider")
            @RequestParam(value = "providerBaseUrl", required = false) String providerBaseUrl,
            @Parameter(description = "Pagination and sorting parameters.")
            @PageableDefault(page = 0, size = 20, sort = "name", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestHeader HttpHeaders headers) {

        //TODO: refactor after merge
        // Extract PAT from headers
        String patUsername = headers.getFirst("x-odm-gpauth-param-username");
        String patToken = headers.getFirst("x-odm-gpauth-param-token");
        PatCredential credential = new PatCredential(patUsername, patToken);

        // Call service to get organizations
        return gitProviderService.listOrganizations(providerType, providerBaseUrl, pageable, credential);
    }

    @GetMapping("/repositories")
    @Operation(summary = "Gets all repositories from a Git provider for a user or organization")
    public Page<RepositoryRes> getRepositories(
            @Parameter(description = "Type of the Git provider (e.g., github, gitlab, bitbucket)")
            @RequestParam(value = "providerType") String providerType,
            @Parameter(description = "Base URL of the Git provider")
            @RequestParam(value = "providerBaseUrl", required = false) String providerBaseUrl,
            @Parameter(description = "The user ID making the request")
            @RequestParam(value = "userId") String userId,
            @Parameter(description = "The username making the request")
            @RequestParam(value = "username") String username,
            @Parameter(description = "The organization ID (optional, for user repositories)")
            @RequestParam(value = "organizationId", required = false) String organizationId,
            @Parameter(description = "The organization name (optional, for user repositories)")
            @RequestParam(value = "organizationName", required = false) String organizationName,
            @Parameter(description = "Pagination and sorting parameters.")
            @PageableDefault(page = 0, size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable,
            @RequestHeader HttpHeaders headers) {

        //TODO: refactor after merge
        // Extract PAT from headers
        String patUsername = headers.getFirst("x-odm-gpauth-param-username");
        String patToken = headers.getFirst("x-odm-gpauth-param-token");
        PatCredential credential = new PatCredential(patUsername, patToken);

        // Call service to get repositories
        return gitProviderService.listRepositories(providerType, providerBaseUrl, userId, username, organizationId, organizationName, credential, pageable);
    }
}
