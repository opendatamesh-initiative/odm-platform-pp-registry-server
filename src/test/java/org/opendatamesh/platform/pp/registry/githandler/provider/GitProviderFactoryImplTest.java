package org.opendatamesh.platform.pp.registry.githandler.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.githandler.provider.azure.AzureDevOpsProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.BitbucketProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.github.GitHubProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.gitlab.GitLabProvider;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitProviderFactoryImplTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private GitProviderFactoryImpl gitProviderFactory;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        gitProviderFactory = new GitProviderFactoryImpl(restTemplateBuilder);
    }

    @Test
    void whenProviderIdentifierBaseUrlIsNullThenThrowException() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("github", null);
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");

        // When & Then - provider constructor must enforce non-empty baseUrl
        assertThatThrownBy(() -> gitProviderFactory.buildGitProvider(providerIdentifier, headers))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("baseUrl");
    }

    @Test
    void whenProviderIdentifierBaseUrlIsBlankThenThrowException() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("github", "   ");
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");

        // When & Then - provider constructor must enforce non-empty baseUrl
        assertThatThrownBy(() -> gitProviderFactory.buildGitProvider(providerIdentifier, headers))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("baseUrl");
    }

    @Test
    void whenBuildUnauthenticatedProviderIdentifierBaseUrlIsNullThenThrowException() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("github", null);

        // When & Then
        assertThatThrownBy(() -> gitProviderFactory.buildUnauthenticatedGitProvider(providerIdentifier))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("baseUrl");
    }

    @Test
    void whenBuildUnauthenticatedProviderIdentifierBaseUrlIsBlankThenThrowException() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("github", "");

        // When & Then
        assertThatThrownBy(() -> gitProviderFactory.buildUnauthenticatedGitProvider(providerIdentifier))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("baseUrl");
    }

    @Test
    void whenProviderIdentifierIsNullThenThrowBadRequestException() {
        // Given
        GitProviderIdentifier providerIdentifier = null;
        HttpHeaders headers = new HttpHeaders();

        // When & Then
        assertThatThrownBy(() -> gitProviderFactory.buildGitProvider(providerIdentifier, headers))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("GitProviderIdentifier cannot be null");
    }

    @Test
    void whenProviderIdentifierTypeIsNullThenThrowBadRequestException() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier(null, "https://api.github.com");
        HttpHeaders headers = new HttpHeaders();

        // When & Then
        assertThatThrownBy(() -> gitProviderFactory.buildGitProvider(providerIdentifier, headers))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Git provider type cannot be null");
    }

    @Test
    void whenProviderTypeIsEmptyThenThrowBadRequestException() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("", "https://api.github.com");
        HttpHeaders headers = new HttpHeaders();

        // When & Then
        assertThatThrownBy(() -> gitProviderFactory.buildGitProvider(providerIdentifier, headers))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Unsupported Git provider type: ");
    }

    @Test
    void whenProviderTypeIsUnsupportedThenThrowBadRequestException() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("unsupported", "https://api.github.com");
        HttpHeaders headers = new HttpHeaders();

        // When & Then
        assertThatThrownBy(() -> gitProviderFactory.buildGitProvider(providerIdentifier, headers))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Unsupported Git provider type: unsupported");
    }

    @Test
    void whenProviderTypeIsGitHubThenReturnGitHubProvider() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("github", "https://api.github.com");
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");

        // When
        GitProvider provider = gitProviderFactory.buildGitProvider(providerIdentifier, headers);

        // Then
        assertThat(provider).isInstanceOf(GitHubProvider.class);
    }

    @Test
    void whenProviderTypeIsGitLabThenReturnGitLabProvider() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("gitlab", "https://gitlab.com");
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");

        // When
        GitProvider provider = gitProviderFactory.buildGitProvider(providerIdentifier, headers);

        // Then
        assertThat(provider).isInstanceOf(GitLabProvider.class);
    }

    @Test
    void whenProviderTypeIsBitbucketThenReturnBitbucketProvider() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("bitbucket", "https://api.bitbucket.org/2.0");
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");
        headers.set("x-odm-gpauth-param-username", "test-user");

        // When
        GitProvider provider = gitProviderFactory.buildGitProvider(providerIdentifier, headers);

        // Then
        assertThat(provider).isInstanceOf(BitbucketProvider.class);
    }

    @Test
    void whenProviderTypeIsAzureThenReturnAzureDevOpsProvider() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("azure", "https://dev.azure.com");
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");

        // When
        GitProvider provider = gitProviderFactory.buildGitProvider(providerIdentifier, headers);

        // Then
        assertThat(provider).isInstanceOf(AzureDevOpsProvider.class);
    }

    @Test
    void whenProviderTypeIsGitHubUpperCaseThenReturnGitHubProvider() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("GITHUB", "https://api.github.com");
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");

        // When
        GitProvider provider = gitProviderFactory.buildGitProvider(providerIdentifier, headers);

        // Then
        assertThat(provider).isInstanceOf(GitHubProvider.class);
    }

    @Test
    void whenProviderTypeIsGitHubMixedCaseThenReturnGitHubProvider() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("GitHub", "https://api.github.com");
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-odm-gpauth-type", "PAT");
        headers.set("x-odm-gpauth-param-token", "test-token");

        // When
        GitProvider provider = gitProviderFactory.buildGitProvider(providerIdentifier, headers);

        // Then
        assertThat(provider).isInstanceOf(GitHubProvider.class);
    }

    @Test
    void whenBuildUnauthenticatedProviderIdentifierIsNullThenThrowBadRequestException() {
        // Given
        GitProviderIdentifier providerIdentifier = null;

        // When & Then
        assertThatThrownBy(() -> gitProviderFactory.buildUnauthenticatedGitProvider(providerIdentifier))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("GitProviderIdentifier cannot be null");
    }

    @Test
    void whenBuildUnauthenticatedProviderIdentifierTypeIsNullThenThrowBadRequestException() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier(null, "https://api.github.com");

        // When & Then
        assertThatThrownBy(() -> gitProviderFactory.buildUnauthenticatedGitProvider(providerIdentifier))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Git provider type cannot be null");
    }

    @Test
    void whenBuildUnauthenticatedProviderTypeIsEmptyThenThrowBadRequestException() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("", "https://api.github.com");

        // When & Then
        assertThatThrownBy(() -> gitProviderFactory.buildUnauthenticatedGitProvider(providerIdentifier))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Unsupported Git provider type: ");
    }

    @Test
    void whenBuildUnauthenticatedProviderTypeIsUnsupportedThenThrowBadRequestException() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("unsupported", "https://api.github.com");

        // When & Then
        assertThatThrownBy(() -> gitProviderFactory.buildUnauthenticatedGitProvider(providerIdentifier))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Unsupported Git provider type: unsupported");
    }

    @Test
    void whenBuildUnauthenticatedProviderTypeIsGitHubThenReturnGitHubProvider() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("github", "https://api.github.com");

        // When
        GitProvider provider = gitProviderFactory.buildUnauthenticatedGitProvider(providerIdentifier);

        // Then
        assertThat(provider).isInstanceOf(GitHubProvider.class);
    }

    @Test
    void whenBuildUnauthenticatedProviderTypeIsGitLabThenReturnGitLabProvider() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("gitlab", "https://gitlab.com");

        // When
        GitProvider provider = gitProviderFactory.buildUnauthenticatedGitProvider(providerIdentifier);

        // Then
        assertThat(provider).isInstanceOf(GitLabProvider.class);
    }

    @Test
    void whenBuildUnauthenticatedProviderTypeIsBitbucketThenReturnBitbucketProvider() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("bitbucket", "https://api.bitbucket.org/2.0");

        // When
        GitProvider provider = gitProviderFactory.buildUnauthenticatedGitProvider(providerIdentifier);

        // Then
        assertThat(provider).isInstanceOf(BitbucketProvider.class);
    }

    @Test
    void whenBuildUnauthenticatedProviderTypeIsAzureThenReturnAzureDevOpsProvider() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("azure", "https://dev.azure.com");

        // When
        GitProvider provider = gitProviderFactory.buildUnauthenticatedGitProvider(providerIdentifier);

        // Then
        assertThat(provider).isInstanceOf(AzureDevOpsProvider.class);
    }

    @Test
    void whenBuildUnauthenticatedProviderTypeIsGitHubUpperCaseThenReturnGitHubProvider() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("GITHUB", "https://api.github.com");

        // When
        GitProvider provider = gitProviderFactory.buildUnauthenticatedGitProvider(providerIdentifier);

        // Then
        assertThat(provider).isInstanceOf(GitHubProvider.class);
    }

    @Test
    void whenBuildUnauthenticatedProviderTypeIsGitHubMixedCaseThenReturnGitHubProvider() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("GitHub", "https://api.github.com");

        // When
        GitProvider provider = gitProviderFactory.buildUnauthenticatedGitProvider(providerIdentifier);

        // Then
        assertThat(provider).isInstanceOf(GitHubProvider.class);
    }
}

