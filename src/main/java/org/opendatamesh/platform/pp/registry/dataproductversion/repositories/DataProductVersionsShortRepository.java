package org.opendatamesh.platform.pp.registry.dataproductversion.repositories;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionShort;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionShort_;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersionValidationState;
import org.opendatamesh.platform.pp.registry.utils.repositories.PagingAndSortingAndSpecificationExecutorRepository;
import org.opendatamesh.platform.pp.registry.utils.repositories.SpecsUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public interface DataProductVersionsShortRepository extends PagingAndSortingAndSpecificationExecutorRepository<DataProductVersionShort, String> {

    class Specs extends SpecsUtils {

        public static Specification<DataProductVersionShort> hasDataProductUuid(String dataProductUuid) {
            return (root, query, cb) -> {
                if (!StringUtils.hasText(dataProductUuid)) {
                    return cb.conjunction();
                }
                return cb.equal(root.get(DataProductVersionShort_.dataProductUuid), dataProductUuid);
            };
        }

        public static Specification<DataProductVersionShort> hasName(String name) {
            return (root, query, cb) -> {
                if (!StringUtils.hasText(name)) {
                    return cb.conjunction();
                }
                return cb.equal(cb.lower(root.get(DataProductVersionShort_.name)), name.toLowerCase());
            };
        }

        public static Specification<DataProductVersionShort> hasTag(String tag) {
            return (root, query, cb) -> {
                if (!StringUtils.hasText(tag)) {
                    return cb.conjunction();
                }
                return cb.equal(cb.lower(root.get(DataProductVersionShort_.tag)), tag.toLowerCase());
            };
        }

        public static Specification<DataProductVersionShort> hasValidationState(DataProductVersionValidationState validationState) {
            return (root, query, cb) -> {
                if (validationState == null) {
                    return cb.conjunction();
                }
                return cb.equal(root.get(DataProductVersionShort_.validationState), validationState);
            };
        }

        public static Specification<DataProductVersionShort> matchSearch(String search) {
            return (root, query, cb) -> {
                if (!StringUtils.hasText(search)) {
                    return cb.conjunction();
                }
                final String pattern = String.format("%%%s%%", escapeLikeParameter(search.toLowerCase(), LIKE_ESCAPE_CHAR));
                return cb.like(cb.lower(root.get(DataProductVersionShort_.name)), pattern, LIKE_ESCAPE_CHAR);
            };
        }
    }
}
