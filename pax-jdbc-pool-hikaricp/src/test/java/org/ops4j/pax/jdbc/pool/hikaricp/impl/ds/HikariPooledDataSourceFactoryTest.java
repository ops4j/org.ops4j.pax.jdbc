/*
 * Copyright 2021 OPS4J.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.jdbc.pool.hikaricp.impl.ds;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.Test;
import org.ops4j.pax.jdbc.pool.hikaricp.impl.HikariPooledDataSourceFactory;
import org.osgi.service.jdbc.DataSourceFactory;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HikariPooledDataSourceFactoryTest {

    @Test
    public void testcreateDataSource() throws SQLException {
        DataSourceFactory dataSourceFactory = mock(DataSourceFactory.class);
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);

        when(connection.isValid(anyInt())).thenReturn(true);
        when(connection.getAutoCommit()).thenReturn(true);
        when(connection.isReadOnly()).thenReturn(false);
        when(connection.getTransactionIsolation()).thenReturn(Connection.TRANSACTION_NONE);
        when(connection.getNetworkTimeout()).thenReturn(0);

        when(dataSource.getConnection()).thenReturn(connection);

        when(dataSourceFactory.createDataSource(any(Properties.class))).thenReturn(dataSource);

        HikariPooledDataSourceFactory pdsf = new HikariPooledDataSourceFactory();
        DataSource ds = pdsf.create(dataSourceFactory, createValidProps());

        verify(dataSource).setLoginTimeout(30);
        verify(connection).getNetworkTimeout();
        verify(connection, atLeastOnce()).setNetworkTimeout(any(), anyInt());

        assertEquals(HikariDataSource.class, ds.getClass());
        assertEquals(((HikariDataSource)ds).getMaximumPoolSize(), 8);

        try {
            pdsf.create(dataSourceFactory, createInvalidPoolConfig());
        } catch (RuntimeException e) {
            assertEquals(
                    "Property dummy does not exist on target class com.zaxxer.hikari.HikariConfig",
                    e.getMessage());
        }
    }

    private Properties createValidProps() {
        Properties props = new Properties();
        props.put("hikari.maximumPoolSize", "8");
        return props;
    }

    private Properties createInvalidPoolConfig() {
        Properties props = new Properties();
        props.put("hikari.dummy", "8");
        return props;
    }

}
