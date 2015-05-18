package org.ops4j.pax.jdbc.pool.aries.impl;

import javax.transaction.TransactionManager;

import org.ops4j.pax.jdbc.pool.common.impl.AbstractDataSourceFactoryTracker;
import org.ops4j.pax.jdbc.pool.common.impl.AbstractTransactionManagerTracker;
import org.osgi.framework.BundleContext;

final class AriesTransactionManagerTracker extends AbstractTransactionManagerTracker {

    public AriesTransactionManagerTracker(BundleContext context) {
        super(context);
    }

    public AbstractDataSourceFactoryTracker createTracker(BundleContext context,
        TransactionManager tm) {
        return new AriesDataSourceFactoryTracker(context, tm);
    }

}
