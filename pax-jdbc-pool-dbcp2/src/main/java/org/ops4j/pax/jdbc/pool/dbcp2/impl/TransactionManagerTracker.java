package org.ops4j.pax.jdbc.pool.dbcp2.impl;

import org.ops4j.pax.jdbc.pool.common.impl.AbstractTransactionManagerTracker;

import org.osgi.service.jdbc.DataSourceFactory;
import org.ops4j.pax.jdbc.pool.common.impl.AbstractDataSourceFactoryTracker;
import javax.transaction.TransactionManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
final class TransactionManagerTracker extends AbstractTransactionManagerTracker {

    private Logger LOG = LoggerFactory.getLogger(TransactionManager.class);

    public TransactionManagerTracker(BundleContext context) {
        super(context);
    }

    @Override
    public AbstractDataSourceFactoryTracker createTracker(BundleContext context,
        TransactionManager tm) {

        return new DataSourceFactoryTracker(context, tm);
    }

}
