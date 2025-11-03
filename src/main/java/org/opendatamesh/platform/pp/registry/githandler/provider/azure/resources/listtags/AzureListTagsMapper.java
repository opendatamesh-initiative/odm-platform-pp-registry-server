package org.opendatamesh.platform.pp.registry.githandler.provider.azure.resources.listtags;

import org.opendatamesh.platform.pp.registry.githandler.model.Tag;

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

