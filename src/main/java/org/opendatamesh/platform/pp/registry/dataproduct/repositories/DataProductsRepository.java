package org.opendatamesh.platform.pp.registry.dataproduct.repositories;

import jakarta.persistence.criteria.Expression;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct_;
import org.opendatamesh.platform.pp.registry.utils.repositories.PagingAndSortingAndSpecificationExecutorRepository;
import org.opendatamesh.platform.pp.registry.utils.repositories.SpecsUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public interface DataProductsRepository extends PagingAndSortingAndSpecificationExecutorRepository<DataProduct, String> {

    // JPA named methods for uniqueness validation

    /**
     * Check if a DataProduct exists by name and domain (case-insensitive)
     */
    boolean existsByNameIgnoreCaseAndDomainIgnoreCase(String name, String domain);

    /**
     * Check if a DataProduct exists by FQN (case-insensitive)
     */
    boolean existsByFqnIgnoreCase(String fqn);

    /**
     * Check if a DataProduct exists by name and domain excluding a specific UUID (case-insensitive)
     */
    boolean existsByNameIgnoreCaseAndDomainIgnoreCaseAndUuidNot(String name, String domain, String uuid);

    /**
     * Check if a DataProduct exists by FQN excluding a specific UUID (case-insensitive)
     */
    boolean existsByFqnIgnoreCaseAndUuidNot(String fqn, String uuid);

    class Specs extends SpecsUtils {

        public static Specification<DataProduct> hasDomain(String domain) {
            return (root, query, cb) -> {
                if (!StringUtils.hasText(domain)) {
                    return cb.conjunction();
                }
                return cb.equal(cb.lower(root.get(DataProduct_.domain)), domain.toLowerCase());
            };
        }

        public static Specification<DataProduct> hasName(String name) {
            return (root, query, cb) -> {
                if (!StringUtils.hasText(name)) {
                    return cb.conjunction();
                }
                return cb.equal(cb.lower(root.get(DataProduct_.name)), name.toLowerCase());
            };
        }

        public static Specification<DataProduct> hasFqn(String fqn) {
            return (root, query, cb) -> {
                if (!StringUtils.hasText(fqn)) {
                    return cb.conjunction();
                }
                return cb.equal(cb.lower(root.get(DataProduct_.fqn)), fqn.toLowerCase());
            };
        }

        /**
         * Matches rows where {@code extension_properties} contains the given scope
         * object with a property
         * whose scalar JSON value stringifies to the same text as {@code value}
         * (PostgreSQL
         * {@code jsonb_extract_path_text}). Scope and key are case-sensitive; value is
         * an exact textual
         * match to the extracted scalar representation (v1: suitable for string
         * filters).
         */
        public static Specification<DataProduct> hasExtensionPropertyTriple(String scope, String key, String value) {
            return (root, query, cb) -> {
                Expression<String> extracted = cb.function(
                        "jsonb_extract_path_text",
                        String.class,
                        root.get("extensionProperties"),
                        cb.literal(scope),
                        cb.literal(key));
                return cb.equal(extracted, cb.literal(value));
            };
        }
    }
}
