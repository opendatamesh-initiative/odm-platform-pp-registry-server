package org.opendatamesh.platform.pp.registry.descriptorvariable.services.core;

import org.opendatamesh.platform.pp.registry.dataproductversion.services.core.DataProductVersionCrudService;
import org.opendatamesh.platform.pp.registry.descriptorvariable.entities.DescriptorVariable;
import org.opendatamesh.platform.pp.registry.descriptorvariable.repositories.DescriptorVariableRepository;
import org.opendatamesh.platform.pp.registry.exceptions.BadRequestException;
import org.opendatamesh.platform.pp.registry.exceptions.ResourceConflictException;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.DescriptorVariableMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.DescriptorVariableRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.descriptorvariable.DescriptorVariableSearchOptions;
import org.opendatamesh.platform.pp.registry.utils.repositories.PagingAndSortingAndSpecificationExecutorRepository;
import org.opendatamesh.platform.pp.registry.utils.repositories.SpecsUtils;
import org.opendatamesh.platform.pp.registry.utils.services.GenericMappedAndFilteredCrudServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class DescriptorVariableCrudServiceImpl extends GenericMappedAndFilteredCrudServiceImpl<DescriptorVariableSearchOptions, DescriptorVariableRes, DescriptorVariable, Long> implements DescriptorVariableCrudService {

    private final DescriptorVariableMapper mapper;
    private final DescriptorVariableRepository repository;
    private final DataProductVersionCrudService dataProductVersionCrudService;

    @Autowired
    public DescriptorVariableCrudServiceImpl(
            DescriptorVariableMapper mapper,
            DescriptorVariableRepository repository,
            DataProductVersionCrudService dataProductVersionCrudService) {
        this.mapper = mapper;
        this.repository = repository;
        this.dataProductVersionCrudService = dataProductVersionCrudService;
    }

    @Override
    protected DescriptorVariable toEntity(DescriptorVariableRes resource) {
        return mapper.toEntity(resource);
    }

    @Override
    protected void validate(DescriptorVariable descriptorVariable) {
        validateRequiredFields(descriptorVariable);
        validateFieldConstraints(descriptorVariable);
    }

    private void validateRequiredFields(DescriptorVariable descriptorVariable) {
        if (!StringUtils.hasText(descriptorVariable.getDataProductVersionUuid())) {
            throw new BadRequestException("Missing DataProductVersion on DescriptorVariable");
        }
        if (!StringUtils.hasText(descriptorVariable.getVariableKey())) {
            throw new BadRequestException("Missing variable key on DescriptorVariable");
        }
        if (!StringUtils.hasText(descriptorVariable.getVariableValue())) {
            throw new BadRequestException("Missing variable value on DescriptorVariable");
        }
    }

    private void validateFieldConstraints(DescriptorVariable descriptorVariable) {
        validateLength("Variable key", descriptorVariable.getVariableKey(), 255);
        validateLength("Variable value", descriptorVariable.getVariableValue(), 10000); // text field, reasonable limit

    }

    @Override
    protected void reconcile(DescriptorVariable descriptorVariable) {
        descriptorVariable.setDataProductVersion(
                dataProductVersionCrudService.findOne(descriptorVariable.getDataProductVersionUuid())
        );
    }

    @Override
    protected Specification<DescriptorVariable> getSpecFromFilters(DescriptorVariableSearchOptions searchOptions) {
        List<Specification<DescriptorVariable>> specs = new ArrayList<>();
        if (StringUtils.hasText(searchOptions.getDataProductVersionUuid())) {
            specs.add(DescriptorVariableRepository.Specs.hasDataProductVersionUuid(searchOptions.getDataProductVersionUuid()));
        }
        if (StringUtils.hasText(searchOptions.getVariableKey())) {
            specs.add(DescriptorVariableRepository.Specs.hasVariableKey(searchOptions.getVariableKey()));
        }
        return SpecsUtils.combineWithAnd(specs);
    }

    @Override
    protected PagingAndSortingAndSpecificationExecutorRepository<DescriptorVariable, Long> getRepository() {
        return repository;
    }

    @Override
    public DescriptorVariableRes toRes(DescriptorVariable entity) {
        return mapper.toRes(entity);
    }

    @Override
    protected void beforeCreation(DescriptorVariable descriptorVariable) {
        validateNaturalKeyConstraints(descriptorVariable, null);
    }

    @Override
    protected void beforeOverwrite(DescriptorVariable descriptorVariable) {
        // For overwrite, we need to validate uniqueness excluding the current entity
        validateNaturalKeyConstraints(descriptorVariable, descriptorVariable.getSequenceId());
    }

    /**
     * Validates uniqueness constraints for variableKey within a DataProductVersion.
     * Only one DescriptorVariable can exist with the same dataProductVersionUuid and variableKey combination.
     *
     * @param descriptorVariable the descriptor variable to validate
     * @param excludeSequenceId        sequenceId to exclude from uniqueness check (for updates)
     */
    private void validateNaturalKeyConstraints(DescriptorVariable descriptorVariable, Long excludeSequenceId) {
        // Validate variableKey uniqueness within the same DataProductVersion
        boolean existsByKey;

        if (excludeSequenceId != null) {
            existsByKey = repository.existsByVariableKeyIgnoreCaseAndDataProductVersionUuidAndSequenceIdNot(
                    descriptorVariable.getVariableKey(), descriptorVariable.getDataProductVersionUuid(), excludeSequenceId);
        } else {
            existsByKey = repository.existsByVariableKeyIgnoreCaseAndDataProductVersionUuid(
                    descriptorVariable.getVariableKey(), descriptorVariable.getDataProductVersionUuid());
        }

        if (existsByKey) {
            throw new ResourceConflictException(
                    String.format("A descriptor variable with key '%s' already exists for this data product version",
                            descriptorVariable.getVariableKey()));
        }
    }

    // Validation helper methods

    private void validateLength(String fieldName, String value, int maxLength) {
        if (StringUtils.hasText(value) && value.length() > maxLength) {
            throw new BadRequestException(fieldName + " cannot exceed " + maxLength + " characters");
        }
    }

    @Override
    public DescriptorVariableRes overwriteResource(Long sequenceId, DescriptorVariableRes resource) {
        // Force the sequenceId in the resource to match the path parameter
        resource.setSequenceId(sequenceId);
        return super.overwriteResource(sequenceId, resource);
    }
}
