package org.opendatamesh.platform.pp.registry.utils.git.provider.azure.resources.listtags;

import org.opendatamesh.platform.pp.registry.utils.git.model.Tag;

public abstract class AzureListTagsMapper {

    public static Tag toInternalModel(AzureListTagsTagRes tagRes) {
        if (tagRes == null) {
            return null;
        }

        return new Tag(
                tagRes.getName().replace("refs/tags/", ""),
                tagRes.getObjectId()
        );
    }
}

