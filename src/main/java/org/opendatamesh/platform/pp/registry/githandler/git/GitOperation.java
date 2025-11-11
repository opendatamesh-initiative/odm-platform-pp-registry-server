package org.opendatamesh.platform.pp.registry.githandler.git;


import org.opendatamesh.platform.pp.registry.githandler.exceptions.GitOperationException;
import org.opendatamesh.platform.pp.registry.githandler.model.RepositoryPointer;

import java.io.File;
import java.net.URL;
import java.util.List;

public interface GitOperation {

    /**
     * Initializes a new Git repository locally and sets up the remote origin.
     * This method should be used when the remote repository is empty and needs to be initialized.
     * The method only initializes the repository and sets up the remote - it does not push anything.
     *
     * @param repoName the repository name (will be created in a tmp directory)
     * @param initialBranch the initial branch name
     * @param remoteUrl the remote repository URL
     * @return the initialized Git repository directory
     * @throws GitOperationException if the repository initialization fails
     */
    File initRepository(String repoName, String initialBranch, URL remoteUrl) throws GitOperationException;

    /**
     * Clones a repository and checks out the specified pointer (branch, commit, or tag).
     *
     * @param pointer the repository pointer specifying which version to checkout
     * @return the local repository directory
     * @throws GitOperationException if the repository cloning or checkout fails
     */
    File getRepositoryContent(RepositoryPointer pointer) throws GitOperationException;

    /**
     * Adds one or more files to the Git index (staging area).
     *
     * @param repoDir the local repository directory
     * @param files list of files to add (must be actual files, not directories)
     * @throws GitOperationException if adding files to the index fails
     */
    void addFiles(File repoDir, List<File> files) throws GitOperationException;

    /**
     * Commits staged changes if there are any.
     *
     * @param repoDir the local repository directory
     * @param message commit message
     * @return true if a commit was created, false if no changes to commit
     * @throws GitOperationException if the commit operation fails
     */
    boolean commit(File repoDir, String message) throws GitOperationException;

    /**
     * Pushes commits to the remote.
     *
     * @param repoDir the local repository directory
     * @param pushTags to push tags
     * @throws GitOperationException if the push operation fails
     */
    void push(File repoDir, boolean pushTags) throws GitOperationException;


    /**
     * Creates a new Git tag in the given repository.
     *
     * @param repoDir   the local repository directory
     * @param tagName   the name of the tag (e.g. "v1.0.0")
     * @param targetSha optional commit SHA to tag; if null, tags HEAD
     * @param message   optional message for annotated tag; if null, creates a lightweight tag
     * @throws GitOperationException if the tag creation or push fails
     */
    void addTag(File repoDir, String tagName, String targetSha, String message) throws GitOperationException;


    /**
     * Retrieve the SHA of the latest commit (HEAD) on a specific branch
     *
     * @param repoDir   optional message for annotated tag; if null, creates a lightweight tag
     * @param branchName the name of the branch to look for retrieve last sha
     * @throws GitOperationException if the push operation fails
     */
    String getLatestCommitSha(File repoDir, String branchName) throws GitOperationException;
}
