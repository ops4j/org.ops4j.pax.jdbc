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
package org.ops4j.pax.jdbc.pool.aries.impl;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.apache.aries.transaction.AriesTransactionManager;
import org.apache.aries.transaction.jdbc.RecoverableDataSource;
import org.ops4j.pax.jdbc.pool.common.impl.BeanConfig;
import org.osgi.service.jdbc.DataSourceFactory;

/**
 * Creates pooled and optionally XA ready DataSources out of a non pooled DataSourceFactory.
 * 
 * XA transaction handling Besides pooling this also supports to provide a DataSource that wraps a
 * XADataSource and handles the XA Resources. This kind of DataSource can then for example be used
 * in persistence.xml as jta-data-source
 */
public class AriesXaPooledDataSourceFactory extends AriesPooledDataSourceFactory {
    private AriesTransactionManager tm;

    /**
     * Initialize XA PoolingDataSourceFactory
     * 
     * @param tm
     *            transaction manager (Only needed for XA mode)
     */
    public AriesXaPooledDataSourceFactory(AriesTransactionManager tm) {
        this.tm = tm;
    }

    @Override
    public DataSource create(DataSourceFactory dsf, Properties props) throws SQLException {
        try {
            XADataSource ds = dsf.createXADataSource(getNonPoolProps(props));
            RecoverableDataSource mds = new RecoverableDataSource();
            mds.setUsername(props.getProperty(DataSourceFactory.JDBC_USER));
            mds.setPassword(props.getProperty(DataSourceFactory.JDBC_PASSWORD));
            mds.setDataSource((CommonDataSource) ds);
            mds.setTransactionManager((AriesTransactionManager) tm);
            BeanConfig.configure(mds, getPoolProps(props));
            mds.start();
            return mds;
        }
        catch (Throwable e) {
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            else {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

}
