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

        public static Specification<DataProductVersionShort> hasVersionNumber(String versionNumber) {
            return (root, query, cb) -> {
                if (!StringUtils.hasText(versionNumber)) {
                    return cb.conjunction();
                }
                return cb.equal(cb.lower(root.get(DataProductVersionShort_.versionNumber)), versionNumber.toLowerCase());
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

        /**
         * Matches rows where {@code extension_properties_snapshot} contains the scope object with a
         * property whose scalar JSON value stringifies to the same text as {@code value} (PostgreSQL
         * {@code jsonb_extract_path_text}). Scope and key are case-sensitive; value is an exact textual
         * match to the extracted scalar representation (v1: suitable for string filters).
         */
        public static Specification<DataProductVersionShort> hasExtensionPropertyTriple(String scope, String key, String value) {
            return (root, query, cb) -> {
                jakarta.persistence.criteria.Expression<String> extracted = cb.function(
                        "jsonb_extract_path_text",
                        String.class,
                        root.get("extensionPropertiesSnapshot"),
                        cb.literal(scope),
                        cb.literal(key)
                );
                return cb.equal(extracted, cb.literal(value));
            };
        }
    }
}
