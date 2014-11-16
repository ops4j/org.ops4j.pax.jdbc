/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.jdbc.pool.aries.impl;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.aries.transaction.AriesTransactionManager;
import org.ops4j.pax.jdbc.pool.aries.impl.ds.PooledDataSourceFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches for DataSourceFactory services and creates/destroys a PooledDataSourceFactory for each
 * existing DataSourceFactory
 */
public class DataSourceFactoryTracker extends
    ServiceTracker<DataSourceFactory, ServiceRegistration<DataSourceFactory>> {

    private Logger LOG = LoggerFactory.getLogger(DataSourceFactoryTracker.class);

    private AriesTransactionManager tm;

    public DataSourceFactoryTracker(BundleContext context) {
        this(context, null);
    }

    public DataSourceFactoryTracker(BundleContext context, AriesTransactionManager tm) {
        super(context, DataSourceFactory.class, null);
        this.tm = tm;
    }

    @Override
    public ServiceRegistration<DataSourceFactory> addingService(
        ServiceReference<DataSourceFactory> reference) {
        if (reference.getProperty("pooled") != null) {
            // Make sure we do not react on our own service for the pooled factory
            return null;
        }

        LOG.debug("Registering PooledDataSourceFactory");
        DataSourceFactory dsf = context.getService(reference);
        PooledDataSourceFactory pdsf = new PooledDataSourceFactory(dsf, tm);
        Dictionary<String, Object> props = createPropsForPoolingDataSourceFactory(reference);
        return context.registerService(DataSourceFactory.class, pdsf, props);
    }

    private Dictionary<String, Object> createPropsForPoolingDataSourceFactory(
        ServiceReference<DataSourceFactory> reference) {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        for (String key : reference.getPropertyKeys()) {
            if (!"service.id".equals(key)) {
                props.put(key, reference.getProperty(key));
            }
        }
        props.put("pooled", "true");
        if (tm != null) {
            props.put("xa", "true");
        }
        props.put(DataSourceFactory.OSGI_JDBC_DRIVER_NAME, getPoolDriverName(reference));
        return props;
    }

    private String getPoolDriverName(ServiceReference<DataSourceFactory> reference) {
        String origName = (String) reference.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_NAME);
        if (origName == null) {
            origName = (String) reference.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
        }
        return origName + "-pool" + ((tm != null) ? "-xa" : "");
    }

    @Override
    public void modifiedService(ServiceReference<DataSourceFactory> reference,
        ServiceRegistration<DataSourceFactory> service) {

    }

    @Override
    public void removedService(ServiceReference<DataSourceFactory> reference,
        ServiceRegistration<DataSourceFactory> service) {
        LOG.warn("Unregistering PooledDataSourceFactory");
        service.unregister();
        context.ungetService(reference);
    }

}
