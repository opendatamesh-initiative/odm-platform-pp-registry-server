package org.opendatamesh.platform.pp.registry.utils.git.provider;

import org.opendatamesh.platform.pp.registry.utils.git.model.ProviderCustomResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

public interface GitProviderCustomResourceReader {
    boolean support(String resourceType);

    Page<ProviderCustomResource> getCustomResources(MultiValueMap<String, String> parameters, Pageable pageable);
}
