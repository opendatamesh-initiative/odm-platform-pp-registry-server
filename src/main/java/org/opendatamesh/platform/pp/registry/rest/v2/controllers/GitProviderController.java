package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.CredentialFactory;
import org.opendatamesh.platform.pp.registry.gitproviders.services.core.GitProviderService;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.*;
import org.opendatamesh.platform.pp.registry.gitproviders.services.core.GitProviderService;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.BranchRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v2/pp/registry/git-providers", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Git Providers", description = "Endpoints for interacting with Git providers")
public class GitProviderController {

    @Autowired
    private GitProviderService gitProviderService;

    @Operation(summary = "Get organizations", description = "Retrieves a paginated list of organizations/groups/workspaces from a Git provider")
    @GetMapping("/organizations")
    @ResponseStatus(HttpStatus.OK)
    public Page<OrganizationRes> getOrganizations(
            @Parameter(description = "Git provider configuration")
            ProviderIdentifierRes providerIdentifier,
            @Parameter(description = "Search options for filtering organizations")
            OrganizationSearchOptions searchOptions,
            @Parameter(description = "Pagination and sorting parameters. Default sort is by name in descending order")
            @PageableDefault(page = 0, size = 20, sort = "name", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestHeader HttpHeaders headers
    ) {
        // Validate required parameters
        if (providerIdentifier == null || !StringUtils.hasText(providerIdentifier.getProviderType())) {
            throw new BadRequestException("Provider type is required");
        }

        // Extract credentials from headers using CredentialFactory
        Credential credential = CredentialFactory.fromHeaders(headers.toSingleValueMap())
                .orElseThrow(() -> new BadRequestException("Missing or invalid credentials in headers"));

        // Call service to get organizations
        return gitProviderService.listOrganizations(providerIdentifier, credential, pageable);
    }

    @Operation(summary = "Get repositories", description = "Retrieves a paginated list of repositories from a Git provider for a user or organization")
    @GetMapping("/repositories")
    @ResponseStatus(HttpStatus.OK)
    public Page<RepositoryRes> getRepositories(
            @Parameter(description = "Git provider configuration")
            ProviderIdentifierRes providerIdentifier,
            @Parameter(description = "Search options for filtering repositories")
            RepositorySearchOptions searchOptions,
            @Parameter(description = "Provider specific parameters.")
            @RequestParam(required = false) MultiValueMap<String, String> parameters,
            @Parameter(description = "Type of the Git provider")
            @RequestParam String providerType,
            @Parameter(description = "Base URL of the Git provider")
            @RequestParam(required = false) String providerBaseUrl,
            @Parameter(description = "Whether to show user repositories (true) or organization repositories (false)")
            @RequestParam boolean showUserRepositories,
            @Parameter(description = "Organization ID (optional)")
            @RequestParam(required = false) String organizationId,
            @Parameter(description = "Organization name (optional)")
            @RequestParam(required = false) String organizationName,
            @Parameter(description = "Pagination and sorting parameters. Default sort is by name in ascending order")
            @PageableDefault(page = 0, size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable,
            @RequestHeader HttpHeaders headers
    ) {
        // Validate required parameters
        if (providerIdentifier == null || !StringUtils.hasText(providerIdentifier.getProviderType())) {
            throw new BadRequestException("Provider type is required");
        }

        // Extract credentials from headers using CredentialFactory
        Credential credential = CredentialFactory.fromHeaders(headers.toSingleValueMap())
                .orElseThrow(() -> new BadRequestException("Missing or invalid credentials in headers"));

        // Create DTOs from individual parameters
        ProviderIdentifierRes providerIdentifier = new ProviderIdentifierRes(providerType, providerBaseUrl);
        // Create DTOs from search options
        UserRes userRes = null;
        OrganizationRes organizationRes = null;
        if (StringUtils.hasText(searchOptions.getOrganizationId())) {
            organizationRes = new OrganizationRes(searchOptions.getOrganizationId(), searchOptions.getOrganizationName(), null);
        }
        if (StringUtils.hasText(searchOptions.getUserId())) {
            userRes = new UserRes(searchOptions.getUserId(), searchOptions.getUsername());
        }

        if (userRes == null && organizationRes == null) {
            throw new BadRequestException("One between OrganizationId or UserId must be valorized.");
        }

        // Use empty map if parameters not provided
        if (parameters == null) {
            parameters = new LinkedMultiValueMap<>();
        }

        // Call service to get repositories
        return gitProviderService.listRepositories(providerIdentifier, showUserRepositories, organizationRes, credential, pageable);
        return gitProviderService.listRepositories(providerIdentifier, userRes, organizationRes, parameters, credential, pageable);
    }

    @Operation(summary = "Create repository", description = "Creates a new repository in a Git provider for a user or organization")
    @PostMapping("/repositories")
    @ResponseStatus(HttpStatus.CREATED)
    public RepositoryRes createRepository(
            @Parameter(description = "Type of the Git provider")
            @RequestParam String providerType,
            @Parameter(description = "Base URL of the Git provider")
            @RequestParam(required = false) String providerBaseUrl,
            @Parameter(description = "Organization ID (optional)")
            @RequestParam(required = false) String organizationId,
            @Parameter(description = "Organization name (optional)")
            @RequestParam(required = false) String organizationName,
            @Parameter(description = "Repository creation request")
            @RequestBody CreateRepositoryReqRes createRepositoryReqRes,
            @RequestHeader HttpHeaders headers
    ) {
        // Extract credentials from headers using CredentialFactory
        Credential credential = CredentialFactory.fromHeaders(headers.toSingleValueMap())
                .orElseThrow(() -> new BadRequestException("Missing or invalid credentials in headers"));

        // Create DTOs from individual parameters
        ProviderIdentifierRes providerIdentifier = new ProviderIdentifierRes(providerType, providerBaseUrl);
        UserRes userRes = new UserRes(userId, username);
        OrganizationRes organizationRes = null;
        if (organizationId != null && !organizationId.trim().isEmpty()) {
            organizationRes = new OrganizationRes(organizationId, organizationName, null);
        }

        // Call service to create repository
        return gitProviderService.createRepository(providerIdentifier, organizationRes, credential, createRepositoryReqRes);
    }

    @Operation(summary = "Get repository branches", description = "Retrieves a paginated list of branches from a Git provider repository")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Branches retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication failed"),
            @ApiResponse(responseCode = "404", description = "Repository not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/repositories/{repositoryId}/branches")
    @ResponseStatus(HttpStatus.OK)
    public Page<BranchRes> getRepositoryBranches(
            @Parameter(description = "Repository ID", required = true)
            @PathVariable String repositoryId,
            @Parameter(description = "Owner ID")
            @RequestParam String ownerId,
            @Parameter(description = "Type of the Git provider")
            @RequestParam String providerType,
            @Parameter(description = "Base URL of the Git provider")
            @RequestParam(required = false) String providerBaseUrl,
            @Parameter(description = "Pagination and sorting parameters. Default sort is by name in ascending order")
            @PageableDefault(page = 0, size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable,
            @RequestHeader HttpHeaders headers
    ) {
        // Extract credentials from headers using CredentialFactory
        Credential credential = CredentialFactory.fromHeaders(headers.toSingleValueMap())
                .orElseThrow(() -> new BadRequestException("Missing or invalid credentials in headers"));

        // Create DTO from individual parameters
        ProviderIdentifierRes providerIdentifier = new ProviderIdentifierRes(providerType, providerBaseUrl);

        // Call service to get branches
        return gitProviderService.listBranches(providerIdentifier, repositoryId, ownerId, credential, pageable);
    }

    @Operation(summary = "Get custom provider resource definition", description = "Retrieves a resource custom definition given a specific provider.")
    @GetMapping("/custom-resources/definitions")
    @ResponseStatus(HttpStatus.OK)
    public ProviderCustomResourcesDefinitionsRes getProviderCustomResourcesDefinitions(
            @Parameter(description = "Name of the resource type", example = "repository", required = true)
            @RequestParam("resourceName") String resourceName,
            @Parameter(description = "Git provider configuration")
            ProviderIdentifierRes providerIdentifier
    ) {
        // Validate required parameters
        if (providerIdentifier == null || !StringUtils.hasText(providerIdentifier.getProviderType())) {
            throw new BadRequestException("Provider type is required");
        }

        return gitProviderService.getProviderCustomResourcesDefinitions(providerIdentifier, resourceName);
    }

    @Operation(summary = "Get provider-specific custom resources", description = "Retrieves a paginated list of provider-specific custom resources for a given resource type. Each Git provider may expose provider-specific resources that are not part of the standard Git model (e.g., Bitbucket projects, GitHub projects).")
    @GetMapping("/custom-resources")
    @ResponseStatus(HttpStatus.OK)
    public Page<ProviderCustomResourceRes> getProviderCustomResources(
            @Parameter(description = "Type of the custom resource to retrieve (e.g., 'project')", example = "project", required = true)
            @RequestParam("resourceType") String resourceType,
            @Parameter(description = "Git provider configuration")
            ProviderIdentifierRes providerIdentifier,
            @Parameter(description = "Filter parameters for the custom resources (e.g., organization filter). Can be passed as query parameters.")
            @RequestParam(required = false) MultiValueMap<String, String> parameters,
            @Parameter(description = "Pagination and sorting parameters. Default sort is by displayName in ascending order")
            @PageableDefault(page = 0, size = 20, sort = "displayName", direction = Sort.Direction.ASC)
            Pageable pageable,
            @RequestHeader HttpHeaders headers
    ) {
        // Validate required parameters
        if (providerIdentifier == null || !StringUtils.hasText(providerIdentifier.getProviderType())) {
            throw new BadRequestException("Provider type is required");
        }

        // Extract credentials from headers using CredentialFactory
        Credential credential = CredentialFactory.fromHeaders(headers.toSingleValueMap())
                .orElseThrow(() -> new BadRequestException("Missing or invalid credentials in headers"));

        // Use empty map if parameters not provided
        if (parameters == null) {
            parameters = new LinkedMultiValueMap<>();
        }

        // Call service to get custom resources
        return gitProviderService.getProviderCustomResources(providerIdentifier, resourceType, parameters, credential, pageable);
    }

}
