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
package org.ops4j.pax.jdbc.pool.aries.impl.ds;

import java.util.Collection;
import java.util.HashSet;
import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import org.apache.aries.transaction.AriesTransactionManager;
import org.apache.aries.transaction.jdbc.RecoverableDataSource;
import org.ops4j.pax.jdbc.pool.common.impl.ds.XAPooledDataSourceFactory;
import org.osgi.service.jdbc.DataSourceFactory;

/**
 * Creates pooled and optionally XA ready DataSources out of a non pooled DataSourceFactory.
 * 
 * XA transaction handling Besides pooling this also supports to provide a DataSource that wraps a
 * XADataSource and handles the XA Resources. This kind of DataSource can then for example be used
 * in persistence.xml as jta-data-source
 */
public class AriesXaPooledDataSourceFactory extends XAPooledDataSourceFactory {

   
    /**
     * Initialize XA PoolingDataSourceFactory
     * 
     * @param dsFactory
     *            non pooled DataSourceFactory we delegate to
     * @param tm
     *            transaction manager (Only needed for XA mode)
     */
    public AriesXaPooledDataSourceFactory(DataSourceFactory dsFactory, AriesTransactionManager tm) {
      super(dsFactory, tm);
    }
    @Override
    protected Iterable<Object> internalCreateDatasource(Object ds) {
      RecoverableDataSource mds = new RecoverableDataSource();
      mds.setDataSource((CommonDataSource) ds);
      mds.setTransactionManager((AriesTransactionManager) tm);
      Collection<Object> ret = new HashSet<Object>();
      ret.add(mds);
      return ret;
    }


    @Override
    protected DataSource doStart(Iterable<Object> mds) throws Exception {
      RecoverableDataSource ds = (RecoverableDataSource) mds.iterator().next();
      ds.start();
      return ds;
      
    }
   

}
