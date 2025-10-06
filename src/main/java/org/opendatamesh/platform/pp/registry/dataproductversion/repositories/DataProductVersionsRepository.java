package org.opendatamesh.platform.pp.registry.dataproductversion.repositories;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion_;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionValidationState;
import org.opendatamesh.platform.pp.registry.utils.repositories.PagingAndSortingAndSpecificationExecutorRepository;
import org.opendatamesh.platform.pp.registry.utils.repositories.SpecsUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public interface DataProductVersionsRepository extends PagingAndSortingAndSpecificationExecutorRepository<DataProductVersion, String> {

    // JPA named methods for uniqueness validation
    
    /**
     * Check if a DataProductVersion exists by tag and dataProductUuid (case-insensitive)
     */
    boolean existsByTagIgnoreCaseAndDataProductUuid(String tag, String dataProductUuid);
    
    /**
     * Check if a DataProductVersion exists by tag and dataProductUuid excluding a specific UUID (case-insensitive)
     */
    boolean existsByTagIgnoreCaseAndDataProductUuidAndUuidNot(String tag, String dataProductUuid, String excludeUuid);

    class Specs extends SpecsUtils {

        public static Specification<DataProductVersion> hasDataProductUuid(String dataProductUuid) {
            return (root, query, cb) -> {
                if (!StringUtils.hasText(dataProductUuid)) {
                    return cb.conjunction();
                }
                return cb.equal(root.get(DataProductVersion_.dataProductUuid), dataProductUuid);
            };
        }

        public static Specification<DataProductVersion> hasDataProductVersionUuid(String dataProductVersionUuid) {
            return (root, query, cb) -> {
                if (!StringUtils.hasText(dataProductVersionUuid)) {
                    return cb.conjunction();
                }
                return cb.equal(root.get(DataProductVersion_.uuid), dataProductVersionUuid);
            };
        }

        public static Specification<DataProductVersion> hasName(String name) {
            return (root, query, cb) -> {
                if (!StringUtils.hasText(name)) {
                    return cb.conjunction();
                }
                return cb.equal(cb.lower(root.get(DataProductVersion_.name)), name.toLowerCase());
            };
        }

        public static Specification<DataProductVersion> hasTag(String tag) {
            return (root, query, cb) -> {
                if (!StringUtils.hasText(tag)) {
                    return cb.conjunction();
                }
                return cb.equal(cb.lower(root.get(DataProductVersion_.tag)), tag.toLowerCase());
            };
        }

        public static Specification<DataProductVersion> hasValidationState(DataProductVersionValidationState validationState) {
            return (root, query, cb) -> {
                if (validationState == null) {
                    return cb.conjunction();
                }
                return cb.equal(root.get(DataProductVersion_.validationState), validationState);
            };
        }
    }
}
