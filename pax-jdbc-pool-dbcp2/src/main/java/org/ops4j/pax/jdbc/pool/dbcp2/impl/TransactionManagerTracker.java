package org.ops4j.pax.jdbc.pool.dbcp2.impl;

import javax.transaction.TransactionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

@SuppressWarnings({ "unchecked", "rawtypes" })
final class TransactionManagerTracker implements ServiceTrackerCustomizer {

    private Logger LOG = LoggerFactory.getLogger(TransactionManager.class);

    private BundleContext context;
    private ServiceTracker dsfTracker;

    public TransactionManagerTracker(BundleContext context) {
        this.context = context;
        this.dsfTracker = null;
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        LOG.info("TransactionManager service lost. Shutting down support for XA DataSourceFactories");
        context.ungetService(reference);
        if (this.dsfTracker != null) {
            this.dsfTracker.close();
            this.dsfTracker = null;
        }
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
    }

    @Override
    public Object addingService(ServiceReference reference) {
        LOG.info("TransactionManager service detected. Providing support for XA DataSourceFactories");
        TransactionManager tm = (TransactionManager) context.getService(reference);
        DataSourceFactoryTracker dsManager = new DataSourceFactoryTracker(context, tm);
        dsfTracker = new ServiceTracker(context, DataSourceFactory.class.getName(), dsManager);
        dsfTracker.open();
        return null;
    }
}
