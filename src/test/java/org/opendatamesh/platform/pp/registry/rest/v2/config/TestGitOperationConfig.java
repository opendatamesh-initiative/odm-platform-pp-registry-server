package org.opendatamesh.platform.pp.registry.rest.v2.config;

import org.mockito.Mockito;
import org.opendatamesh.platform.pp.registry.githandler.git.GitOperation;
import org.opendatamesh.platform.pp.registry.githandler.git.GitOperationFactory;
import org.opendatamesh.platform.pp.registry.githandler.git.GitAuthContext;
import org.opendatamesh.platform.pp.registry.githandler.model.RepositoryPointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class TestGitOperationConfig {

    @Bean
    public GitOperationFactory testGitOperationFactory() {
        return new TestGitOperationFactory();
    }

    public static class TestGitOperationFactory implements GitOperationFactory {
        private static final Logger logger = LoggerFactory.getLogger(TestGitOperationFactory.class);
        private static final AtomicLong testCounter = new AtomicLong(0);
        private final Object lock = new Object();
        
        private GitOperation mockGitOperation;
        private File currentRepoDir;

        public TestGitOperationFactory() {
            resetMock();
        }

        public void resetMock() {
            synchronized (lock) {
                // Clean up previous directory
                cleanupCurrentDirectory();
                
                mockGitOperation = Mockito.mock(GitOperation.class);
                
                try {
                    // Create a base temp directory for this test session
                    currentRepoDir = createUniqueTempDirectory();
                    logger.debug("Created base temp directory for test session: {}", currentRepoDir.getAbsolutePath());
                    
                    // Mock getRepositoryContent to return a subdirectory of the base directory
                    Mockito.when(mockGitOperation.getRepositoryContent(Mockito.any(RepositoryPointer.class)))
                            .thenAnswer(invocation -> {
                                File repoDir = new File(currentRepoDir, "repo-" + System.nanoTime());
                                repoDir.mkdirs();
                                logger.debug("Created repo directory: {}", repoDir.getAbsolutePath());
                                return repoDir;
                            });
                    
                    // Mock initRepository to return a subdirectory of the base directory
                    Mockito.when(mockGitOperation.initRepository(Mockito.anyString(), Mockito.any(URL.class)))
                            .thenAnswer(invocation -> {
                                File repoDir = new File(currentRepoDir, "repo-" + System.nanoTime());
                                repoDir.mkdirs();
                                logger.debug("Created repo directory for init: {}", repoDir.getAbsolutePath());
                                return repoDir;
                            });
                    
                    Mockito.doNothing().when(mockGitOperation).addFiles(Mockito.any(File.class), Mockito.anyList());
                    
                    Mockito.when(mockGitOperation.commit(Mockito.any(File.class), Mockito.anyString()))
                            .thenReturn(true);
                    
                    Mockito.doNothing().when(mockGitOperation).push(Mockito.any(File.class));
                    
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
        
        private void cleanupCurrentDirectory() {
            if (currentRepoDir != null && currentRepoDir.exists()) {
                try {
                    Files.walk(currentRepoDir.toPath())
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
                    logger.debug("Failed to cleanup directory: {}", currentRepoDir.getAbsolutePath(), e);
                }
            }
            currentRepoDir = null;
        }

        public void setMockRepositoryContent(File repoDir) {
            synchronized (lock) {
                try {
                    // Store the specific directory for this test
                    currentRepoDir = repoDir;
                    
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
         * Clean up the current directory.
         * This should be called when the test is done to ensure proper cleanup.
         */
        public void cleanup() {
            synchronized (lock) {
                cleanupCurrentDirectory();
            }
        }

        @Override
        public GitOperation createGitOperation() {
            return mockGitOperation;
        }

        @Override
        public GitOperation createGitOperation(GitAuthContext authContext) {
            return mockGitOperation;
        }

        public GitOperation getMockGitOperation() {
            return mockGitOperation;
        }

        public void setMockGitOperation(GitOperation mockGitOperation) {
            this.mockGitOperation = mockGitOperation;
        }
    }
}
