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
package org.ops4j.pax.jdbc.oracle.impl;

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
        https://blogs.oracle.com/oraclemagazine/deliver-oracle-database-18c-express-edition-in-containers

        $ git clone https://github.com/oracle/docker-images.git
        $ cd docker-images/OracleDatabase/SingleInstance/dockerfiles
        $ ./buildContainerImage.sh -v 18.4.0 -x
        uilding image 'oracle/database:18.4.0-xe' ...
        Sending build context to Docker daemon  18.43kB
        Step 1/8 : FROM oraclelinux:7-slim
        ...
        emoving intermediate container d92f713cf17c
        OCI runtime create failed: this version of runc doesn't work on cgroups v2: unknown
     */

    /*
        A bit unofficial
        $ podman run -itd --name pax.jdbc.oracle -p 1521:1521 --privileged=true oracleinanutshell/oracle-xe-11g
     */

    @Rule
    public ExternalResource resource = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            try (Socket socket = new Socket()) {
                InetSocketAddress endpoint = new InetSocketAddress("localhost", 1521);
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
        DataSourceFactory factory = new OracleDataSourceFactory();
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_URL, "jdbc:oracle:thin:@localhost:1521:xe");
        props.setProperty(DataSourceFactory.JDBC_USER, "system");
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, "oracle");
        DataSource ds = factory.createDataSource(props);
        try (Connection con = ds.getConnection()) {
            DatabaseMetaData md = con.getMetaData();
            LOG.info("DB: {}/{}", md.getDatabaseProductName(), md.getDatabaseProductVersion());

            try (Statement st = con.createStatement()) {
                try (ResultSet rs = st.executeQuery("select TABLE_NAME from USER_TABLES")) {
                    while (rs.next()) {
                        LOG.info("Table: {}", rs.getString(1));
                    }
                }
            }
        }
    }

}
