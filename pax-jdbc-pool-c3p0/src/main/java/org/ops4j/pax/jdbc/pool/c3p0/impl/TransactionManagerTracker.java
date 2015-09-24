package org.ops4j.pax.jdbc.pool.c3p0.impl;

import javax.transaction.TransactionManager;

import org.ops4j.pax.jdbc.pool.common.impl.AbstractDataSourceFactoryTracker;
import org.ops4j.pax.jdbc.pool.common.impl.AbstractTransactionManagerTracker;
import org.osgi.framework.BundleContext;

final class TransactionManagerTracker extends AbstractTransactionManagerTracker {

    public TransactionManagerTracker(BundleContext context) {
        super(context);
    }

    @Override
    public AbstractDataSourceFactoryTracker createTracker(BundleContext context, TransactionManager tm) {
        return new DataSourceFactoryTracker(context, tm);
    }

}
