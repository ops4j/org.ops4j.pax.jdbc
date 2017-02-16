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

import java.util.Dictionary;

import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;

@SuppressWarnings({"rawtypes", "unchecked"})
final class DataSourceFactoryTracker extends ServiceTracker {
    private Dictionary config;
    private Dictionary decryptedConfig;
    private PooledDataSourceFactory pdsf;

    DataSourceFactoryTracker(BundleContext context, PooledDataSourceFactory pdsf, Filter dsfFilter, Dictionary config, Dictionary decryptedConfig) {
        super(context, dsfFilter, null);
        this.pdsf = pdsf;
        this.config = config;
        this.decryptedConfig = decryptedConfig;
    }
    
    @Override
    public Object addingService(ServiceReference reference) {
        DataSourceFactory dsf = (DataSourceFactory) context.getService(reference);
        DataSourceFactory wrappedDsf = (pdsf == null) ? dsf : new PoolingWrapper(pdsf, dsf);
        return new DataSourceRegistration(context, wrappedDsf, config, decryptedConfig);
    }

    @Override
    public void removedService(ServiceReference reference, Object reg) {
        ((DataSourceRegistration)reg).close();
        super.removedService(reference, reg);
    }

}
