package org.opendatamesh.platform.pp.registry.descriptorvariable.repositories;

import org.opendatamesh.platform.pp.registry.descriptorvariable.entities.DescriptorVariable;
import org.opendatamesh.platform.pp.registry.descriptorvariable.entities.DescriptorVariable_;
import org.opendatamesh.platform.pp.registry.utils.repositories.PagingAndSortingAndSpecificationExecutorRepository;
import org.opendatamesh.platform.pp.registry.utils.repositories.SpecsUtils;
import org.springframework.data.jpa.domain.Specification;

public interface DescriptorVariableRepository extends PagingAndSortingAndSpecificationExecutorRepository<DescriptorVariable, Long> {

    // JPA named methods for uniqueness validation
    
    /**
     * Check if a DescriptorVariable exists by variableKey and dataProductVersionUuid (case-insensitive)
     */
    boolean existsByVariableKeyIgnoreCaseAndDataProductVersionUuid(String variableKey, String dataProductVersionUuid);
    
    /**
     * Check if a DescriptorVariable exists by variableKey and dataProductVersionUuid excluding a specific sequenceId (case-insensitive)
     */
    boolean existsByVariableKeyIgnoreCaseAndDataProductVersionUuidAndSequenceIdNot(String variableKey, String dataProductVersionUuid, Long excludeSequenceId);

    class Specs extends SpecsUtils {

        public static Specification<DescriptorVariable> hasDataProductVersionUuid(String dataProductVersionUuid) {
            return (root, query, cb) -> cb.equal(root.get(DescriptorVariable_.dataProductVersionUuid), dataProductVersionUuid);
        }

        public static Specification<DescriptorVariable> hasVariableKey(String variableKey) {
            return (root, query, cb) -> cb.equal(cb.lower(root.get(DescriptorVariable_.variableKey)), variableKey.toLowerCase());
        }
    }
}
