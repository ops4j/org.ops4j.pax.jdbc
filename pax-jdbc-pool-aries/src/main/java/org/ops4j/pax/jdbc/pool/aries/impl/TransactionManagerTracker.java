package org.ops4j.pax.jdbc.pool.aries.impl;

import org.apache.aries.transaction.AriesTransactionManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TransactionManagerTracker extends
    ServiceTracker<AriesTransactionManager, DataSourceFactoryTracker> {

    private Logger LOG = LoggerFactory.getLogger(TransactionManagerTracker.class);

    public TransactionManagerTracker(BundleContext context) {
        super(context, AriesTransactionManager.class, null);
    }

    @Override
    public DataSourceFactoryTracker addingService(
        ServiceReference<AriesTransactionManager> reference) {
        LOG.info("TransactionManager service detected. Providing support for XA DataSourceFactories");
        AriesTransactionManager tm = context.getService(reference);
        DataSourceFactoryTracker dsManager = new DataSourceFactoryTracker(context, tm);
        dsManager.open();
        return dsManager;
    }

    @Override
    public void modifiedService(ServiceReference<AriesTransactionManager> reference,
        DataSourceFactoryTracker service) {
    }

    @Override
    public void removedService(ServiceReference<AriesTransactionManager> reference,
        DataSourceFactoryTracker service) {
        LOG.info("TransactionManager service lost. Shutting down support for XA DataSourceFactories");
        service.close();
        context.ungetService(reference);
    }

}
