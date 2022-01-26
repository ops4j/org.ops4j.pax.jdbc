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
package org.ops4j.pax.jdbc.db2.impl;

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

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectTest {

    public static final Logger LOG = LoggerFactory.getLogger(ConnectTest.class);

    /*
        $ mkdir -p /data/tmp/pax.jdbc.db2
        $ podman run -itd --name pax.jdbc.db2 --privileged=true \
            -p 50000:50000 -e LICENSE=accept \
            -e DB2INST1_PASSWORD=paxjdbc -e DBNAME=paxjdbc \
            -v /data/tmp/pax.jdbc.db2:/database ibmcom/db2

        $ podman ps
        CONTAINER ID  IMAGE                        COMMAND  CREATED         STATUS             PORTS                     NAMES
        cfe51e650164  docker.io/ibmcom/db2:latest           13 seconds ago  Up 13 seconds ago  0.0.0.0:50000->50000/tcp  pax.jdbc.db2

        $ podman logs -f pax.jdbc.db2
        (*) Previous setup has not been detected. Creating the users...
        ...
        SQL1063N  DB2START processing was successful.
        (*) User chose to create paxjdbc database
        (*) Creating database paxjdbc ...
        ...
        DB20000I  The UPDATE DATABASE CONFIGURATION command completed successfully.
        ...
        (*) Setup has completed.
     */

    @Rule
    public ExternalResource resource = new ExternalResource() {
        @Override
        protected void before() {
            try (Socket socket = new Socket()) {
                InetSocketAddress endpoint = new InetSocketAddress("localhost", 50000);
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
        DataSourceFactory factory = new DB2DataSourceFactory();
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_URL, "jdbc:db2://localhost:50000/paxjdbc");
        props.setProperty(DataSourceFactory.JDBC_USER, "db2inst1");
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, "paxjdbc");
        DataSource ds = factory.createDataSource(props);
        try (Connection con = ds.getConnection()) {
            DatabaseMetaData md = con.getMetaData();
            LOG.info("DB: {}/{}", md.getDatabaseProductName(), md.getDatabaseProductVersion());

            try (Statement st = con.createStatement()) {
                try (ResultSet rs = st.executeQuery("select SCHEMANAME, OWNER from SYSCAT.SCHEMATA")) {
                    while (rs.next()) {
                        LOG.info("Schema: {}, owner: {}", rs.getString(1), rs.getString(2));
                    }
                }
            }
        }
    }

    @Test
    public void currentSchemaConnectTest() throws ClassNotFoundException, SQLException {
        DataSourceFactory factory = new DB2DataSourceFactory();
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_URL, "jdbc:db2://localhost:50000/paxjdbc:currentSchema=SYSCAT;password=paxjdbc;");
        props.setProperty(DataSourceFactory.JDBC_USER, "db2inst1");
        DataSource ds = factory.createDataSource(props);
        try (Connection con = ds.getConnection()) {
            DatabaseMetaData md = con.getMetaData();
            LOG.info("DB: {}/{}", md.getDatabaseProductName(), md.getDatabaseProductVersion());

            try (Statement st = con.createStatement()) {
                // see - "SCHEMATA" without "SYSCAT."
                try (ResultSet rs = st.executeQuery("select SCHEMANAME, OWNER from SCHEMATA")) {
                    while (rs.next()) {
                        LOG.info("Schema: {}, owner: {}", rs.getString(1), rs.getString(2));
                    }
                }
            }
        }
    }

}
