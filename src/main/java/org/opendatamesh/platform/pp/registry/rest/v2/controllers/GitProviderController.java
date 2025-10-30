package org.opendatamesh.platform.pp.registry.rest.v2.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.OrganizationRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.RepositoryRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.UserRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.ProviderIdentifierRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.gitproviders.CreateRepositoryReqRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.BranchRes;
import org.opendatamesh.platform.pp.registry.gitproviders.services.core.GitProviderService;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.Credential;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.CredentialFactory;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v2/pp/registry/git-providers", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Git Providers", description = "Endpoints for interacting with Git providers")
public class GitProviderController {

    @Autowired
    private GitProviderService gitProviderService;

    @Operation(summary = "Get organizations", description = "Retrieves a paginated list of organizations/groups/workspaces from a Git provider")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organizations retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/organizations")
    @ResponseStatus(HttpStatus.OK)
    public Page<OrganizationRes> getOrganizations(
            @Parameter(description = "Type of the Git provider")
            @RequestParam String providerType,
            @Parameter(description = "Base URL of the Git provider")
            @RequestParam(required = false) String providerBaseUrl,
            @Parameter(description = "Pagination and sorting parameters. Default sort is by name in descending order")
            @PageableDefault(page = 0, size = 20, sort = "name", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestHeader HttpHeaders headers
    ) {
        // Extract credentials from headers using CredentialFactory
        Credential credential = CredentialFactory.fromHeaders(headers.toSingleValueMap())
                .orElseThrow(() -> new BadRequestException("Missing or invalid credentials in headers"));

        // Create DTO from individual parameters
        ProviderIdentifierRes providerIdentifier = new ProviderIdentifierRes(providerType, providerBaseUrl);

        // Call service to get organizations
        return gitProviderService.listOrganizations(providerIdentifier, credential, pageable);
    }

    @Operation(summary = "Get repositories", description = "Retrieves a paginated list of repositories from a Git provider for a user or organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Repositories retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/repositories")
    @ResponseStatus(HttpStatus.OK)
    public Page<RepositoryRes> getRepositories(
            @Parameter(description = "Type of the Git provider")
            @RequestParam String providerType,
            @Parameter(description = "Base URL of the Git provider")
            @RequestParam(required = false) String providerBaseUrl,
            @Parameter(description = "User ID")
            @RequestParam String userId,
            @Parameter(description = "Username")
            @RequestParam String username,
            @Parameter(description = "Organization ID (optional)")
            @RequestParam(required = false) String organizationId,
            @Parameter(description = "Organization name (optional)")
            @RequestParam(required = false) String organizationName,
            @Parameter(description = "Pagination and sorting parameters. Default sort is by name in ascending order")
            @PageableDefault(page = 0, size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable,
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

        // Call service to get repositories
        return gitProviderService.listRepositories(providerIdentifier, userRes, organizationRes, credential, pageable);
    }

    @Operation(summary = "Create repository", description = "Creates a new repository in a Git provider for a user or organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Repository created successfully",
                    content = @Content(schema = @Schema(implementation = RepositoryRes.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/repositories")
    @ResponseStatus(HttpStatus.CREATED)
    public RepositoryRes createRepository(
            @Parameter(description = "Type of the Git provider")
            @RequestParam String providerType,
            @Parameter(description = "Base URL of the Git provider")
            @RequestParam(required = false) String providerBaseUrl,
            @Parameter(description = "User ID")
            @RequestParam String userId,
            @Parameter(description = "Username")
            @RequestParam String username,
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
        return gitProviderService.createRepository(providerIdentifier, userRes, organizationRes, credential, createRepositoryReqRes);
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
        return gitProviderService.listBranches(providerIdentifier, repositoryId, credential, pageable);
    }
}
