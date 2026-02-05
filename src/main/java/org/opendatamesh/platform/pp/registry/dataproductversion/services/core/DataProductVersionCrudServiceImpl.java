package org.opendatamesh.platform.pp.registry.dataproductversion.services.core;

import org.opendatamesh.platform.pp.registry.dataproduct.services.core.DataProductsService;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionValidationState;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DescriptorSpec;
import org.opendatamesh.platform.pp.registry.dataproductversion.repositories.DataProductVersionsRepository;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.exceptions.NotImplemented;
import org.opendatamesh.platform.pp.registry.exceptions.ResourceConflictException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionSearchOptions;
import org.opendatamesh.platform.pp.registry.utils.repositories.PagingAndSortingAndSpecificationExecutorRepository;
import org.opendatamesh.platform.pp.registry.utils.services.GenericMappedAndFilteredCrudServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Implementation of {@link DataProductVersionCrudService} for CRUD operations on individual DataProductVersion entities.
 * <p>
 * This service implementation provides full CRUD functionality including all descriptor content.
 * Paginated reads are explicitly disabled to encourage the use of the query service for listing operations.
 */
@Service
public class DataProductVersionCrudServiceImpl extends GenericMappedAndFilteredCrudServiceImpl<DataProductVersionSearchOptions, DataProductVersionRes, DataProductVersion, String> implements DataProductVersionCrudService {

    private final DataProductVersionMapper mapper;
    private final DataProductVersionsRepository repository;
    private final DataProductsService dataProductsService;

    @Autowired
    public DataProductVersionCrudServiceImpl(
            DataProductVersionMapper mapper,
            DataProductVersionsRepository repository,
            DataProductsService dataProductsService) {
        this.mapper = mapper;
        this.repository = repository;
        this.dataProductsService = dataProductsService;
    }

    @Override
    protected DataProductVersion toEntity(DataProductVersionRes resource) {
        return mapper.toEntity(resource);
    }

    @Override
    protected void validate(DataProductVersion dataProductVersion) {
        validateRequiredFields(dataProductVersion);
        validateFieldConstraints(dataProductVersion);
    }

    private void validateRequiredFields(DataProductVersion dataProductVersion) {
        if (!StringUtils.hasText(dataProductVersion.getDataProductUuid())) {
            throw new BadRequestException("Missing DataProduct on DataProductVersion");
        }
        if (!StringUtils.hasText(dataProductVersion.getName())) {
            throw new BadRequestException("Missing Data Product Version name");
        }
        if (!StringUtils.hasText(dataProductVersion.getVersionNumber())) {
            throw new BadRequestException("Missing Data Product Version version number");
        }
        if (dataProductVersion.getContent() == null) {
            throw new BadRequestException("Missing Data Product Version content");
        }
    }

    private void validateFieldConstraints(DataProductVersion dataProductVersion) {
        // Validate field lengths based on database schema
        validateLength("Name", dataProductVersion.getName(), 255);
        validateLength("Description", dataProductVersion.getDescription(), 10000); // text field, reasonable limit
        validateLength("Tag", dataProductVersion.getTag(), 255);
        validateLength("Descriptor spec", dataProductVersion.getSpec(), 255);
        validateLength("Descriptor spec version", dataProductVersion.getSpecVersion(), 255);
        validateLength("Version number", dataProductVersion.getVersionNumber(), 255);

        // Validate enum values
        validateValidationState(dataProductVersion.getValidationState());
    }

    @Override
    protected void reconcile(DataProductVersion dataProductVersion) {
        dataProductVersion.setDataProduct(
                dataProductsService.findOne(dataProductVersion.getDataProductUuid())
        );
        setDefaultDescriptorSpecs(dataProductVersion);
    }

    @Override
    protected Specification<DataProductVersion> getSpecFromFilters(DataProductVersionSearchOptions searchOptions) {
        throw new NotImplemented("Paginated reads are not supported for DataProductVersionCrudService");
    }

    @Override
    protected PagingAndSortingAndSpecificationExecutorRepository<DataProductVersion, String> getRepository() {
        return repository;
    }

    @Override
    public DataProductVersionRes toRes(DataProductVersion entity) {
        return mapper.toRes(entity);
    }

