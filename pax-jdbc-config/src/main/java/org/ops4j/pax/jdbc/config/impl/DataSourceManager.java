/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.jdbc.config.impl;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches for DataSource configs in OSGi configuration admin and creates / destroys the trackers
 * for the DataSourceFactories
 */
public class DataSourceManager implements ManagedServiceFactory {
    private Logger LOG = LoggerFactory.getLogger(DataSourcePublisher.class);
    private BundleContext context;
    
    /**
     * Stores one ServiceTracker for DataSourceFactories for each config pid
     */
    private Map<String, ServiceTracker> trackers;
    
    /**
     * Stores one publisher for each config pid
     */
    private Map<String, DataSourcePublisher> publishers;

    public DataSourceManager(BundleContext context) {
        this.context = context;
        this.trackers = new HashMap<String, ServiceTracker>();
        this.publishers = new HashMap<String, DataSourcePublisher>();
    }

    private String getFilter(String driverClass, String driverName) throws ConfigurationException {
        if (driverClass == null && driverName == null) {
            throw new ConfigurationException(null,
                "Could not determine driver to use. Specify either "
                    + DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + " or "
                    + DataSourceFactory.OSGI_JDBC_DRIVER_NAME);
        }
        List<String> filterList = new ArrayList<String>();
        filterList.add("objectClass=" + DataSourceFactory.class.getName());
        if (driverClass != null) {
            filterList.add(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=" + driverClass);
        }
        if (driverName != null) {
            filterList.add(DataSourceFactory.OSGI_JDBC_DRIVER_NAME + "=" + driverName);
        }
        String filter = andFilter(filterList);
        return filter;
    }

    private String andFilter(List<String> filterList) {
        StringBuilder filter = new StringBuilder();
        if (filterList.size() > 1) {
            filter.append("(&");
        }
        for (String filterPart : filterList) {
            filter.append("(" + filterPart + ")");
        }
        if (filterList.size() > 1) {
            filter.append(")");
        }
        return filter.toString();
    }

    @Override
    public String getName() {
        return "datasource";
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void updated(final String pid, final Dictionary config) throws ConfigurationException {
        deleted(pid);

        if (config == null) {
            return;
        }

        String driverClass = (String) config.get(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
        String driverName = (String) config.get(DataSourceFactory.OSGI_JDBC_DRIVER_NAME);
        String filter = getFilter(driverClass, driverName);
        try {
            final DataSourcePublisher publisher = createPublisher(config);
            ServiceTrackerCustomizer customizer = new DataSourceFactoryTracker(publisher);
            Filter filterO = context.createFilter(filter);
            ServiceTracker tracker = new ServiceTracker(context, filterO, customizer);
            tracker.open();
            trackers.put(pid, tracker);
            publishers.put(pid, publisher);
        }
        catch (InvalidSyntaxException e) {
            LOG.warn("Invalid filter for DataSource config from pid " + pid, e);
        }
    }

    @SuppressWarnings("rawtypes")
    protected DataSourcePublisher createPublisher(final Dictionary config) {
        return new DataSourcePublisher(context, config);
    }

    @Override
    public void deleted(String pid) {
        ServiceTracker tracker = trackers.get(pid);
        if (tracker != null) {
            tracker.close();
            trackers.remove(pid);
        }
        DataSourcePublisher publisher = publishers.get(pid);
        if (publisher != null) {
            publisher.unpublish();
        }
    }
    
    @SuppressWarnings("rawtypes")
    private final class DataSourceFactoryTracker implements ServiceTrackerCustomizer {

        private final DataSourcePublisher publisher;

        private DataSourceFactoryTracker(DataSourcePublisher publisher) {
            this.publisher = publisher;
        }

        @Override
        public void removedService(ServiceReference reference, Object service) {
            publisher.unpublish();
        }

        @Override
        public void modifiedService(ServiceReference reference, Object service) {
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object addingService(ServiceReference reference) {
            DataSourceFactory dsf = (DataSourceFactory) context.getService(reference);
            publisher.publish(dsf);
            context.ungetService(reference);
            return null;
        }
    }

}
