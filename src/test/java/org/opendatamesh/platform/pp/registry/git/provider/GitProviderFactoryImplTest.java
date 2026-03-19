package org.opendatamesh.platform.pp.registry.git.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.git.exceptions.GitProviderConfigurationException;
import org.opendatamesh.platform.git.provider.GitProvider;
import org.opendatamesh.platform.git.provider.GitProviderIdentifier;
import org.opendatamesh.platform.git.provider.azure.AzureDevOpsProvider;
import org.opendatamesh.platform.git.provider.bitbucket.BitbucketProvider;
import org.opendatamesh.platform.git.provider.github.GitHubProvider;
import org.opendatamesh.platform.git.provider.gitlab.GitLabProvider;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

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
    void whenProviderIdentifierIsNullThenThrowGitProviderConfigurationException() {
        // Given
        GitProviderIdentifier providerIdentifier = null;
        HttpHeaders headers = new HttpHeaders();

        // When & Then
        assertThatThrownBy(() -> gitProviderFactory.buildGitProvider(providerIdentifier, headers))
                .isInstanceOf(GitProviderConfigurationException.class)
                .hasMessage("GitProviderIdentifier cannot be null");
    }

    @Test
    void whenProviderIdentifierTypeIsNullThenThrowGitProviderConfigurationException() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier(null, "https://api.github.com");
        HttpHeaders headers = new HttpHeaders();

        // When & Then
        assertThatThrownBy(() -> gitProviderFactory.buildGitProvider(providerIdentifier, headers))
                .isInstanceOf(GitProviderConfigurationException.class)
                .hasMessage("Git provider type cannot be null");
    }

    @Test
    void whenProviderTypeIsEmptyThenThrowGitProviderConfigurationException() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("", "https://api.github.com");
        HttpHeaders headers = new HttpHeaders();

        // When & Then
        assertThatThrownBy(() -> gitProviderFactory.buildGitProvider(providerIdentifier, headers))
                .isInstanceOf(GitProviderConfigurationException.class)
                .hasMessage("Unsupported Git provider type: ");
    }

    @Test
    void whenProviderTypeIsUnsupportedThenThrowGitProviderConfigurationException() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("unsupported", "https://api.github.com");
        HttpHeaders headers = new HttpHeaders();

        // When & Then
        assertThatThrownBy(() -> gitProviderFactory.buildGitProvider(providerIdentifier, headers))
                .isInstanceOf(GitProviderConfigurationException.class)
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
    void whenBuildUnauthenticatedProviderIdentifierIsNullThenThrowGitProviderConfigurationException() {
        // Given
        GitProviderIdentifier providerIdentifier = null;

        // When & Then
        assertThatThrownBy(() -> gitProviderFactory.buildGitProviderExtension(providerIdentifier))
                .isInstanceOf(GitProviderConfigurationException.class)
                .hasMessage("GitProviderIdentifier cannot be null");
    }

    @Test
    void whenBuildUnauthenticatedProviderIdentifierTypeIsNullThenThrowGitProviderConfigurationException() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier(null, "https://api.github.com");

        // When & Then
        assertThatThrownBy(() -> gitProviderFactory.buildGitProviderExtension(providerIdentifier))
                .isInstanceOf(GitProviderConfigurationException.class)
                .hasMessage("Git provider type cannot be null");
    }

    @Test
    void whenBuildUnauthenticatedProviderTypeIsEmptyThenThrowGitProviderConfigurationException() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("", "https://api.github.com");

        // When & Then
        assertThatThrownBy(() -> gitProviderFactory.buildGitProviderExtension(providerIdentifier))
                .isInstanceOf(GitProviderConfigurationException.class)
                .hasMessage("Unsupported Git provider type: ");
    }

    @Test
    void whenBuildUnauthenticatedProviderTypeIsUnsupportedThenThrowGitProviderConfigurationException() {
        // Given
        GitProviderIdentifier providerIdentifier = new GitProviderIdentifier("unsupported", "https://api.github.com");

        // When & Then
        assertThatThrownBy(() -> gitProviderFactory.buildGitProviderExtension(providerIdentifier))
                .isInstanceOf(GitProviderConfigurationException.class)
                .hasMessage("Unsupported Git provider type: unsupported");
    }
}

