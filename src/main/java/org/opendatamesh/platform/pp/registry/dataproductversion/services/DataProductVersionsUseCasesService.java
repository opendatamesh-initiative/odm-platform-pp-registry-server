package org.opendatamesh.platform.pp.registry.dataproductversion.services;

import org.opendatamesh.platform.pp.registry.dataproductversion.entities.DataProductVersion;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approve.DataProductVersionApproveCommand;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approve.DataProductVersionApprovePresenter;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.approve.DataProductVersionApproverFactory;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish.DataProductVersionPublishCommand;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish.DataProductVersionPublishPresenter;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.publish.DataProductVersionPublisherFactory;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.reject.DataProductVersionRejectCommand;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.reject.DataProductVersionRejectPresenter;
import org.opendatamesh.platform.pp.registry.dataproductversion.services.usecases.reject.DataProductVersionRejectorFactory;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.DataProductVersionMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.approve.DataProductVersionApproveCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.approve.DataProductVersionApproveResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.publish.DataProductVersionPublishCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.publish.DataProductVersionPublishResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.reject.DataProductVersionRejectCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproductversion.usecases.reject.DataProductVersionRejectResultRes;
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
}
