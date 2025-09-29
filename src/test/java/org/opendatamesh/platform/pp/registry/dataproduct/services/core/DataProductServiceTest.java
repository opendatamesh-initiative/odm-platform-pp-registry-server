package org.opendatamesh.platform.pp.registry.dataproduct.services.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendatamesh.platform.pp.registry.dataproduct.repositories.DataProductRepository;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductMapper;
import org.opendatamesh.platform.pp.registry.utils.services.TransactionHandler;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DataProductServiceTest {

    @Mock
    private DataProductRepository repository;

    @Mock
    private DataProductMapper mapper;

    @Mock
    private TransactionHandler transactionHandler;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private DataProductServiceImpl dataProductService;

    @Test
    void whenServiceIsCreatedThenItShouldNotBeNull() {
        // This is a simple test to verify the service can be instantiated
        // Remove when adding more tests
        assertThat(dataProductService).isNotNull();
    }
}
