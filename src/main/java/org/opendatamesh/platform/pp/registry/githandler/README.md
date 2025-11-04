<!-- TOC -->

* [Git Handler Architecture](#git-handler-architecture)
    * [Overview](#overview)
    * [Core Components](#core-components)
        * [GitProvider Interface](#gitprovider-interface)
        * [Git Provider Factory](#git-provider-factory)
        * [Git Provider Model Extension](#git-provider-model-extension)
        * [Git Provider Custom Resource Reader](#git-provider-custom-resource-reader)
        * [GitProviderModelResourceType Enum](#gitprovidermodelresourcetype-enum)
    * [Model Resource Extensions Mechanism](#model-resource-extensions-mechanism)
        * [Repository and ProviderCustomResourceProperty](#repository-and-providercustomresourceproperty)
        * [How It Works](#how-it-works)
    * [Provider Structure](#provider-structure)
    * [How-To Guide](#how-to-guide)
        * [Extend a Core Git Resource for a Specific Provider](#extend-a-core-git-resource-for-a-specific-provider)
            * [Step 1: Create a Model Extension Class](#step-1-create-a-model-extension-class)
            * [Step 2: Register the Extension in Your Provider](#step-2-register-the-extension-in-your-provider)
            * [Step 3: Use the Custom Property in Provider Operations](#step-3-use-the-custom-property-in-provider-operations)
        * [Support Reading a New Custom Resource for a Specific Git Provider](#support-reading-a-new-custom-resource-for-a-specific-git-provider)
            * [Step 1: Create API Response Models and Mapper](#step-1-create-api-response-models-and-mapper)
            * [Step 2: Implement the Resource Listing Method](#step-2-implement-the-resource-listing-method)
            * [Step 3: Create and Register a Custom Resource Reader](#step-3-create-and-register-a-custom-resource-reader)
        * [Add a New Provider](#add-a-new-provider)
            * [Step 1: Create the Provider Implementation](#step-1-create-the-provider-implementation)
            * [Step 2: Create API Response Models](#step-2-create-api-response-models)
            * [Step 3: Add Provider Type to Enum](#step-3-add-provider-type-to-enum)
            * [Step 4: Register Provider in Factory](#step-4-register-provider-in-factory)
            * [Step 5: Add Extensions (Optional)](#step-5-add-extensions-optional)
            * [Step 6: Add Custom Resource Readers (Optional)](#step-6-add-custom-resource-readers-optional)
            * [Step 7: Test Your Implementation](#step-7-test-your-implementation)
    * [Summary](#summary)

<!-- TOC -->

# Git Handler Architecture

This package provides an abstraction layer for interacting with different Git providers (GitHub, GitLab, Bitbucket,
Azure DevOps) through a unified interface. The architecture is designed to be extensible, allowing providers to add
provider-specific properties and resources while maintaining a common core model.

## Overview

The githandler package is organized into several key components:

- **`provider/`**: Core provider interfaces and implementations
- **`model/`**: Shared domain models (Repository, Branch, Commit, etc.)
- **`git/`**: Git operations and authentication context
- **`auth/`**: Credential management
- **`exceptions/`**: Custom exceptions for Git operations

## Core Components

### GitProvider Interface

The `GitProvider` interface defines the contract for all Git provider implementations. It provides a unified set of
operations that all providers must support:

- **Connection Management**: `checkConnection()` - Verify authentication
- **User Operations**: `getCurrentUser()` - Get authenticated user information
- **Organization Operations**: `listOrganizations()`, `getOrganization()`, `listMembers()`
- **Repository Operations**: `listRepositories()`, `getRepository()`, `createRepository()`
- **Repository Content**: `listCommits()`, `listBranches()`, `listTags()`
- **Authentication**: `createGitAuthContext()` - Create authentication context for Git operations

Additionally, the interface provides two extension points for provider-specific functionality:

1. **`getProviderCustomResourceDefinitions()`**: Returns metadata about additional properties that can be added to
   standard model resources (e.g., a "project" property for Bitbucket repositories)
2. **`getProviderCustomResources()`**: Retrieves provider-specific resources that don't exist in the standard model (
   e.g., Bitbucket projects, GitHub projects)

### Git Provider Factory

The `GitProviderFactory` interface and its implementation `GitProviderFactoryImpl` handle the creation of provider
instances. The factory uses the `DataProductRepoProviderType` enum to determine which provider implementation to
instantiate:

- `GITHUB` → `GitHubProvider`
- `GITLAB` → `GitLabProvider`
- `BITBUCKET` → `BitbucketProvider`
- `AZURE` → `AzureDevOpsProvider`

Each provider is initialized with:

- `baseUrl`: The API base URL for the provider
- `restTemplate`: Spring's RestTemplate for HTTP requests
- `credential`: Authentication credentials (PAT, OAuth, etc.)

### Git Provider Model Extension

The `GitProviderModelExtension` interface allows providers to extend standard model resources with additional
properties. This is used when a provider requires extra information that isn't part of the core Git model.

**Example**: Bitbucket requires a "project" property when creating repositories, which is not part of the standard
Repository model.

**Interface Methods**:

- `support(GitProviderModelResourceType)`: Indicates which resource type this extension supports
- `getCustomResourcesDefinitions()`: Returns the list of additional property definitions (name, type, required flag)

The extension mechanism works as follows:

1. A provider implementation registers one or more `GitProviderModelExtension` instances
2. When `getProviderCustomResourceDefinitions()` is called, the provider filters extensions by resource type
3. The matching extension returns the list of custom property definitions
4. These definitions are used by the API layer to validate and process provider-specific properties

### Git Provider Custom Resource Reader

The `GitProviderCustomResourceReader` interface enables providers to expose provider-specific resources that don't exist
in the standard Git model. These are resources unique to a particular provider (e.g., Bitbucket projects, GitHub
projects, GitLab groups).

**Interface Methods**:

- `support(String resourceType)`: Indicates which custom resource type this reader supports
- `getCustomResources(...)`: Retrieves a paginated list of custom resources

**Example**: Bitbucket exposes a "project" custom resource type that can be listed and used when creating repositories.

### GitProviderModelResourceType Enum

This enum defines the standard Git model resource types that can be extended. Currently, it contains:

- `REPOSITORY`: The Repository resource type

This enum is used by `GitProviderModelExtension` to specify which resource type an extension applies to. Future resource
types (e.g., `BRANCH`, `COMMIT`) can be added as needed.

## Model Resource Extensions Mechanism

### Repository and ProviderCustomResourceProperty

The standard `Repository` model includes a `providerCustomResourceProperties` field of type
`List<ProviderCustomResourceProperty>`. This allows providers to attach additional properties to repository instances
without modifying the core model.

Each `ProviderCustomResourceProperty` contains:

- `name`: The property name (e.g., "project")
- `value`: A `JsonNode` containing the property value (flexible structure)

### How It Works

1. **Extension Definition**: A provider implements `GitProviderModelExtension` to define what additional properties are
   available for a resource type
2. **Property Definition**: The extension returns `ProviderCustomResourceDefinition` objects that specify:
    - Property name
    - Property type (e.g., "OBJECT", "STRING")
    - Whether the property is required
3. **Property Storage**: When creating or retrieving repositories, provider-specific properties are stored in the
   `providerCustomResourceProperties` list
4. **Provider Usage**: The provider implementation reads these properties when performing operations (e.g., using the "
   project" property when creating a Bitbucket repository)

## Provider Structure

Each provider implementation (e.g., `BitbucketProvider`, `GitHubProvider`) is organized as follows:

- **Main Provider Class**: Implements `GitProvider` interface
- **`resources/`**: Provider-specific API response models and mappers organized by operation:
    - `checkconnection/`
    - `getcurrentuser/`
    - `listrepositories/`
    - `createrepository/`
    - `listbranches/`
    - `listcommits/`
    - `listtags/`
    - etc.
- **`modelextensions/`**: Model extension implementations (e.g., `BitbucketRepositoryExtension`)

## How-To Guide

This section provides step-by-step instructions for common extension tasks.

### Extend a Core Git Resource for a Specific Provider

When a provider requires additional properties for a standard Git resource (e.g., Repository), follow these steps:

#### Step 1: Create a Model Extension Class

Create a new class in your provider's `modelextensions/` package that implements `GitProviderModelExtension`:

```java
package org.opendatamesh.platform.pp.registry.githandler.provider.yourprovider.modelextensions;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.opendatamesh.platform.pp.registry.githandler.model.ProviderCustomResourceDefinition;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderModelExtension;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderModelResourceType;

import java.util.List;

public class YourProviderRepositoryExtension implements GitProviderModelExtension {

    public static final String CUSTOM_PROPERTY = "customProperty";

    @Override
    public boolean support(GitProviderModelResourceType resourceType) {
        return GitProviderModelResourceType.REPOSITORY.equals(resourceType);
    }

    @Override
    public List<ProviderCustomResourceDefinition> getCustomResourcesDefinitions() {
        return List.of(
                new ProviderCustomResourceDefinition(
                        CUSTOM_PROPERTY,
                        JsonNodeType.STRING.name(),  // or OBJECT, NUMBER, etc.
                        true  // required or false
                )
        );
    }
}
```

#### Step 2: Register the Extension in Your Provider

In your provider class, add the extension to the `registeredExtensions` list:

```java
public class YourProviderProvider implements GitProvider {

    private final List<GitProviderModelExtension> registeredExtensions = List.of(
            new YourProviderRepositoryExtension()
    );

    @Override
    public List<ProviderCustomResourceDefinition> getProviderCustomResourceDefinitions(
            GitProviderModelResourceType modelResourceType) {
        return this.registeredExtensions.stream()
                .filter(ext -> ext.support(modelResourceType))
                .findFirst()
                .map(GitProviderModelExtension::getCustomResourcesDefinitions)
                .orElse(List.of());
    }
}
```

#### Step 3: Use the Custom Property in Provider Operations

When implementing operations that use the extended resource, read the custom properties:

```java

@Override
public Repository createRepository(Repository repositoryToCreate) {
    // Extract custom properties
    List<ProviderCustomResourceProperty> customProps =
            repositoryToCreate.getProviderCustomResourceProperties();

    // Find and validate the custom property
    ProviderCustomResourceProperty customProp = customProps.stream()
            .filter(p -> YourProviderRepositoryExtension.CUSTOM_PROPERTY.equals(p.getName()))
            .findFirst()
            .orElseThrow(() -> new BadRequestException("Custom property is required"));

    // Use the property value in your API calls
    JsonNode propertyValue = customProp.getValue();
    // ... use propertyValue to build your provider's API request
}
```

**Note**: When reading repositories, you may also need to populate the `providerCustomResourceProperties` field by
mapping provider-specific data from the API response to the standard model.

### Support Reading a New Custom Resource for a Specific Git Provider

To expose provider-specific resources (e.g., projects, workspaces, teams) that don't exist in the standard Git model:

#### Step 1: Create API Response Models and Mapper

Create response models in `resources/listyourresource/` package to represent the provider's API response:

```java
// YourProviderListYourResourceRes.java - represents the API response
public class YourProviderListYourResourceRes {
    // fields matching provider's API response
}

// YourProviderListYourResourceMapper.java - maps to ProviderCustomResource
public class YourProviderListYourResourceMapper {
    public static ProviderCustomResource toProviderCustomResource(
            YourProviderListYourResourceRes resourceRes) {
        // Map provider response to ProviderCustomResource
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode content = mapper.createObjectNode();
        // ... populate content from resourceRes

        return new ProviderCustomResource(
                resourceRes.getId(),  // identifier
                resourceRes.getName(),  // displayName
                content  // JsonNode with additional properties
        );
    }
}
```

#### Step 2: Implement the Resource Listing Method

In your provider class, implement a method that fetches and returns the custom resources:

```java
private Page<ProviderCustomResource> listYourResources(
        MultiValueMap<String, String> parameters,
        Pageable pageable) {
    // Build API request URL with pagination
    // Extract filter parameters if needed
    // Make HTTP request to provider's API

    // Map response to ProviderCustomResource list
    List<ProviderCustomResource> resources = response.getValues().stream()
            .map(YourProviderListYourResourceMapper::toProviderCustomResource)
            .toList();

    return new PageImpl<>(resources, pageable, totalCount);
}
```

#### Step 3: Create and Register a Custom Resource Reader

In your provider's constructor or initialization, create and register a `GitProviderCustomResourceReader`:

```java
public class YourProviderProvider implements GitProvider {

    private static final String YOUR_RESOURCE_TYPE = "yourResourceType";

    private final List<GitProviderCustomResourceReader> customResourceReaders = List.of(
            createCustomResourceReader(YOUR_RESOURCE_TYPE, this::listYourResources)
    );

    // Helper method to create a custom resource reader
    private GitProviderCustomResourceReader createCustomResourceReader(
            String resourceType,
            BiFunction<MultiValueMap<String, String>, Pageable, Page<ProviderCustomResource>> resourceSupplier) {
        return new GitProviderCustomResourceReader() {
            @Override
            public boolean support(String type) {
                return resourceType.equalsIgnoreCase(type);
            }

            @Override
            public Page<ProviderCustomResource> getCustomResources(
                    MultiValueMap<String, String> parameters,
                    Pageable pageable) {
                return resourceSupplier.apply(parameters, pageable);
            }
        };
    }

    @Override
    public Page<ProviderCustomResource> getProviderCustomResources(
            String customResourceType,
            MultiValueMap<String, String> parameters,
            Pageable pageable) {
        return this.customResourceReaders.stream()
                .filter(reader -> reader.support(customResourceType))
                .findFirst()
                .map(reader -> reader.getCustomResources(parameters, pageable))
                .orElseThrow(() -> new BadRequestException(
                        "Unsupported resource type: " + customResourceType));
    }
}
```

**Note**: The resource type string (e.g., `"yourResourceType"`) should be meaningful and unique to your provider. This
is what clients will use to request the custom resource.

### Add a New Provider

To add support for an entirely new Git provider:

#### Step 1: Create the Provider Implementation

Create a new package `provider/yourprovider/` and implement the `GitProvider` interface:

```java
package org.opendatamesh.platform.pp.registry.githandler.provider.yourprovider;

import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.springframework.web.client.RestTemplate;

public class YourProviderProvider implements GitProvider {

    private final String baseUrl;
    private final RestTemplate restTemplate;
    private final Credential credential;

    public YourProviderProvider(String baseUrl, RestTemplate restTemplate, Credential credential) {
        this.baseUrl = baseUrl != null ? baseUrl : "https://api.yourprovider.com";
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
        this.credential = credential;
    }

    // Implement all required methods from GitProvider interface
    @Override
    public void checkConnection() { /* ... */ }

    @Override
    public User getCurrentUser() { /* ... */ }

    @Override
    public Page<Organization> listOrganizations(Pageable page) { /* ... */ }

    // ... implement all other required methods
}
```

#### Step 2: Create API Response Models

For each operation, create response models in `resources/` subdirectories:

```
provider/yourprovider/
├── YourProviderProvider.java
└── resources/
    ├── checkconnection/
    │   └── YourProviderCheckConnectionRes.java
    ├── getcurrentuser/
    │   ├── YourProviderGetCurrentUserRes.java
    │   └── YourProviderGetCurrentUserMapper.java
    ├── listrepositories/
    │   ├── YourProviderListRepositoriesRes.java
    │   └── YourProviderListRepositoriesMapper.java
    └── ... (other operations)
```

Create mapper classes to convert provider-specific responses to standard model objects.

#### Step 3: Add Provider Type to Enum

Add your provider type to `DataProductRepoProviderType` enum (in the `dataproduct/entities/` package):

```java
public enum DataProductRepoProviderType {
    GITHUB,
    GITLAB,
    BITBUCKET,
    AZURE,
    YOUR_PROVIDER  // Add your new type here
}
```

#### Step 4: Register Provider in Factory

Update `GitProviderFactoryImpl` to instantiate your provider:

```java

@Component
public class GitProviderFactoryImpl implements GitProviderFactory {

    @Override
    public Optional<GitProvider> getProvider(
            DataProductRepoProviderType type,
            String baseUrl,
            RestTemplate restTemplate,
            Credential credential) {
        return switch (type) {
            case GITHUB -> Optional.of(new GitHubProvider(baseUrl, restTemplate, credential));
            case GITLAB -> Optional.of(new GitLabProvider(baseUrl, restTemplate, credential));
            case BITBUCKET -> Optional.of(new BitbucketProvider(baseUrl, restTemplate, credential));
            case AZURE -> Optional.of(new AzureDevOpsProvider(baseUrl, restTemplate, credential));
            case YOUR_PROVIDER -> Optional.of(new YourProviderProvider(baseUrl, restTemplate, credential));
            default -> Optional.empty();
        };
    }
}
```

#### Step 5: Add Extensions (Optional)

If your provider requires additional properties on standard resources, follow the steps in section **a** to create model
extensions.

#### Step 6: Add Custom Resource Readers (Optional)

If your provider exposes unique resources, follow the steps in section **b** to create custom resource readers.

#### Step 7: Test Your Implementation

Create test classes to verify:

- All `GitProvider` methods work correctly
- Authentication is properly handled
- API responses are correctly mapped to standard models
- Custom properties and resources work as expected

## Summary

The githandler architecture provides:

- **Unified Interface**: Common operations across all Git providers
- **Extensibility**: Providers can add custom properties and resources
- **Type Safety**: Enum-based resource types and extension points
- **Flexibility**: JsonNode-based property values allow for complex nested structures
- **Maintainability**: Clear separation of concerns with interfaces and implementations

This design allows the system to support multiple Git providers while accommodating their unique requirements without
polluting the core model with provider-specific fields.
