package org.opendatamesh.platform.pp.registry.dataproduct.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.platform.pp.registry.dataproduct.resources.DataProductRes;
import org.opendatamesh.platform.pp.registry.dataproduct.resources.ProviderType;
import org.opendatamesh.platform.pp.registry.githandler.auth.gitprovider.PatCredential;
import org.opendatamesh.platform.pp.registry.githandler.model.Repository;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProvider;
import org.opendatamesh.platform.pp.registry.githandler.provider.GitProviderFactory;
import org.opendatamesh.platform.pp.registry.utils.services.GenericCrudServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;


@Service
public class DataProductServiceImpl extends GenericCrudServiceImpl<DataProductRes, String> implements DataProductService {

    @Autowired
    GitProviderFactory gitProviderFactory;

    @Override
    protected PagingAndSortingRepository<DataProductRes, String> getRepository() {
        return null;
    }

    @Override
    protected void validate(DataProductRes objectToValidate) {

    }

    @Override
    protected void reconcile(DataProductRes objectToReconcile) {

    }

    @Override
    public JsonNode getDescriptor(String uuid, VersionPointer pointer, PatCredential credential) {

        DataProductRes dataProduct = findOne(uuid);
        ProviderType providerType = dataProduct.getDataProductRepositoryRes().getProviderType();
        String gitRemoteBaseUrl = dataProduct.getDataProductRepositoryRes().getProviderBaseUrl();
        GitProvider provider = gitProviderFactory.getProvider(providerType, gitRemoteBaseUrl, null, credential, null,  null);
        //Repository gitRepo = provider.getRepository(dataProduct.getDataProductRepositoryRes().getExternalIdentifier());
        //return provider.readRepository(gitRepo.)
        return null;
    }
}
