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

import org.ops4j.pax.jdbc.pool.c3p0.impl.ds.C3p0PooledDataSourceFactory;
import org.ops4j.pax.jdbc.pool.c3p0.impl.ds.C3p0XAPooledDataSourceFactory;
import org.ops4j.pax.jdbc.pool.common.impl.AbstractDataSourceFactoryTracker;
import org.osgi.framework.BundleContext;
import org.osgi.service.jdbc.DataSourceFactory;

/**
 * Watches for DataSourceFactory services and creates/destroys a DbcpPooledDataSourceFactory for
 * each existing DataSourceFactory
 */
public class DataSourceFactoryTracker extends AbstractDataSourceFactoryTracker {

    public DataSourceFactoryTracker(BundleContext context) {
        this(context, null);
    }

    public DataSourceFactoryTracker(BundleContext context, TransactionManager tm) {
        super(context, tm);
    }

    @Override
    protected DataSourceFactory createPooledDatasourceFactory(DataSourceFactory dsf) {
        if (getTransactionManager() != null) {
            return new C3p0XAPooledDataSourceFactory(dsf, tm);
        }
        else {
            return new C3p0PooledDataSourceFactory(dsf);
        }
    }

}
