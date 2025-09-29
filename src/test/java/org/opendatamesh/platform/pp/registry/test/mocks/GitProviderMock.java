package org.opendatamesh.platform.pp.registry.test.mocks;

import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.mockito.Mockito;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Optional;

/**
 * Mock implementation of GitProvider for integration tests.
 * This decorator pattern allows us to control the behavior of GitProvider
 * without using @MockBean, providing better test isolation and control.
 */
@Profile("test")
@Component
@Primary
public class GitProviderMock extends IntegrationMock implements GitProvider {

    private GitProvider mock;

    public GitProviderMock() {
        super();
    }

    @Override
    public void reset() {
        mock = Mockito.mock(GitProvider.class);
    }

    @Override
    public void checkConnection() {
        mock.checkConnection();
    }

    @Override
    public User getCurrentUser() {
        return mock.getCurrentUser();
    }

    @Override
    public Page<Organization> listOrganizations(Pageable pageable) {
        return mock.listOrganizations(pageable);
    }

    @Override
    public Optional<Organization> getOrganization(String id) {
        return mock.getOrganization(id);
    }

    @Override
    public Page<User> listMembers(Organization org, Pageable pageable) {
        return mock.listMembers(org, pageable);
    }

    @Override
    public Page<Repository> listRepositories(Organization org, User usr, Pageable pageable) {
        return mock.listRepositories(org, usr, pageable);
    }

    @Override
    public Optional<Repository> getRepository(String id) {
        return mock.getRepository(id);
    }

    @Override
    public Repository createRepository(Repository repository) {
        return mock.createRepository(repository);
    }

    @Override
    public Page<Commit> listCommits(Organization org, User usr, Repository repository, Pageable pageable) {
        return mock.listCommits(org, usr, repository, pageable);
    }

    @Override
    public Page<Branch> listBranches(Organization org, User usr, Repository repository, Pageable pageable) {
        return mock.listBranches(org, usr, repository, pageable);
    }

    @Override
    public Page<Tag> listTags(Organization org, User usr, Repository repository, Pageable pageable) {
        return mock.listTags(org, usr, repository, pageable);
    }

    @Override
    public File readRepository(RepositoryPointer pointer) {
        return mock.readRepository(pointer);
    }

    public GitProvider getMock() {
        return mock;
    }

    public void setMock(GitProvider mock) {
        this.mock = mock;
    }
}
