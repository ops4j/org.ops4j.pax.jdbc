package org.ops4j.pax.jdbc.pool.aries.impl;

import javax.transaction.TransactionManager;
import org.ops4j.pax.jdbc.pool.common.impl.AbstractDataSourceFactoryTracker;

import org.apache.aries.transaction.AriesTransactionManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.jdbc.pool.common.impl.AbstractTransactionManagerTracker;

final class AriesTransactionManagerTracker extends AbstractTransactionManagerTracker {

    private Logger LOG = LoggerFactory.getLogger(AriesTransactionManagerTracker.class);

    public AriesTransactionManagerTracker(BundleContext context) {
        super(context);
    }
    public AbstractDataSourceFactoryTracker createTracker(BundleContext context, TransactionManager tm) {
      return new AriesDataSourceFactoryTracker(context, tm);
    }
   

}
