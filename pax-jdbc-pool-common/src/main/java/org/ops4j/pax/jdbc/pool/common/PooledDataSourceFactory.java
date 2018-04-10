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
package org.ops4j.pax.jdbc.pool.common;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.osgi.service.jdbc.DataSourceFactory;

/**
 * PAX-JDBC specific extension to standard OSGi/JDBC concept of {@link DataSourceFactory}.
 */
public interface PooledDataSourceFactory {

    /**
     * A logical name (key) of registered {@code PooledDataSourceFactory}
     */
    String POOL_KEY = "pool";

    /**
     * A boolean flag indicating whether the registered {@code PooledDataSourceFactory} is or is not XA-Aware.
     */
    String XA_KEY = "xa";

    /**
     * Method similar to {@link DataSourceFactory} factory methods.
     * It creates pooled {@link DataSource} using OSGi JDBC standard {@link DataSourceFactory}.
     * @param dsf existing {@link DataSourceFactory} that can be used to create {@link DataSource}, {@link javax.sql.XADataSource}
     * or {@link javax.sql.ConnectionPoolDataSource} depending on configuration properties
     * @param config pooling and connection factory configuration
     * @return poolable {@link DataSource}
     * @throws SQLException
     */
    DataSource create(DataSourceFactory dsf, Properties config) throws SQLException;

}
