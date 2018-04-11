/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ops4j.pax.jdbc.config.impl;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.sql.CommonDataSource;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

    private static final String FACTORY_PID = "org.ops4j.datasource";

    private ServiceTracker<?, ?> dataSourceTracker;

    @Override
    public void start(BundleContext context) throws Exception {
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put(Constants.SERVICE_PID, FACTORY_PID);
        DataSourceConfigManager configManager = new DataSourceConfigManager(context);
        // this service will track:
        //  - org.ops4j.datasource factory PIDs
        //  - (optionally) org.jasypt.encryption.StringEncryptor services
        //  - (optionally) org.ops4j.pax.jdbc.hook.PreHook services
        //  - org.osgi.service.jdbc.DataSourceFactory services
        context.registerService(ManagedServiceFactory.class.getName(), configManager, props);

        // this service will track:
        //  - javax.sql.DataSource services
        //  - javax.sql.XADataSource services
        // and when they're registered:
        //  - with "pool=<pool name>"
        //  - without "pax.jdbc.managed=true"
        // they'll be processed by selected org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory
        // (as with org.ops4j.datasource factory PIDs)
        ServiceTrackerHelper helper = ServiceTrackerHelper.helper(context);
        String filter = "(&(pool=*)(!(pax.jdbc.managed=true))" +
                "(|(objectClass=javax.sql.DataSource)(objectClass=javax.sql.XADataSource)))";
        dataSourceTracker = helper.track(CommonDataSource.class, filter,
                (ds, reference) -> new DataSourceWrapper(context, ds, reference),
                DataSourceWrapper::close
        );
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (dataSourceTracker != null) {
            dataSourceTracker.close();
        }
    }

}
