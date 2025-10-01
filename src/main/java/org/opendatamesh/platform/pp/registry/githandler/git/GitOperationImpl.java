package org.opendatamesh.platform.pp.registry.githandler.git;


import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.opendatamesh.platform.pp.registry.githandler.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GitOperationImpl implements GitOperation {

    @Override
    public File getRepositoryContent(RepositoryPointer pointer, GitAuthContext ctx) {
        if (pointer == null || pointer.getRepository() == null || ctx == null) {
            throw new IllegalArgumentException("RepositoryPointer and GitAuthContext cannot be null");
        }

        try {
            // Create temporary directory for cloning
            Path tempDir = Files.createTempDirectory("git-repo-");
            File localRepo = tempDir.toFile();

            // Determine clone URL based on transport protocol
            String cloneUrl = getCloneUrl(pointer.getRepository(), ctx.transportProtocol);

            // Setup authentication
            CredentialsProvider credentialsProvider = setupCredentials(ctx);

            // Clone the repository with shallow clone (depth=1)
            CloneCommand cloneCommand = Git.cloneRepository()
                    .setURI(cloneUrl)
                    .setDirectory(localRepo)
                    .setCredentialsProvider(credentialsProvider)
                    .setDepth(1); // Shallow clone - only get the latest commit

            // Setup SSH transport if needed
            if (ctx.transportProtocol == GitAuthContext.TransportProtocol.SSH) {
                setupSshAuthentication(ctx);
            }

            // Use try-with-resources to ensure Git is properly closed
            try (Git git = cloneCommand.call()) {
                // Checkout specific pointer (branch, commit, or tag)
                checkoutPointer(git, pointer);
            }

            return localRepo;

        } catch (GitAPIException | IOException e) {
            throw new RuntimeException("Failed to clone repository: " + e.getMessage(), e);
        }
    }

    @Override
    public void addFiles(File repoDir, List<String> filePatterns) {
        try (Git git = Git.open(repoDir)) {
            AddCommand add = git.add();
            for (String pattern : filePatterns) {
                add.addFilepattern(pattern);
            }
            add.call();
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException("Failed to add files: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean commit(File repoDir, String message) {
        try (Git git = Git.open(repoDir)) {
            Status status = git.status().call();
            if (status.isClean()) {
                return false; // no changes
            }
            git.commit()
                    .setMessage(message)
                    .call();
            return true;
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException("Failed to commit: " + e.getMessage(), e);
        }
    }

    @Override
    public void push(File repoDir, GitAuthContext ctx) {
        try (Git git = Git.open(repoDir)) {
            CredentialsProvider cp = setupCredentials(ctx);
            git.push()
                    .setRemote("origin")
                    .setCredentialsProvider(cp)
                    .setPushAll()
                    .call();
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException("Failed to push: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean addCommitPush(File repoDir,
                                 List<String> filePatterns,
                                 String message,
                                 GitAuthContext ctx) {
        addFiles(repoDir, filePatterns);
        boolean committed = commit(repoDir, message);
        if (committed) {
            push(repoDir, ctx);
        }
        return committed;
    }

    private String getCloneUrl(Repository repository,
                               GitAuthContext.TransportProtocol protocol) {
        if (protocol == GitAuthContext.TransportProtocol.SSH) {
            return repository.getCloneUrlSsh();
        } else {
            return repository.getCloneUrlHttp();
        }
    }

    private CredentialsProvider setupCredentials(GitAuthContext ctx) {
        if (ctx.transportProtocol == GitAuthContext.TransportProtocol.SSH) {
            return null; // SSH uses key-based auth, no credentials provider needed
        } else {
            // HTTP authentication using headers
            if (ctx.httpAuthHeaders != null) {
                String username = ctx.httpAuthHeaders.getFirst("username");
                String password = ctx.httpAuthHeaders.getFirst("password");
                if (username != null && password != null) {
                    return new UsernamePasswordCredentialsProvider(username, password);
                } else {
                    // PAT tokens are used for authentication and the username is ignored,
                    // so we can supply a placeholder username and use the token as the password.
                    String token = ctx.httpAuthHeaders.getFirst("Authorization");
                    return new UsernamePasswordCredentialsProvider("dummy", token);
                }
            }
            return null; // No authentication
        }
    }

    private void setupSshAuthentication(GitAuthContext ctx) {
        // For now, we'll use a simplified approach
        // In a real implementation, you might want to use system SSH keys
        // or implement a more sophisticated SSH session factory
        System.setProperty("jsch.knownhosts", "/dev/null"); // Disable host key checking for demo
    }

    private void checkoutPointer(Git git, RepositoryPointer pointer) throws GitAPIException {
        if (pointer instanceof RepositoryPointerBranch) {
            RepositoryPointerBranch branchPointer = (RepositoryPointerBranch) pointer;
            git.checkout()
                    .setName(branchPointer.getName())
                    .call();
        } else if (pointer instanceof RepositoryPointerCommit) {
            RepositoryPointerCommit commitPointer = (RepositoryPointerCommit) pointer;
            git.checkout()
                    .setName(commitPointer.getHash())
                    .call();
        } else if (pointer instanceof RepositoryPointerTag) {
            RepositoryPointerTag tagPointer = (RepositoryPointerTag) pointer;
            git.checkout()
                    .setName(tagPointer.getName())
                    .call();
        } else {
            // Default to main/master branch
            try {
                git.checkout().setName("main").call();
            } catch (GitAPIException e) {
                // Try master if main doesn't exist
                git.checkout().setName("master").call();
            }
        }
    }
}
