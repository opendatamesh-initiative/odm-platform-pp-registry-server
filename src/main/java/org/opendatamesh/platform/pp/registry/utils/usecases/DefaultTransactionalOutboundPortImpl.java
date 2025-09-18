package org.opendatamesh.platform.pp.registry.utils.usecases;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Function;

public class DefaultTransactionalOutboundPortImpl implements TransactionalOutboundPort {

    private final TransactionTemplate transactionTemplate;

    public DefaultTransactionalOutboundPortImpl(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
    }

    @Override
    public void doInTransaction(Runnable runnable) {
        transactionTemplate.executeWithoutResult(status -> runnable.run());
    }

    @Override
    public <T, R> R doInTransactionWithResults(Function<T, R> function, T arg) {
        return transactionTemplate.execute(status -> function.apply(arg));
    }
}
