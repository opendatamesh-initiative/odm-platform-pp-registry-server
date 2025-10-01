package org.opendatamesh.platform.pp.registry.githandler.git;


import org.opendatamesh.platform.pp.registry.githandler.model.RepositoryPointer;

import java.io.File;
import java.util.List;

public interface GitOperation {

    File getRepositoryContent(RepositoryPointer pointer, GitAuthContext ctx);

    /**
     * Adds one or more files to the Git index (staging area).
     *
     * @param repoDir the local repository directory
     * @param filePatterns file paths or patterns relative to repo root
     */
    void addFiles(File repoDir, List<String> filePatterns);

    /**
     * Commits staged changes if there are any.
     *
     * @param repoDir the local repository directory
     * @param message commit message
     * @return true if a commit was created, false if no changes to commit
     */
    boolean commit(File repoDir, String message);

    /**
     * Pushes commits to the remote.
     *
     * @param repoDir the local repository directory
     * @param ctx authentication context
     */
    void push(File repoDir, GitAuthContext ctx);

    /**
     * Convenience method: add → commit → push in one go.
     */
    boolean addCommitPush(File repoDir,
                          List<String> filePatterns,
                          String message,
                          GitAuthContext ctx);
}
