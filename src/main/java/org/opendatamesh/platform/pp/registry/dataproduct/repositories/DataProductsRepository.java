package org.opendatamesh.platform.pp.registry.dataproduct.repositories;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.utils.repositories.PagingAndSortingAndSpecificationExecutorRepository;
import org.opendatamesh.platform.pp.registry.utils.repositories.SpecsUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.Optional;

public interface DataProductsRepository extends PagingAndSortingAndSpecificationExecutorRepository<DataProduct, String> {

    Optional<DataProduct> findByNameIgnoreCaseAndDomainIgnoreCase(String name, String domain);

    Optional<DataProduct> findByFqnIgnoreCase(String fqn);

    Optional<DataProduct> findByNameIgnoreCaseAndDomainIgnoreCaseAndUuidNot(String name, String domain, String uuid);

    Optional<DataProduct> findByFqnIgnoreCaseAndUuidNot(String fqn, String uuid);
    class Specs extends SpecsUtils {

        public static Specification<DataProduct> hasDomain(String domain) {
            return (root, query, cb) -> {
                if (!StringUtils.hasText(domain)) {
                    return cb.conjunction();
                }
                return cb.equal(cb.lower(root.get("domain")), domain.toLowerCase());
            };
        }

        public static Specification<DataProduct> hasName(String name) {
            return (root, query, cb) -> {
                if (!StringUtils.hasText(name)) {
                    return cb.conjunction();
                }
                return cb.equal(cb.lower(root.get("name")), name.toLowerCase());
            };
        }

        public static Specification<DataProduct> hasFqn(String fqn) {
            return (root, query, cb) -> {
                if (!StringUtils.hasText(fqn)) {
                    return cb.conjunction();
                }
                return cb.equal(cb.lower(root.get("fqn")), fqn.toLowerCase());
            };
        }
    }
}
