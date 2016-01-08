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
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches for DataSource configs in OSGi configuration admin and creates / destroys the trackers
 * for the DataSourceFactories
 */
public class DataSourceConfigManager implements ManagedServiceFactory {


    private Logger LOG = LoggerFactory.getLogger(DataSourceRegistration.class);
    BundleContext context;

    /**
     * Stores one ServiceTracker for DataSourceFactories for each config pid
     */
    private Map<String, ServiceTracker> trackers;
    private Decryptor decryptor;


    public DataSourceConfigManager(BundleContext context, Decryptor decryptor) {
        this.context = context;
        this.trackers = new HashMap<String, ServiceTracker>();
        this.decryptor = decryptor;
    }

    @Override
    public String getName() {
        return "datasource";
    }

    @SuppressWarnings({ "rawtypes"})
    @Override
    public void updated(final String pid, final Dictionary config) throws ConfigurationException {
        deleted(pid);

        if (config == null) {
            return;
        }

        decryptor.decrypt(config);

        try {
            String filter = getFilter(config);
            Filter filterO = context.createFilter(filter);
            DataSourceFactoryTracker tracker = new DataSourceFactoryTracker(context, filterO, config);
            tracker.open();
            trackers.put(pid, tracker);
        }
        catch (InvalidSyntaxException e) {
            LOG.warn("Invalid filter for DataSource config from pid " + pid, e);
        }
    }



    @SuppressWarnings("rawtypes")
    private String getFilter(Dictionary config) throws ConfigurationException {
        String driverClass = (String) config.get(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS);
        String driverName = (String) config.get(DataSourceFactory.OSGI_JDBC_DRIVER_NAME);
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
    public void deleted(String pid) {
        ServiceTracker tracker = trackers.get(pid);
        if (tracker != null) {
            tracker.close();
            trackers.remove(pid);
        }
    }
    

}
