package org.ops4j.pax.jdbc.config.impl;

import java.util.Dictionary;

import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class PooledDataSourceFactoryTracker extends ServiceTracker {
    private Logger LOG = LoggerFactory.getLogger(DataSourceRegistration.class);

    private Filter dsFilter;
    private Dictionary config;
    private Dictionary decryptedConfig;

    public PooledDataSourceFactoryTracker(BundleContext context, 
                                          Filter pdsFilter, 
                                          Filter dsFilter,
                                          Dictionary config,
                                          Dictionary decryptedConfig
                                          ) {
        super(context, pdsFilter, null);
        this.dsFilter = dsFilter;
        this.config = config;
        this.decryptedConfig = decryptedConfig;
    }

    @Override
    public Object addingService(ServiceReference reference) {
        PooledDataSourceFactory pdsf = (PooledDataSourceFactory)super.addingService(reference);
        LOG.info("Found pooling support for DataSource {}. Now tracking DataSourceFactory with filter {}", DataSourceRegistration.getDSName(config), dsFilter);
        DataSourceFactoryTracker tracker = new DataSourceFactoryTracker(context, pdsf, dsFilter, config, decryptedConfig);
        tracker.open();
        return tracker;
    }
    
    @Override
    public void removedService(ServiceReference reference, Object tracked) {
        super.removedService(reference, tracked);
        ((ServiceTracker)tracked).close();
    }
}
