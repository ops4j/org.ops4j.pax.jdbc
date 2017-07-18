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
package org.ops4j.pax.jdbc.pool.transx.impl;

import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.ops4j.pax.jdbc.pool.common.impl.AbstractTransactionManagerTracker;
import org.ops4j.pax.transx.tm.TransactionManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory.POOL_KEY;
import static org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory.XA_KEY;

/**
 * Manage DataSourceFactory tracker
 */
public class Activator implements BundleActivator {

    private static final String TRANSX = "transx";
    private ServiceTracker<TransactionManager, ServiceRegistration<PooledDataSourceFactory>> tmTracker;

    @Override
    public void start(final BundleContext context) throws Exception {
        TransxPooledDataSourceFactory dsf = new TransxPooledDataSourceFactory();
        Dictionary<String, String> props = new Hashtable<>();
        props.put(POOL_KEY, TRANSX);
        context.registerService(PooledDataSourceFactory.class, dsf, props);
        tmTracker = new AbstractTransactionManagerTracker<TransactionManager>(context, TransactionManager.class) {
            @Override
            public ServiceRegistration<PooledDataSourceFactory> createService(BundleContext context, TransactionManager tm) {
                TransxXaPooledDataSourceFactory dsf = new TransxXaPooledDataSourceFactory(tm);
                Dictionary<String, String> props = new Hashtable<>();
                props.put(POOL_KEY, TRANSX);
                props.put(XA_KEY, "true");
                return context.registerService(PooledDataSourceFactory.class, dsf, props);
            }
        };
        tmTracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        tmTracker.close();
    }
}
