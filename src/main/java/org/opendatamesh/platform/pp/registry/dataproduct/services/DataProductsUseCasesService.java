package org.opendatamesh.platform.pp.registry.dataproduct.services;

import org.opendatamesh.platform.pp.registry.dataproduct.entities.DataProduct;
import org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.init.DataProductInitCommand;
import org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.init.DataProductInitPresenter;
import org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.init.DataProductInitializerFactory;
import org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.reject.DataProductRejectCommand;
import org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.reject.DataProductRejectPresenter;
import org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.reject.DataProductRejectorFactory;
import org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approve.DataProductApproveCommand;
import org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approve.DataProductApprovePresenter;
import org.opendatamesh.platform.pp.registry.dataproduct.services.usecases.approve.DataProductApproverFactory;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.DataProductMapper;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.init.DataProductInitCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.init.DataProductInitResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.reject.DataProductRejectCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.reject.DataProductRejectResultRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.approve.DataProductApproveCommandRes;
import org.opendatamesh.platform.pp.registry.rest.v2.resources.dataproduct.usecases.approve.DataProductApproveResultRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataProductsUseCasesService {

    @Autowired
    private DataProductInitializerFactory dataProductInitializerFactory;
    @Autowired
    private DataProductApproverFactory dataProductApproverFactory;
    @Autowired
    private DataProductRejectorFactory dataProductRejectorFactory;
    @Autowired
    private DataProductMapper mapper;

    public DataProductInitResultRes initializeDataProduct(DataProductInitCommandRes initCommandRes) {
        DataProductInitCommand initCommand = new DataProductInitCommand(mapper.toEntity(initCommandRes.getDataProduct()));

        // Create a result holder that implements the presenter interface
        DataProductResultHolder resultHolder = new DataProductResultHolder();

        dataProductInitializerFactory.buildDataProductInitializer(
                initCommand,
                resultHolder
        ).execute();

        return new DataProductInitResultRes(mapper.toRes(resultHolder.getResult()));
    }

    public DataProductApproveResultRes approveDataProduct(DataProductApproveCommandRes approveCommandRes) {
        DataProductApproveCommand approveCommand = new DataProductApproveCommand(mapper.toEntity(approveCommandRes.getDataProduct()));

        // Create a result holder that implements the presenter interface
        DataProductApproveResultHolder resultHolder = new DataProductApproveResultHolder();

        dataProductApproverFactory.buildDataProductApprover(
                approveCommand,
                resultHolder
        ).execute();

        return new DataProductApproveResultRes(mapper.toRes(resultHolder.getResult()));
    }

    public DataProductRejectResultRes rejectDataProduct(DataProductRejectCommandRes rejectCommandRes) {
        DataProductRejectCommand rejectCommand = new DataProductRejectCommand(mapper.toEntity(rejectCommandRes.getDataProduct()));

        // Create a result holder that implements the presenter interface
        DataProductRejectResultHolder resultHolder = new DataProductRejectResultHolder();

        dataProductRejectorFactory.buildDataProductRejector(
                rejectCommand,
                resultHolder
        ).execute();

        return new DataProductRejectResultRes(mapper.toRes(resultHolder.getResult()));
    }

    // Inner class to hold the result for init
    private static class DataProductResultHolder implements DataProductInitPresenter {
        private DataProduct result;

        @Override
        public void presentDataProductInitialized(DataProduct dataProduct) {
            this.result = dataProduct;
        }

        public DataProduct getResult() {
            return result;
        }
    }

    // Inner class to hold the result for approve
    private static class DataProductApproveResultHolder implements DataProductApprovePresenter {
        private DataProduct result;

        @Override
        public void presentDataProductApproved(DataProduct dataProduct) {
            this.result = dataProduct;
        }

        public DataProduct getResult() {
            return result;
        }
    }

    // Inner class to hold the result for reject
    private static class DataProductRejectResultHolder implements DataProductRejectPresenter {
        private DataProduct result;

        @Override
        public void presentDataProductRejected(DataProduct dataProduct) {
            this.result = dataProduct;
        }

        public DataProduct getResult() {
            return result;
        }
    }
}
