package org.ops4j.pax.jdbc.pool.dbcp2.impl;

import javax.transaction.TransactionManager;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
final class TransactionManagerTracker extends ServiceTracker<TransactionManager, ServiceTracker> {
    private Logger LOG = LoggerFactory.getLogger(TransactionManager.class);

    public TransactionManagerTracker(BundleContext context) {
    	super(context, TransactionManager.class, null);
    }

	@Override
    public void removedService(ServiceReference<TransactionManager> reference, ServiceTracker dsfTracker) {
        LOG.info("TransactionManager service lost. Shutting down support for XA DataSourceFactories");
        dsfTracker.close();
        context.ungetService(reference);
    }

    @Override
    public void modifiedService(ServiceReference<TransactionManager> reference, ServiceTracker dsfTracker) {
        LOG.info("TransactionManager service modified");
    }

    @Override
    public ServiceTracker addingService(ServiceReference<TransactionManager> reference) {
        LOG.info("TransactionManager service detected. Providing support for XA DataSourceFactories");
        TransactionManager tm = context.getService(reference);
        DataSourceFactoryTracker dsfTracker = new DataSourceFactoryTracker(context, tm);
        dsfTracker.open();
        return dsfTracker;
    }
}
