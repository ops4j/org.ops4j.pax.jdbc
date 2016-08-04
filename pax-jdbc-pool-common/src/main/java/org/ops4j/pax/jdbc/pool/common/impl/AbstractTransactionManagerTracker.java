package org.ops4j.pax.jdbc.pool.common.impl;

import javax.transaction.TransactionManager;

import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public abstract class AbstractTransactionManagerTracker extends
    ServiceTracker<TransactionManager, ServiceRegistration> {

    private Logger LOG = LoggerFactory.getLogger(TransactionManager.class);
    private ServiceReference<TransactionManager> selectedService;

    public AbstractTransactionManagerTracker(BundleContext context) {
        super(context, TransactionManager.class, null);
    }

    @Override
    public ServiceRegistration addingService(ServiceReference<TransactionManager> reference) {
        synchronized (this) {
            if (selectedService != null) {
                LOG.warn("There is more than one TransactionManager service. Ignoring this one");
                return null;
            }
            selectedService = reference;
        }
        LOG.info("TransactionManager service detected. Providing support for XA DataSourceFactories");
        TransactionManager tm = context.getService(reference);
        return createService(context, tm);
    }

    @Override
    public void modifiedService(ServiceReference<TransactionManager> reference,
                                ServiceRegistration sreg) {
        LOG.info("TransactionManager service modified");
    }

    @Override
    public void removedService(ServiceReference<TransactionManager> reference,
                               ServiceRegistration sreg) {
        synchronized (this) {
            if (selectedService == null || !selectedService.equals(reference)) {
                return;
            }
            selectedService = null;
        }
        
        LOG.info("TransactionManager service lost. Shutting down support for XA DataSourceFactories");
        sreg.unregister();
        context.ungetService(reference);
    }

    public abstract ServiceRegistration<PooledDataSourceFactory> createService(BundleContext context,
        TransactionManager tm);
}
