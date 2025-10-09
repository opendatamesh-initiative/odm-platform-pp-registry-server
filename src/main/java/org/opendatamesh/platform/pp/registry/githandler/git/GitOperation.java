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
     * @param remoteUrl the remote repository URL
     * @return the initialized Git repository directory
     * @throws GitOperationException if the repository initialization fails
     */
    File initRepository(String repoName, URL remoteUrl) throws GitOperationException;

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
     * @throws GitOperationException if the push operation fails
     */
    void push(File repoDir) throws GitOperationException;


}
