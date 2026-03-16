package org.opendatamesh.platform.pp.registry.utils.git.git;


import org.opendatamesh.platform.pp.registry.utils.git.model.Commit;
import org.opendatamesh.platform.pp.registry.utils.git.model.Repository;
import org.opendatamesh.platform.pp.registry.utils.git.model.RepositoryPointer;
import org.opendatamesh.platform.pp.registry.utils.git.model.Tag;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * Low-level Git operations: init, clone, add, commit, push, tag, and resolve
 * HEAD SHA.
 * All methods use a local working directory; clone/read operations accept a
 * consumer
 * that receives the repo root. Implementations may throw
 * {@link org.opendatamesh.platform.pp.registry.utils.git.exceptions.GitOperationException}
 * on failure.
 */
public interface GitOperation {

    /**
     * Initializes a new Git repository (bare init, remote "origin", default
     * branch),
     * invokes the consumer with the repo directory, then cleans up the directory.
     *
     * @param repository       repository metadata (name, clone URL, default branch)
     * @param repositoryReader consumer invoked with the local repo root directory
     * @throws org.opendatamesh.platform.pp.registry.utils.git.exceptions.GitOperationException if
     *                                                                                          init
     *                                                                                          or
     *                                                                                          remote
     *                                                                                          add
     *                                                                                          fails
     */
    void initRepository(Repository repository, Consumer<File> repositoryReader);

    /**
     * Clones the repository and checks out the given pointer (branch, tag, or
     * commit),
     * invokes the consumer with the repo directory, then cleans up the directory.
     *
     * @param repository       repository metadata (clone URL, etc.)
     * @param pointer          ref to checkout (branch name, tag name, or commit
     *                         hash)
     * @param repositoryReader consumer invoked with the local repo root directory
     * @throws org.opendatamesh.platform.pp.registry.utils.git.exceptions.GitOperationException if
     *                                                                                          clone
     *                                                                                          or
     *                                                                                          checkout
     *                                                                                          fails
     */
    void readRepository(Repository repository, RepositoryPointer pointer, Consumer<File> repositoryReader);

    /**
     * Adds one or more files to the Git index (staging area).
     *
     * @param repoDir the local repository root directory
     * @param files   files to add (paths relative to repo; must be files, not
     *                directories)
     * @throws org.opendatamesh.platform.pp.registry.utils.git.exceptions.GitOperationException if
     *                                                                                          add
     *                                                                                          fails
     *                                                                                          or
     *                                                                                          a
     *                                                                                          file
     *                                                                                          is
     *                                                                                          outside
     *                                                                                          the
     *                                                                                          repo
     */
    void addFiles(File repoDir, List<File> files);

    /**
     * Commits the current index to the repository.
     *
     * @param repoDir the local repository root directory
     * @param commit  commit message and author info
     * @return true if a commit was created, false if there was nothing to commit
     * (clean working tree)
     * @throws org.opendatamesh.platform.pp.registry.utils.git.exceptions.GitOperationException if
     *                                                                                          commit
     *                                                                                          fails
     */
    boolean commit(File repoDir, Commit commit);

    /**
     * Pushes the current branch (and optionally tags) to the remote.
     *
     * @param repoDir  the local repository root directory
     * @param pushTags whether to push tags as well
     * @throws org.opendatamesh.platform.pp.registry.utils.git.exceptions.GitOperationException if
     *                                                                                          push
     *                                                                                          fails
     *                                                                                          or
     *                                                                                          credentials
     *                                                                                          are
     *                                                                                          missing
     */
    void push(File repoDir, boolean pushTags);

    /**
     * Creates a tag at the given commit (lightweight or annotated depending on tag
     * message).
     *
     * @param repoDir the local repository root directory
     * @param tag     tag name, target commit hash, and optional message
     * @throws GitOperationException if the tag cannot be created
     */
    void addTag(File repoDir, Tag tag);

    /**
     * Returns the SHA of the latest commit (HEAD) for the given branch.
     *
     * @param repoDir    the local repository root directory
     * @param branchName the branch name (e.g. "main", "master")
     * @return the full SHA of the branch HEAD
     * @throws org.opendatamesh.platform.pp.registry.utils.git.exceptions.GitOperationException if
     *                                                                                          the
     *                                                                                          repo
     *                                                                                          is
     *                                                                                          invalid
     *                                                                                          or
     *                                                                                          the
     *                                                                                          branch
     *                                                                                          cannot
     *                                                                                          be
     *                                                                                          resolved
     */
    String getHeadSha(File repoDir, String branchName);
}
