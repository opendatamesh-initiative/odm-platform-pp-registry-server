package org.opendatamesh.platform.pp.registry.utils.git.git;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.opendatamesh.platform.pp.registry.utils.git.exceptions.GitOperationException;
import org.opendatamesh.platform.pp.registry.utils.git.model.Commit;
import org.opendatamesh.platform.pp.registry.utils.git.model.Repository;
import org.opendatamesh.platform.pp.registry.utils.git.model.RepositoryPointer;
import org.opendatamesh.platform.pp.registry.utils.git.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class GitOperationImpl implements GitOperation {

    private final Logger logger = LoggerFactory.getLogger(GitOperationImpl.class);
    private final GitCredential authContext;

    public GitOperationImpl() {
        this.authContext = null;
    }

    public GitOperationImpl(GitCredential authContext) {
        this.authContext = authContext;
    }

    @Override
    public void initRepository(Repository repository, Consumer<File> repositoryReader) {
        validateInitRepositoryArgs(repository, repositoryReader);

        File localRepo = null;
        try {
            // Use OS-level secure temporary directories to avoid collisions
            Path tempDir = Files.createTempDirectory("git-init-" + repository.getName() + "-");
            localRepo = tempDir.toFile();

            // Use try-with-resources to automatically close the Git instance
            try (Git git = Git.init().setDirectory(localRepo)
                    .setInitialBranch(repository.getDefaultBranch())
                    .call()) {
                git.remoteAdd()
                        .setName("origin")
                        .setUri(new URIish(repository.getRemoteUrl()))
                        .call();
            }

            repositoryReader.accept(localRepo);

        } catch (GitAPIException | IOException | URISyntaxException e) {
            throw new GitOperationException("initRepository", "Failed to initialize repository: " + e.getMessage(), e);
        } finally {
            if (localRepo != null && localRepo.exists()) {
                deleteRecursively(localRepo);
            }
        }
    }

    @Override
    public void readRepository(Repository repository, RepositoryPointer pointer, Consumer<File> consumer) {
        validateReadRepositoryArgs(repository, pointer, consumer);

        File localRepo = null;
        try {
            // Create temporary directory for cloning
            Path tempDir = Files.createTempDirectory("git-repo-" + repository.getName() + "-");
            localRepo = tempDir.toFile();

            String cloneUrl = getCloneUrl(repository, authContext.getTransportProtocol());
            CredentialsProvider credentialsProvider = buildCredentialsProvider(authContext);

            CloneCommand cloneCommand = Git.cloneRepository()
                    .setURI(cloneUrl)
                    .setDirectory(localRepo)
                    .setCredentialsProvider(credentialsProvider)
                    .setDepth(1); // Shallow clone

            if (pointer.getRefType() == RepositoryPointer.RefType.BRANCH) {
                cloneCommand.setBranch(pointer.getRefValue());
            }

            try (Git git = cloneCommand.call()) {
                git.checkout()
                        .setName(pointer.getRefValue())
                        .call();
            }

            consumer.accept(localRepo);

        } catch (GitAPIException | IOException e) {
            throw new GitOperationException("getRepositoryContent", "Failed to clone repository: " + e.getMessage(), e);
        } finally {
            if (localRepo != null && localRepo.exists()) {
                deleteRecursively(localRepo);
            }
        }
    }

    @Override
    public void addFiles(File repoDir, List<File> files) {
        if (repoDir == null || !repoDir.exists()) {
            throw new GitOperationException("addFiles", "Repository directory must exist and cannot be null");
        }
        if (files == null || files.isEmpty()) {
            return;
        }

        try (Git git = Git.open(repoDir)) {
            AddCommand add = git.add();
            boolean hasValidFiles = false;

            for (File file : files) {
                if (file != null && file.isFile()) {
                    String relativePath = getRelativePath(repoDir, file);
                    add.addFilepattern(relativePath);
                    hasValidFiles = true;
                }
            }

            if (hasValidFiles) {
                add.call();
            }
        } catch (IOException | GitAPIException e) {
            throw new GitOperationException("addFiles", "Failed to add files: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean commit(File repoDir, Commit commit) {
        if (repoDir == null || !repoDir.exists() || commit == null) {
            throw new GitOperationException("commit", "Valid repository directory and commit context are required");
        }

        try (Git git = Git.open(repoDir)) {
            Status status = git.status().call();
            if (status.isClean()) {
                throw new GitOperationException("commit", "No changes to commit. Working tree is clean.");
            }
            var commitCmd = git.commit().setMessage(commit.getMessage());
            if (StringUtils.hasText(commit.getAuthor()) && StringUtils.hasText(commit.getAuthorEmail())) {
                PersonIdent ident = new PersonIdent(commit.getAuthor(), commit.getAuthorEmail());
                commitCmd.setAuthor(ident);
                commitCmd.setCommitter(ident);
            }
            commitCmd.call();
            return true;
        } catch (IOException | GitAPIException e) {
            throw new GitOperationException("commit", "Failed to commit: " + e.getMessage(), e);
        }
    }

    @Override
    public void push(File repoDir, boolean pushTags) {
        if (repoDir == null || !repoDir.exists()) {
            throw new GitOperationException("push", "Valid repository directory is required");
        }
        if (authContext == null) {
            throw new GitOperationException("push", "GitAuthContext not set. Use constructor with GitAuthContext parameter.");
        }

        try (Git git = Git.open(repoDir)) {
            CredentialsProvider cp = buildCredentialsProvider(authContext);
            var pushCommand = git.push()
                    .setRemote("origin")
                    .setCredentialsProvider(cp)
                    .setPushAll();
            if (pushTags) {
                pushCommand.setPushTags();
            }
            pushCommand.call();
        } catch (IOException | GitAPIException e) {
            throw new GitOperationException("push", "Failed to push: " + e.getMessage(), e);
        }
    }

    @Override
    public String getHeadSha(File repoDir, String branchName) {
        if (repoDir == null || !repoDir.exists()) {
            throw new GitOperationException("getLatestCommitSha", "Valid repository directory is required");
        }
        if (!StringUtils.hasText(branchName)) {
            throw new GitOperationException("getLatestCommitSha", "Branch name is required to retrieve the latest commit SHA");
        }

        try (Git git = Git.open(repoDir)) {
            String branchRef = toFullBranchRef(branchName);
            ObjectId commitId = git.getRepository().resolve(branchRef);
            if (commitId == null) {
                throw new GitOperationException("getLatestCommitSha", "Cannot resolve latest commit for branch: " + branchName);
            }
            return commitId.getName();
        } catch (IOException e) {
            throw new GitOperationException("getLatestCommitSha", "Failed to get latest commit SHA: " + e.getMessage(), e);
        }
    }

    @Override
    public void addTag(File repoDir, Tag tag) {
        if (repoDir == null || !repoDir.exists()) {
            throw new GitOperationException("addTag", "Valid repository directory is required");
        }
        if (tag == null || !StringUtils.hasText(tag.getName()) || !StringUtils.hasText(tag.getCommitHash())) {
            throw new GitOperationException("addTag", "Tag name and target SHA are required");
        }

        try (Git git = Git.open(repoDir)) {
            ObjectId commitId = git.getRepository().resolve(tag.getCommitHash());
            if (commitId == null) {
                throw new GitOperationException("addTag", "Commit not found: " + tag.getCommitHash());
            }

            try (var revWalk = new org.eclipse.jgit.revwalk.RevWalk(git.getRepository())) {
                var revCommit = revWalk.parseCommit(commitId);
                var tagCmd = git.tag().setObjectId(revCommit).setName(tag.getName());

                if (StringUtils.hasText(tag.getMessage())) {
                    tagCmd.setMessage(tag.getMessage());
                }
                if (StringUtils.hasText(tag.getAuthor()) && StringUtils.hasText(tag.getAuthorEmail())) {
                    PersonIdent taggerIdent = new PersonIdent(tag.getAuthor(), tag.getAuthorEmail());
                    tagCmd.setTagger(taggerIdent);
                }
                tagCmd.call();
            }
        } catch (IOException | GitAPIException e) {
            throw new GitOperationException("addTag", "Failed to create tag: " + e.getMessage(), e);
        }
    }

    // --- Private Helper & Validation Methods ---

    private void validateInitRepositoryArgs(Repository repository, Consumer<File> repositoryReader) {
        if (repository == null || !StringUtils.hasText(repository.getName())
                || !StringUtils.hasText(repository.getRemoteUrl())) {
            throw new GitOperationException("initRepository", "Repository name and remote URL cannot be null or empty");
        }
        if (!StringUtils.hasText(repository.getDefaultBranch())) {
            throw new GitOperationException("initRepository", "Repository default branch cannot be null or empty");
        }
        if (repositoryReader == null) {
            throw new GitOperationException("initRepository", "Repository reader consumer cannot be null");
        }
        if (authContext == null) {
            throw new GitOperationException("initRepository",
                    "GitAuthContext not set. Use constructor with GitAuthContext parameter.");
        }
    }

    private void validateReadRepositoryArgs(Repository repository, RepositoryPointer pointer, Consumer<File> consumer) {
        if (repository == null) {
            throw new GitOperationException("getRepositoryContent", "Repository cannot be null");
        }
        if (pointer == null) {
            throw new GitOperationException("getRepositoryContent", "RepositoryPointer cannot be null");
        }
        if (consumer == null) {
            throw new GitOperationException("getRepositoryContent", "Consumer cannot be null");
        }
        if (authContext == null) {
            throw new GitOperationException("getRepositoryContent",
                    "GitAuthContext not set. Use constructor with GitAuthContext parameter.");
        }
    }

    private String getCloneUrl(Repository repository, GitCredential.TransportProtocol protocol) {
        if (protocol == GitCredential.TransportProtocol.SSH) {
            throw new UnsupportedOperationException("SSH cloning is not supported");
        } else {
            return repository.getCloneUrlHttp();
        }
    }

    private CredentialsProvider buildCredentialsProvider(GitCredential ctx) {
        if (ctx.getTransportProtocol() == GitCredential.TransportProtocol.SSH) {
            return null; // Handled via key-based auth implicitly
        } else {
            return buildCredentialsProvider((GitCredentialHttps) ctx);
        }
    }

    private CredentialsProvider buildCredentialsProvider(GitCredentialHttps ctx) {
        if (ctx.getHttpAuthHeaders() != null) {
            String username = ctx.getHttpAuthHeaders().getFirst("username");
            String password = ctx.getHttpAuthHeaders().getFirst("password");
            if (username != null && password != null) {
                return new UsernamePasswordCredentialsProvider(username, password);
            } else {
                String token = ctx.getHttpAuthHeaders().getFirst("Authorization");
                return new UsernamePasswordCredentialsProvider("dummy", token);
            }
        }
        return null;
    }

    private String getRelativePath(File repoDir, File file) {
        try {
            Path repoPath = repoDir.toPath().toAbsolutePath().normalize();
            Path filePath = file.toPath().toAbsolutePath().normalize();

            if (!filePath.startsWith(repoPath)) {
                throw new GitOperationException("addFiles", "File is not within repository directory: " + file.getPath());
            }

            Path relativePath = repoPath.relativize(filePath);
            return relativePath.toString().replace('\\', '/');
        } catch (Exception e) {
            throw new GitOperationException("addFiles", "Failed to get relative path: " + e.getMessage(), e);
        }
    }

    private String toFullBranchRef(String branchName) {
        if (branchName.startsWith(Constants.R_HEADS) || branchName.startsWith(Constants.R_REMOTES)) {
            return branchName;
        }
        return Constants.R_HEADS + branchName;
    }

    private void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        try (Stream<Path> pathStream = Files.walk(file.toPath())) {
            pathStream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(f -> {
                        if (!f.delete()) {
                            logger.warn("Failed to delete temp file/folder: {}", f.getAbsolutePath());
                        }
                    });
        } catch (IOException e) {
            logger.error("Error walking directory for deletion: {}", file.getAbsolutePath(), e);
        }
    }
}