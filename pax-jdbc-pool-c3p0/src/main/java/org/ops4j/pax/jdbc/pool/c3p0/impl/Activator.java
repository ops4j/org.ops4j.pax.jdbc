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
package org.ops4j.pax.jdbc.pool.c3p0.impl;

import javax.transaction.TransactionManager;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Manage DataSourceFactory tracker
 */
public class Activator implements BundleActivator {

    private ServiceTracker<DataSourceFactory, ServiceRegistration<DataSourceFactory>> dsfTracker;
    private ServiceTracker<TransactionManager, ServiceTracker> tmTracker;

    @Override
    public void start(BundleContext context) throws Exception {
        dsfTracker = new DataSourceFactoryTracker(context);
        dsfTracker.open();

        tmTracker = new TransactionManagerTracker(context);
        tmTracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        dsfTracker.close();
        tmTracker.close();
    }

}
