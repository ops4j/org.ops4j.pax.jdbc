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
package org.ops4j.pax.jdbc.derbyclient.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;

import org.apache.derby.jdbc.ClientDataSource;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.ops4j.pax.jdbc.derbyclient.constants.ClientConnectionConstant;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class TestDsf {

    public static final Logger LOG = LoggerFactory.getLogger(TestDsf.class);

    /*
        Download db-derby-10.14.2.0-bin.zip and unzip it (it'll be $DERBY_HOME)

        $ export DERBY_HOME=/data/db/db-derby-10.14.2.0-bin
        $ cd $DERBY_HOME
        $ mkdir databases; cd databases
        $ java -jar $DERBY_HOME/lib/derbyrun.jar server start
     */

    @Rule
    public ExternalResource resource = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            try (Socket socket = new Socket()) {
                InetSocketAddress endpoint = new InetSocketAddress("localhost", 1527);
                socket.connect(endpoint, (int) TimeUnit.SECONDS.toMillis(5));
                Assume.assumeTrue("Derby DB should start and listen on port 1527", true);
            } catch (Exception ex) {
                Assume.assumeTrue("Derby DB should start and listen on port 1527", false);
            }
        }

        @Override
        protected void after() {
        }
    };

    @Test
    public void testPropertyBased() throws ClassNotFoundException, SQLException {
        DataSourceFactory factory = new DerbyClientDatasourceFactory();
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, "target/test1");
        props.setProperty(DataSourceFactory.JDBC_USER, "sa");
        props.put(ClientConnectionConstant.CREATE_DATABASE, "create");
        DataSource ds = factory.createDataSource(props);
        try (Connection con = ds.getConnection()) {
            DatabaseMetaData md = con.getMetaData();
            LOG.info("DB: {}/{}", md.getDatabaseProductName(), md.getDatabaseProductVersion());

            try (Statement st = con.createStatement()) {
                try (ResultSet rs = st.executeQuery("select SCHEMAID, SCHEMANAME, AUTHORIZATIONID from SYS.SYSSCHEMAS")) {
                    while (rs.next()) {
                        LOG.info("Schema: {}/{}, owner: {}", rs.getString(1), rs.getString(2), rs.getString(3));
                    }
                }
            }
        }
        ClientDataSource eds = (ClientDataSource) ds;
        assertEquals("target/test1", eds.getDatabaseName());
        assertEquals("create", eds.getCreateDatabase());
    }

    @Test
    public void testUrlBased() throws SQLException, IOException {
        DataSourceFactory dsf = new DerbyClientDatasourceFactory();
        Properties props = new Properties();
        props.put(DataSourceFactory.JDBC_URL, "jdbc:derby://localhost:1527/target/test;create=true");
        props.setProperty(DataSourceFactory.JDBC_USER, "sa");
        DataSource ds = dsf.createDataSource(props);
        try (Connection con = ds.getConnection()) {
            DatabaseMetaData md = con.getMetaData();
            LOG.info("DB: {}/{}", md.getDatabaseProductName(), md.getDatabaseProductVersion());

            try (Statement st = con.createStatement()) {
                try (ResultSet rs = st.executeQuery("select SCHEMAID, SCHEMANAME, AUTHORIZATIONID from SYS.SYSSCHEMAS")) {
                    while (rs.next()) {
                        LOG.info("Schema: {}/{}, owner: {}", rs.getString(1), rs.getString(2), rs.getString(3));
                    }
                }
            }
        }
        ClientDataSource eds = (ClientDataSource) ds;
        assertEquals("target/test", eds.getDatabaseName());
        assertEquals("localhost", eds.getServerName());
        assertEquals(1527, eds.getPortNumber());
        assertEquals("create=true", eds.getConnectionAttributes());
    }

}
