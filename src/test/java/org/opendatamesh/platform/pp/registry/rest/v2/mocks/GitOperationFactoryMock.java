package org.opendatamesh.platform.pp.registry.rest.v2.mocks;

import org.mockito.Mockito;
import org.opendatamesh.platform.pp.registry.githandler.git.GitOperation;
import org.opendatamesh.platform.pp.registry.githandler.git.GitOperationFactory;
import org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext;
import org.opendatamesh.platform.pp.registry.githandler.model.RepositoryPointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Profile("test")
@Primary
public class GitOperationFactoryMock extends IntegrationMock implements GitOperationFactory {

    private final Object lock = new Object();
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final AtomicLong testCounter = new AtomicLong(0);
    
    private GitOperation mockGitOperation;
    private File currentTempDir;
    private boolean initialized = false;
    
    public GitOperationFactoryMock() {
        // Don't call reset() here - it will be called when needed
    }

    @Override
    public void reset() {
        synchronized (lock) {
            // Clean up previous temp directory
            cleanupTempDirectory();
            
            mockGitOperation = Mockito.mock(GitOperation.class);
            
            // Set up default mock behavior
            try {
                // Create a base temp directory for this test session
                currentTempDir = createUniqueTempDirectory();
                logger.debug("Created base temp directory for test session: {}", currentTempDir.getAbsolutePath());
                
                // Mock getRepositoryContent to return a subdirectory of the base directory
                Mockito.when(mockGitOperation.getRepositoryContent(Mockito.any(RepositoryPointer.class)))
                        .thenAnswer(invocation -> {
                            File repoDir = new File(currentTempDir, "repo-" + System.nanoTime());
                            repoDir.mkdirs();
                            logger.debug("Created repo directory: {}", repoDir.getAbsolutePath());
                            return repoDir;
                        });
                
                // Mock initRepository to return a subdirectory of the base directory
                Mockito.when(mockGitOperation.initRepository(Mockito.anyString(), Mockito.any(URL.class)))
                        .thenAnswer(invocation -> {
                            File repoDir = new File(currentTempDir, "repo-" + System.nanoTime());
                            repoDir.mkdirs();
                            logger.debug("Created repo directory for init: {}", repoDir.getAbsolutePath());
                            return repoDir;
                        });
                
                // Mock addFiles to do nothing
                Mockito.doNothing().when(mockGitOperation).addFiles(Mockito.any(File.class), Mockito.anyList());
                
                // Mock commit to return true (success)
                Mockito.when(mockGitOperation.commit(Mockito.any(File.class), Mockito.anyString()))
                        .thenReturn(true);
                
                // Mock push to do nothing
                Mockito.doNothing().when(mockGitOperation).push(Mockito.any(File.class));
                
                initialized = true;
                
            } catch (Exception e) {
                logger.warn("Failed to set up default mock behavior", e);
            }
        }
    }
    
    private File createUniqueTempDirectory() throws IOException {
        String testId = "test-repo-" + testCounter.incrementAndGet() + "-" + System.nanoTime();
        Path tempPath = Files.createTempDirectory(testId);
        return tempPath.toFile();
    }
    
    private void cleanupTempDirectory() {
        if (currentTempDir != null && currentTempDir.exists()) {
            try {
                Files.walk(currentTempDir.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(file -> {
                        try {
                            if (file.exists()) {
                                file.delete();
                            }
                        } catch (Exception e) {
                            logger.debug("Failed to delete file during cleanup: {}", file.getAbsolutePath(), e);
                        }
                    });
            } catch (Exception e) {
                logger.debug("Failed to cleanup temp directory: {}", currentTempDir.getAbsolutePath(), e);
            }
        }
        currentTempDir = null;
    }

    public GitOperation createGitOperation() {
        synchronized (lock) {
            if (!initialized) {
                reset();
            }
            if (mockGitOperation != null) {
                return mockGitOperation;
            }
            logger.warn("Calling factory without properly setting the mock");
            return Mockito.mock(GitOperation.class);
        }
    }

    public GitOperation createGitOperation(GitAuthContext authContext) {
        synchronized (lock) {
            if (!initialized) {
                reset();
            }
            if (mockGitOperation != null) {
                return mockGitOperation;
            }
            logger.warn("Calling factory without properly setting the mock");
            return Mockito.mock(GitOperation.class);
        }
    }

    public void setMockGitOperation(GitOperation mockGitOperation) {
        synchronized (lock) {
            this.mockGitOperation = mockGitOperation;
            initialized = true;
        }
    }

    public GitOperation getMockGitOperation() {
        return mockGitOperation;
    }

    public void setMockRepositoryContent(File repoDir) {
        synchronized (lock) {
            try {
                if (!initialized) {
                    reset();
                }
                
                // Store the specific directory for this test
                currentTempDir = repoDir;
                
                // Override the default behavior to return the specific directory
                Mockito.when(mockGitOperation.getRepositoryContent(Mockito.any(RepositoryPointer.class)))
                        .thenReturn(repoDir);
                
                // Also update initRepository to return the same directory
                Mockito.when(mockGitOperation.initRepository(Mockito.anyString(), Mockito.any(URL.class)))
                        .thenReturn(repoDir);
                        
                logger.debug("Set mock repository content to: {}", repoDir.getAbsolutePath());
            } catch (Exception e) {
                logger.warn("Failed to set mock repository content", e);
            }
        }
    }
    
    /**
     * Clean up the current temporary directory.
     * This should be called when the test is done to ensure proper cleanup.
     */
    public void cleanup() {
        synchronized (lock) {
            cleanupTempDirectory();
        }
    }
}
