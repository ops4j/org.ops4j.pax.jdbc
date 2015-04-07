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

import org.ops4j.pax.jdbc.pool.aries.impl.ds.AriesXaPooledDataSourceFactory;

import javax.transaction.TransactionManager;
import org.ops4j.pax.jdbc.pool.common.impl.AbstractDataSourceFactoryTracker;
import java.util.Dictionary;
import java.util.Hashtable;
import org.apache.aries.transaction.AriesTransactionManager;
import org.ops4j.pax.jdbc.pool.aries.impl.ds.AriesPooledDataSourceFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches for DataSourceFactory services and creates/destroys a AriesPooledDataSourceFactory for
 * each existing DataSourceFactory
 */
public class AriesDataSourceFactoryTracker extends AbstractDataSourceFactoryTracker {

    public AriesDataSourceFactoryTracker(BundleContext context) {
        super(context);

    }

    public AriesDataSourceFactoryTracker(BundleContext context, TransactionManager tm) {
        super(context, tm);

    }

    @Override
    protected DataSourceFactory createPooledDatasourceFactory(DataSourceFactory dsf) {
        if (null != getTransactionManager()) {
            return new AriesXaPooledDataSourceFactory(dsf,
                (AriesTransactionManager) getTransactionManager());
        }
        else {
            return new AriesPooledDataSourceFactory(dsf);
        }
    }

}
