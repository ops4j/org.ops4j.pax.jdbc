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
package org.ops4j.pax.jdbc.pool.common.impl;

import javax.transaction.TransactionManager;

import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTransactionManagerTracker<T> extends
    ServiceTracker<T, ServiceRegistration<PooledDataSourceFactory>> {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionManager.class);
    private ServiceReference<T> selectedService;

    public AbstractTransactionManagerTracker(BundleContext context, Class<T> clazz) throws InvalidSyntaxException {
        this(context, clazz, null);
    }

    public AbstractTransactionManagerTracker(BundleContext context, Class<T> clazz, String filter) throws InvalidSyntaxException {
        super(context,
              context.createFilter(filter == null
                      ? "(objectClass=" + clazz.getName() + ")"
                      : "(&(objectClass=" + clazz.getName() + ")" + filter + ")"), null);
    }

    @Override
    public ServiceRegistration<PooledDataSourceFactory> addingService(ServiceReference<T> reference) {
        synchronized (this) {
            if (selectedService != null) {
                LOG.warn("There is more than one TransactionManager service. Ignoring this one");
                return null;
            }
            selectedService = reference;
        }
        LOG.info("TransactionManager service detected. Providing support for XA DataSourceFactories");
        T tm = context.getService(reference);
        return createService(context, tm);
    }

    @Override
    public void modifiedService(ServiceReference<T> reference,
                                ServiceRegistration<PooledDataSourceFactory> sreg) {
        LOG.info("TransactionManager service modified");
    }

    @Override
    public void removedService(ServiceReference<T> reference,
                               ServiceRegistration<PooledDataSourceFactory> sreg) {
        synchronized (this) {
            if (selectedService == null || !selectedService.equals(reference)) {
                return;
            }
            selectedService = null;
        }
        
        LOG.info("TransactionManager service lost. Shutting down support for XA DataSourceFactories");
        sreg.unregister();
        context.ungetService(reference);
    }

    public abstract ServiceRegistration<PooledDataSourceFactory> createService(BundleContext context, T tm);
}
