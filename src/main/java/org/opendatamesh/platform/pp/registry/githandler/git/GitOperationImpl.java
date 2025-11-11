package org.opendatamesh.platform.pp.registry.githandler.git;


import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.opendatamesh.platform.pp.registry.githandler.exceptions.GitOperationException;
import org.opendatamesh.platform.pp.registry.githandler.model.*;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GitOperationImpl implements GitOperation {

    private final GitAuthContext authContext;

    public GitOperationImpl() {
        this.authContext = null;
    }

    public GitOperationImpl(GitAuthContext authContext) {
        this.authContext = authContext;
    }

    @Override
    public File initRepository(String repoName, String initialBranch, URL remoteUrl) throws GitOperationException {
        if (repoName == null || remoteUrl == null) {
            throw new GitOperationException("initRepository", "RepoName and remoteUrl cannot be null");
        }
        
        if (authContext == null) {
            throw new GitOperationException("initRepository", "GitAuthContext not set. Use constructor with GitAuthContext parameter.");
        }

        try {
            // Create temporary directory for the repository
            Path tempDir = Files.createDirectories(Paths.get("tmp", repoName));
            File localRepo = tempDir.toFile();

            // Initialize the Git repository
            Git git = Git.init().setDirectory(localRepo).setInitialBranch(initialBranch).call();

            // Add remote origin
            git.remoteAdd()
                .setName("origin")
                .setUri(new URIish(remoteUrl))
                .call();

            // Close the Git instance
            git.close();

            return localRepo;

        } catch (GitAPIException | IOException e) {
            throw new GitOperationException("initRepository", "Failed to initialize repository: " + e.getMessage(), e);
        }
    }

    @Override
    public File getRepositoryContent(RepositoryPointer pointer) throws GitOperationException {
        if (pointer == null || pointer.getRepository() == null) {
            throw new GitOperationException("getRepositoryContent", "RepositoryPointer cannot be null");
        }
        
        if (authContext == null) {
            throw new GitOperationException("getRepositoryContent", "GitAuthContext not set. Use constructor with GitAuthContext parameter.");
        }

        try {
            // Create temporary directory for cloning
            Path tempDir = Files.createTempDirectory("git-repo-");
            File localRepo = tempDir.toFile();

            // Determine clone URL based on transport protocol
            String cloneUrl = getCloneUrl(pointer.getRepository(), authContext.transportProtocol);

            // Setup authentication
            CredentialsProvider credentialsProvider = setupCredentials(authContext);

            // Setup SSH transport if needed
            if (authContext.transportProtocol == GitAuthContext.TransportProtocol.SSH) {
                setupSshAuthentication(authContext);
            }

            // Clone the repository with shallow clone (depth=1)
            CloneCommand cloneCommand = Git.cloneRepository()
            .setURI(cloneUrl)
            .setDirectory(localRepo)
            .setCredentialsProvider(credentialsProvider)
            .setDepth(1); // Shallow clone - only get the latest commit

            // Set the branch to clone if the pointer is a branch
            if (pointer instanceof RepositoryPointerBranch) {
                cloneCommand.setBranch(((RepositoryPointerBranch) pointer).getName());
            }

            // Use try-with-resources to ensure Git is properly closed
            try (Git git = cloneCommand.call()) {
                // Checkout specific pointer (branch, commit, or tag)
                checkoutPointer(git, pointer);
            }

            return localRepo;

        } catch (GitAPIException | IOException e) {
            throw new GitOperationException("getRepositoryContent", "Failed to clone repository: " + e.getMessage(), e);
        }
    }

    @Override
    public void addFiles(File repoDir, List<File> files) throws GitOperationException {
        if (files == null || files.isEmpty()) {
            return; // Nothing to add
        }
        
        try (Git git = Git.open(repoDir)) {
            AddCommand add = git.add();
            boolean hasValidFiles = false;
            
            for (File file : files) {
                // Skip null files
                if (file == null) {
                    continue;
                }
                
                // Skip non-existent files
                if (!file.exists()) {
                    continue;
                }
                
                // Skip directories
                if (file.isDirectory()) {
                    continue;
                }
                
                // Skip if not a regular file
                if (!file.isFile()) {
                    continue;
                }
                
                try {
                    // Get relative path from repository root
                    String relativePath = getRelativePath(repoDir, file);
                    add.addFilepattern(relativePath);
                    hasValidFiles = true;
                } catch (Exception e) {
                    // Skip files that can't be processed (e.g., outside repo directory)
                    continue;
                }
            }
            
            // Only call add if we have valid files to add
            if (hasValidFiles) {
                add.call();
            }
        } catch (IOException | GitAPIException e) {
            throw new GitOperationException("addFiles", "Failed to add files: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean commit(File repoDir, String message) throws GitOperationException {
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
            throw new GitOperationException("commit", "Failed to commit: " + e.getMessage(), e);
        }
    }

    @Override
    public void push(File repoDir, boolean pushTags) throws GitOperationException {
        if (authContext == null) {
            throw new GitOperationException("push", "GitAuthContext not set. Use constructor with GitAuthContext parameter.");
        }

        try (Git git = Git.open(repoDir)) {
            CredentialsProvider cp = setupCredentials(authContext);

            // Commit Push
            git.push()
                    .setRemote("origin")
                    .setCredentialsProvider(cp)
                    .setPushAll()
                    .call();

            // If requested push tags also
            if (pushTags) {
                git.push()
                        .setPushTags()
                        .setCredentialsProvider(cp)
                        .call();
            }
        } catch (IOException | GitAPIException e) {
            throw new GitOperationException("push", "Failed to push: " + e.getMessage(), e);
        }
    }

    @Override
    public String getLatestCommitSha(File repoDir, String branchName) throws GitOperationException {
        try (Git git = Git.open(repoDir)) {
            // Resolve HEAD of the specified branch
            String branchRef = "refs/heads/" + (StringUtils.hasText(branchName) ? branchName : "main");
            ObjectId commitId = git.getRepository().resolve(branchRef);

            // If branch not found, try master as fallback
            if (commitId == null) {
                commitId = git.getRepository().resolve("refs/heads/master");
            }

            if (commitId == null) {
                throw new GitOperationException("getLatestCommitSha", "Cannot resolve latest commit for branch: " + branchName);
            }

            return commitId.getName(); // Return full SHA
        } catch (IOException e) {
            throw new GitOperationException("getLatestCommitSha", "Failed to get latest commit SHA: " + e.getMessage(), e);
        }
    }


    @Override
    public void addTag(File repoDir, String tagName, String targetSha, String message) throws GitOperationException {
        if (repoDir == null || !StringUtils.hasText(tagName)  || !StringUtils.hasText(targetSha)) {
            throw new GitOperationException("addTag", "Repository directory, tag name, and target SHA are required");
        }

        try (Git git = Git.open(repoDir)) {
            // Resolve targetCommit
            ObjectId commitId = git.getRepository().resolve(targetSha);
            if (commitId == null) {
                throw new GitOperationException("addTag", "Commit not found: " + targetSha);
            }

            // Translate commitId in rev RevObject id
            try (var revWalk = new org.eclipse.jgit.revwalk.RevWalk(git.getRepository())) {
                var revCommit = revWalk.parseCommit(commitId);
                if (StringUtils.hasText(message)) {
                    git.tag()
                            .setObjectId(revCommit)
                            .setName(tagName)
                            .setMessage(message)
                            .call();
                } else {
                    git.tag()
                            .setObjectId(revCommit)
                            .setName(tagName)
                            .call();
                }

                CredentialsProvider cp = setupCredentials(authContext);
                git.push()
                        .setPushTags()
                        .setCredentialsProvider(cp)
                        .call();
            }
        } catch (IOException | GitAPIException e) {
            throw new GitOperationException("addTag", "Failed to create tag: " + e.getMessage(), e);
        }
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
    
    /**
     * Gets the relative path of a file from the repository root directory.
     * 
     * @param repoDir the repository root directory
     * @param file the file to get the relative path for
     * @return the relative path string
     * @throws GitOperationException if the file is not within the repository directory
     */
    private String getRelativePath(File repoDir, File file) throws GitOperationException {
        try {
            Path repoPath = repoDir.toPath().toAbsolutePath().normalize();
            Path filePath = file.toPath().toAbsolutePath().normalize();
            
            if (!filePath.startsWith(repoPath)) {
                throw new GitOperationException("addFiles", "File is not within repository directory: " + file.getPath());
            }
            
            Path relativePath = repoPath.relativize(filePath);
            return relativePath.toString().replace('\\', '/'); // Normalize path separators for Git
        } catch (Exception e) {
            throw new GitOperationException("addFiles", "Failed to get relative path: " + e.getMessage(), e);
        }
    }

}
