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
package org.ops4j.pax.jdbc.jtds.impl;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;

import net.sourceforge.jtds.jdbc.Driver;
import net.sourceforge.jtds.jdbcx.JtdsDataSource;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JTDSDataSourceFactoryConnectTest {

    public static final Logger LOG = LoggerFactory.getLogger(JTDSDataSourceFactoryConnectTest.class);

    /*
        $ podman run -itd --name pax.jdbc.sqlserver -e 'ACCEPT_EULA=Y' -e 'SA_PASSWORD=PaxJdbc!(*67' -e 'MSSQL_PID=Express' \
            -p 1433:1433 -d mcr.microsoft.com/mssql/server:2019-latest

        $ podman logs -f pax.jdbc.sqlserver
        SQL Server 2019 will run as non-root by default.
        This container is running as user mssql.
        ...
        2021-02-17 07:11:08.81 spid26s     Server is listening on [ 'any' <ipv4> 1433].
        ...
     */

    private static final String DB = "msdb";
    private static final String SERVER = "localhost";
    private static final String PORT = "1433";
    private static final String USER = "sa";
    private static final String PASSWORD = "PaxJdbc!(*67";

    @Rule
    public ExternalResource resource = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            try (Socket socket = new Socket()) {
                InetSocketAddress endpoint = new InetSocketAddress("localhost", 1433);
                socket.connect(endpoint, (int) TimeUnit.SECONDS.toMillis(5));
                Assume.assumeTrue(true);
            } catch (Exception ex) {
                Assume.assumeTrue(false);
            }
        }

        @Override
        protected void after() {
        }
    };

    @Test
    public void connectTest() throws ClassNotFoundException, SQLException {
        DataSourceFactory factory = new JTDSDataSourceFactory();
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_URL, "jdbc:jtds:sqlserver://localhost:1433/msdb");
        props.setProperty(DataSourceFactory.JDBC_USER, USER);
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, PASSWORD);
        DataSource ds = factory.createDataSource(props);
        try (Connection con = ds.getConnection()) {
            DatabaseMetaData md = con.getMetaData();
            LOG.info("DB: {}/{}", md.getDatabaseProductName(), md.getDatabaseProductVersion());

            try (Statement st = con.createStatement()) {
                try (ResultSet rs = st.executeQuery("select SCHEMA_NAME, SCHEMA_OWNER from INFORMATION_SCHEMA.SCHEMATA")) {
                    while (rs.next()) {
                        LOG.info("Schema: {}, owner: {}", rs.getString(1), rs.getString(2));
                    }
                }
            }
        }
    }

}
