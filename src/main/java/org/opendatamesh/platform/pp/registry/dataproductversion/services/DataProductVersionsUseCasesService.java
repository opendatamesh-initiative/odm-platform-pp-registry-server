package org.opendatamesh.platform.pp.registry.dataproductversion.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approve.DataProductVersionApproveCommand;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approve.DataProductVersionApprovePresenter;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approve.DataProductVersionApproverFactory;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.delete.DataProductVersionDeleteCommand;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.delete.DataProductVersionDeletePresenter;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.delete.DataProductVersionDeleterFactory;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.documentationfieldsupdate.DataProductVersionDocumentationFieldsUpdateCommand;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.documentationfieldsupdate.DataProductVersionDocumentationFieldsUpdatePresenter;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.documentationfieldsupdate.DataProductVersionDocumentationFieldsUpdaterFactory;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish.DataProductVersionPublishCommand;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish.DataProductVersionPublishPresenter;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish.DataProductVersionPublisherFactory;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.reject.DataProductVersionRejectCommand;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.reject.DataProductVersionRejectPresenter;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.reject.DataProductVersionRejectorFactory;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.resolvevariables.DataProductVersionVariablesResolverCommand;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.resolvevariables.DataProductVersionVariablesResolverFactory;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.resolvevariables.DataProductVersionVariablesResolverPresenter;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.approve.DataProductVersionApproveCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.approve.DataProductVersionApproveResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.delete.DataProductVersionDeleteCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.documentationfieldsupdate.DataProductVersionDocumentationFieldsRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.documentationfieldsupdate.DataProductVersionDocumentationFieldsUpdateCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.documentationfieldsupdate.DataProductVersionDocumentationFieldsUpdateResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.publish.DataProductVersionPublishCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.publish.DataProductVersionPublishResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.reject.DataProductVersionRejectCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.reject.DataProductVersionRejectResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.resolve.ResolveDataProductVersionCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.resolve.ResolveDataProductVersionResultRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataProductVersionsUseCasesService {

    @Autowired
    private DataProductVersionPublisherFactory dataProductVersionPublisherFactory;
    @Autowired
    private DataProductVersionApproverFactory dataProductVersionApproverFactory;
    @Autowired
    private DataProductVersionRejectorFactory dataProductVersionRejectorFactory;
    @Autowired
    private DataProductVersionDocumentationFieldsUpdaterFactory dataProductVersionDocumentationFieldsUpdaterFactory;
    @Autowired
    private DataProductVersionDeleterFactory dataProductVersionDeleterFactory;
    @Autowired
    private DataProductVersionVariablesResolverFactory dataProductVersionVariablesResolverFactory;

    @Autowired
    private DataProductVersionMapper mapper;

    public DataProductVersionPublishResultRes publishDataProductVersion(DataProductVersionPublishCommandRes publishCommandRes) {
        DataProductVersionPublishCommand publishCommand = new DataProductVersionPublishCommand(mapper.toEntity(publishCommandRes.getDataProductVersion()));

        DataProductVersionResultHolder resultHolder = new DataProductVersionResultHolder();

        dataProductVersionPublisherFactory.buildDataProductVersionPublisher(
                publishCommand,
                resultHolder
        ).execute();

        return new DataProductVersionPublishResultRes(mapper.toRes(resultHolder.getResult()));
    }

    public DataProductVersionApproveResultRes approveDataProductVersion(DataProductVersionApproveCommandRes approveCommandRes) {
        DataProductVersionApproveCommand approveCommand = new DataProductVersionApproveCommand(mapper.toEntity(approveCommandRes.getDataProductVersion()));

        DataProductVersionApproveResultHolder resultHolder = new DataProductVersionApproveResultHolder();

        dataProductVersionApproverFactory.buildDataProductVersionApprover(
                approveCommand,
                resultHolder
        ).execute();

        return new DataProductVersionApproveResultRes(mapper.toRes(resultHolder.getResult()));
    }

    public DataProductVersionRejectResultRes rejectDataProductVersion(DataProductVersionRejectCommandRes rejectCommandRes) {
        DataProductVersionRejectCommand rejectCommand = new DataProductVersionRejectCommand(mapper.toEntity(rejectCommandRes.getDataProductVersion()));

        DataProductVersionRejectResultHolder resultHolder = new DataProductVersionRejectResultHolder();

        dataProductVersionRejectorFactory.buildDataProductVersionRejector(
                rejectCommand,
                resultHolder
        ).execute();

        return new DataProductVersionRejectResultRes(mapper.toRes(resultHolder.getResult()));
    }

    public DataProductVersionDocumentationFieldsUpdateResultRes updateDocumentationFieldsDataProductVersion(DataProductVersionDocumentationFieldsUpdateCommandRes updateDocumentationFieldsCommandRes) {
        DataProductVersionDocumentationFieldsRes documentationFieldsRes = updateDocumentationFieldsCommandRes.getDataProductVersion();

        DataProductVersionDocumentationFieldsUpdateCommand updateDocumentationFieldsCommand =
                new DataProductVersionDocumentationFieldsUpdateCommand(
                        documentationFieldsRes.getUuid(),
                        documentationFieldsRes.getName(),
                        documentationFieldsRes.getDescription(),
                        documentationFieldsRes.getUpdatedBy());

        DataProductVersionDocumentationFieldsUpdateResultHolder resultHolder = new DataProductVersionDocumentationFieldsUpdateResultHolder();

        dataProductVersionDocumentationFieldsUpdaterFactory.buildDataProductVersionDocumentationFieldsUpdater(
                updateDocumentationFieldsCommand,
                resultHolder
        ).execute();

        return new DataProductVersionDocumentationFieldsUpdateResultRes(mapper.toRes(resultHolder.getResult()));

    }

    public void deleteDataProductVersion(DataProductVersionDeleteCommandRes deleteCommandRes) {
        DataProductVersionDeleteCommand deleteCommand = new DataProductVersionDeleteCommand(
                deleteCommandRes.getDataProductVersionUuid(),
                deleteCommandRes.getDataProductFqn(),
                deleteCommandRes.getDataProductVersionTag()
        );

        // Create a simple presenter (no-op since we don't return a result)
        DataProductVersionDeletePresenter presenter = dataProductVersion -> {
            // No-op: we don't need to return anything for delete
        };

        dataProductVersionDeleterFactory.buildDataProductVersionDeleter(
                deleteCommand,
                presenter
        ).execute();
    }

    public ResolveDataProductVersionResultRes resolveDataProductVersion(ResolveDataProductVersionCommandRes resolveCommandRes) {
        DataProductVersionVariablesResolverCommand resolveCommand = new DataProductVersionVariablesResolverCommand(
                resolveCommandRes.getDataProductVersionUuid());

        DataProductVersionVariablesResolverResultHolder resultHolder = new DataProductVersionVariablesResolverResultHolder();

        dataProductVersionVariablesResolverFactory.buildResolveDataProductVersion(
                resolveCommand,
                resultHolder
        ).execute();

        ResolveDataProductVersionResultRes.ResolvedDataProductVersionRes resolvedDataProductVersionRes = mapper.toResolvedRes(resultHolder.getDataProductVersion(), resultHolder.getResolvedContent());
        return new ResolveDataProductVersionResultRes(resolvedDataProductVersionRes);
    }

    // Inner class to hold the result for publish
    private static class DataProductVersionResultHolder implements DataProductVersionPublishPresenter {
        private DataProductVersion result;

        @Override
        public void presentDataProductVersionPublished(DataProductVersion dataProductVersion) {
            this.result = dataProductVersion;
        }

        public DataProductVersion getResult() {
            return result;
        }
    }

    // Inner class to hold the result for approve
    private static class DataProductVersionApproveResultHolder implements DataProductVersionApprovePresenter {
        private DataProductVersion result;

        @Override
        public void presentDataProductVersionApproved(DataProductVersion dataProductVersion) {
            this.result = dataProductVersion;
        }

        public DataProductVersion getResult() {
            return result;
        }
    }

    // Inner class to hold the result for reject
    private static class DataProductVersionRejectResultHolder implements DataProductVersionRejectPresenter {
        private DataProductVersion result;

        @Override
        public void presentDataProductVersionRejected(DataProductVersion dataProductVersion) {
            this.result = dataProductVersion;
        }

        public DataProductVersion getResult() {
            return result;
        }
    }

    // Inner class to hold the result for update documentation fields
    private static class DataProductVersionDocumentationFieldsUpdateResultHolder implements DataProductVersionDocumentationFieldsUpdatePresenter {
        private DataProductVersion result;

        @Override
        public void presentDataProductVersionDocumentationFieldsUpdated(DataProductVersion dataProductVersion) {
            this.result = dataProductVersion;
        }

        public DataProductVersion getResult() {
            return result;
        }
    }

    // Inner class to hold the result for resolve
    private static class DataProductVersionVariablesResolverResultHolder implements DataProductVersionVariablesResolverPresenter {
        private JsonNode resolvedContent;
        private DataProductVersion dataProductVersion;

        @Override
        public void presentDataProductVersionResolvedContent(DataProductVersion dataProductVersion, JsonNode resolvedContent) {
            this.resolvedContent = resolvedContent;
            this.dataProductVersion = dataProductVersion;
        }

        public DataProductVersion getDataProductVersion() {
            return dataProductVersion;
        }

        public JsonNode getResolvedContent() {
            return resolvedContent;
        }
    }
}
