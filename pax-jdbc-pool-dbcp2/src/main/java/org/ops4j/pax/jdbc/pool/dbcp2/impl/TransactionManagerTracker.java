package org.ops4j.pax.jdbc.pool.dbcp2.impl;

import javax.transaction.TransactionManager;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TransactionManagerTracker implements
    ServiceTrackerCustomizer<TransactionManager, Object> {

    private Logger LOG = LoggerFactory.getLogger(TransactionManager.class);

    private BundleContext context;
    private ServiceTracker<DataSourceFactory, Object> dsfTracker;

    public TransactionManagerTracker(BundleContext context) {
        this.context = context;
        this.dsfTracker = null;
    }

    @Override
    public void removedService(ServiceReference<TransactionManager> reference, Object service) {
        LOG.info("TransactionManager service lost. Shutting down support for XA DataSourceFactories");
        if (this.dsfTracker != null) {
            this.dsfTracker.close();
            this.dsfTracker = null;
        }
        context.ungetService(reference);
    }

    @Override
    public void modifiedService(ServiceReference<TransactionManager> reference, Object service) {
        LOG.info("TransactionManager service modified");
    }

    @Override
    public Object addingService(ServiceReference<TransactionManager> reference) {
        LOG.info("TransactionManager service detected. Providing support for XA DataSourceFactories");
        TransactionManager tm = context.getService(reference);
        DataSourceFactoryTracker dsManager = new DataSourceFactoryTracker(context, tm);
        dsfTracker = new ServiceTracker<DataSourceFactory, Object>(context,
            DataSourceFactory.class, dsManager);
        dsfTracker.open();
        return tm;
    }
}
