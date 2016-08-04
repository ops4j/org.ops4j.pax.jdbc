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
package org.ops4j.pax.jdbc.pool.dbcp2.impl;

import static org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory.POOL_KEY;
import static org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory.XA_KEY;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.transaction.TransactionManager;

import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.ops4j.pax.jdbc.pool.common.impl.AbstractTransactionManagerTracker;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    private AbstractTransactionManagerTracker tmTracker;

    @Override
    public void start(final BundleContext context) throws Exception {
        DbcpPooledDataSourceFactory dsf = new DbcpPooledDataSourceFactory();
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put(POOL_KEY, "dbcp2");
        context.registerService(PooledDataSourceFactory.class, dsf, props);

        tmTracker = new AbstractTransactionManagerTracker(context) {
            
            @Override
            public ServiceRegistration<PooledDataSourceFactory> createService(BundleContext context,
                                                                              TransactionManager tm) {
                DbcpXAPooledDataSourceFactory dsf = new DbcpXAPooledDataSourceFactory(tm);
                Dictionary<String, String> props = new Hashtable<String, String>();
                props.put(POOL_KEY, "dbcp2");
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
