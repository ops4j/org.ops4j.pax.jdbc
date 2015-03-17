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

import org.ops4j.pax.jdbc.pool.common.impl.AbstractDataSourceFactoryTracker;

import java.util.Dictionary;
import javax.transaction.TransactionManager;
import org.ops4j.pax.jdbc.pool.dbcp2.impl.ds.DbcpPooledDataSourceFactory;
import org.ops4j.pax.jdbc.pool.dbcp2.impl.ds.DbcpXAPooledDataSourceFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches for DataSourceFactory services and creates/destroys a DbcpPooledDataSourceFactory for each
 * existing DataSourceFactory
 */
@SuppressWarnings("rawtypes")
public class DataSourceFactoryTracker extends AbstractDataSourceFactoryTracker {
   
    public DataSourceFactoryTracker(BundleContext context) {
        this(context, null);
    }

    public DataSourceFactoryTracker(BundleContext context, TransactionManager tm) {
    	super(context, tm);
    }

  @Override
  protected DataSourceFactory createPooledDatasourceFactory(
      DataSourceFactory dsf) {
    if (null == getTransactionManager()) {
      return new DbcpXAPooledDataSourceFactory(dsf, tm);
    } else {
      return new DbcpPooledDataSourceFactory(dsf);
    }
   
  }
    
}