    @Override
    public Page<DataProductVersion> findAllFiltered(Pageable pageable, DataProductVersionSearchOptions searchOptions) {
        throw new NotImplemented("Paginated reads are not supported for DataProductVersionCrudService");
    }

    @Override
    protected void beforeCreation(DataProductVersion dataProductVersion) {
        validateNaturalKeyConstraints(dataProductVersion, null);
    }

    @Override
    protected void beforeOverwrite(DataProductVersion dataProductVersion) {
        // For overwrite, we need to validate uniqueness excluding the current entity
        validateNaturalKeyConstraints(dataProductVersion, dataProductVersion.getUuid());
    }

    /**
     * Sets default values for descriptor spec fields if they are not present.
     * Default values: spec = "DPDS", specVersion = "1.0.0"
     *
     * @param dataProductVersion the data product version to set defaults for
     */
    private void setDefaultDescriptorSpecs(DataProductVersion dataProductVersion) {
        if (!StringUtils.hasText(dataProductVersion.getSpec())) {
            dataProductVersion.setSpec(DescriptorSpec.DPDS.name());
        }
        if (!StringUtils.hasText(dataProductVersion.getSpecVersion())) {
            dataProductVersion.setSpecVersion("1.0.0");
        }
    }

    /**
     * Validates uniqueness constraints for tag and versionNumber within a DataProduct.
     * Only one DataProductVersion can exist with the same dataProduct and tag combination.
     * Only one DataProductVersion can exist with the same dataProduct and versionNumber combination.
     *
     * @param dataProductVersion the data product version to validate
     * @param excludeUuid        UUID to exclude from uniqueness check (for updates)
     */
    private void validateNaturalKeyConstraints(DataProductVersion dataProductVersion, String excludeUuid) {
        // Validate tag uniqueness within the same DataProduct (only when tag is set)
        if (StringUtils.hasText(dataProductVersion.getTag())) {
            boolean existsByTag;
            if (StringUtils.hasText(excludeUuid)) {
                existsByTag = repository.existsByTagIgnoreCaseAndDataProductUuidAndUuidNot(
                        dataProductVersion.getTag(), dataProductVersion.getDataProductUuid(), excludeUuid);
            } else {
                existsByTag = repository.existsByTagIgnoreCaseAndDataProductUuid(
                        dataProductVersion.getTag(), dataProductVersion.getDataProductUuid());
            }
            if (existsByTag) {
                throw new ResourceConflictException(
                        String.format("A data product version with tag '%s' already exists for this data product",
                                dataProductVersion.getTag()));
            }
        }
        // Validate versionNumber uniqueness within the same DataProduct (versionNumber is required)
        boolean existsByVersionNumber;

        if (StringUtils.hasText(excludeUuid)) {
            existsByVersionNumber = repository.existsByVersionNumberIgnoreCaseAndDataProductUuidAndUuidNot(
                    dataProductVersion.getVersionNumber(), dataProductVersion.getDataProductUuid(), excludeUuid);
        } else {
            existsByVersionNumber = repository.existsByVersionNumberIgnoreCaseAndDataProductUuid(
                    dataProductVersion.getVersionNumber(), dataProductVersion.getDataProductUuid());
        }

        if (existsByVersionNumber) {
            throw new ResourceConflictException(
                    String.format("A data product version with version number '%s' already exists for this data product",
                            dataProductVersion.getVersionNumber()));
        }
    }

    // Validation helper methods

    private void validateLength(String fieldName, String value, int maxLength) {
        if (StringUtils.hasText(value) && value.length() > maxLength) {
            throw new BadRequestException(fieldName + " cannot exceed " + maxLength + " characters");
        }
    }

    private void validateValidationState(DataProductVersionValidationState validationState) {
        if (validationState != null) {
            try {
                DataProductVersionValidationState.valueOf(validationState.name());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid validation state: " + validationState);
            }
        }
    }

    @Override
    public DataProductVersionRes overwriteResource(String uuid, DataProductVersionRes resource) {
        // Force the UUID in the resource to match the path parameter
        resource.setUuid(uuid);
        return super.overwriteResource(uuid, resource);
    }

}
