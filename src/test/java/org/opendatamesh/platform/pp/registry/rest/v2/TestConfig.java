package org.opendatamesh.platform.pp.registry.rest.v2;

import org.mockito.Mockito;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.concurrent.Executor;

/**
 * Test configuration for integration tests.
 * This class is included in the SpringBootTest configuration to provide
 * test-specific beans and configurations.
 */
@Profile("test")
@TestConfiguration
public class TestConfig {
    //Makes all @Async methods synchronous
    @Bean
    @Primary
    public Executor taskExecutor() {
        return new SyncTaskExecutor();
    }

    /**
     * Scans for all interfaces under the client packages and
     * registers a Mockito.mock(clazz) as a singleton bean for each.
     */
    @Bean
    public static BeanFactoryPostProcessor mockClientsRegistrar() {
        return beanFactory -> {
            DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;

            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resolver);

            String pattern = "classpath*:org/opendatamesh/platform/pp/registry/client/**/*.class";

            try {
                Resource[] resources = resolver.getResources(pattern);
                for (Resource resource : resources) {
                    MetadataReader reader = readerFactory.getMetadataReader(resource);
                    String className = reader.getClassMetadata().getClassName();
                    Class<?> clazz = ClassUtils.forName(className, TestConfig.class.getClassLoader());

                    if (clazz.isInterface()) {
                        String beanName = StringUtils.uncapitalize(clazz.getSimpleName());
                        // Remove all beans of this type
                        String[] beanNamesForType = factory.getBeanNamesForType(clazz);
                        for (String existingBeanName : beanNamesForType) {
                            factory.removeBeanDefinition(existingBeanName);
                        }
                        Object mock = Mockito.mock(clazz);
                        factory.registerSingleton(beanName, mock);
                    }
                }
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to register mock clients", ex);
            }
        };
    }
}
