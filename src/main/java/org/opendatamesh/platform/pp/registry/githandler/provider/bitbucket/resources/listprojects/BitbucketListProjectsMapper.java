package org.opendatamesh.platform.pp.registry.githandler.provider.bitbucket.resources.listprojects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendatamesh.platform.pp.registry.githandler.model.ProviderCustomResource;

public class BitbucketListProjectsMapper {

    /**
     * Converts Bitbucket project response to ProviderCustomResource
     */
    public static ProviderCustomResource toProviderCustomResource(BitbucketListProjectsProjectRes projectRes) {
        if (projectRes == null) {
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode content = mapper.createObjectNode();
        
        if (projectRes.getKey() != null) {
            content.put("key", projectRes.getKey());
        }
        if (projectRes.getType() != null) {
            content.put("type", projectRes.getType());
        }
        if (projectRes.getUuid() != null) {
            content.put("uuid", projectRes.getUuid());
        }
        if (projectRes.getLinks() != null && projectRes.getLinks().getHtml() != null) {
            content.put("htmlUrl", projectRes.getLinks().getHtml().getHref());
        }

        return new ProviderCustomResource(
                projectRes.getUuid() != null ? projectRes.getUuid() : projectRes.getKey(),
                projectRes.getName(),
                content
        );
    }
}

