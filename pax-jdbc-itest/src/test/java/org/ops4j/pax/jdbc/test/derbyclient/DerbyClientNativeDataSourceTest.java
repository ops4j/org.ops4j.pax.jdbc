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
package org.ops4j.pax.jdbc.test.derbyclient;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.inject.Inject;

import org.apache.derby.drda.NetworkServerControl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.jdbc.derbyclient.constants.ClientConnectionConstant;
import org.ops4j.pax.jdbc.test.AbstractJdbcTest;
import org.ops4j.pax.jdbc.test.ServerConfiguration;
import org.osgi.service.jdbc.DataSourceFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assume.assumeThat;
import static org.ops4j.pax.exam.OptionUtils.combine;

@RunWith(PaxExam.class)
public class DerbyClientNativeDataSourceTest extends AbstractJdbcTest {

    @Rule
    public final ServerConfiguration dbConfig = new ServerConfiguration("derbyclient");

    @Inject
    @Filter(value = "(osgi.jdbc.driver.name=derbyclient)")
    private DataSourceFactory dsf;

    public void startDerbyServer() throws Exception {
        InetAddress addr = Inet4Address.getByName(dbConfig.getServerName());
        int port = dbConfig.getPortNumber();
        NetworkServerControl server = new NetworkServerControl(addr, port);
        server.start(null);
    }

    @Configuration
    public Option[] config() throws Exception {
        startDerbyServer();
        return combine(regressionDefaults(), //
                mvnBundle("org.apache.derby", "derbyclient"), //
                mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-derbyclient") //
        );
    }

    @Test
    public void createDataSourceAndConnection() throws SQLException, InterruptedException {
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, dbConfig.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_USER, dbConfig.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, dbConfig.getPassword());
        props.setProperty(DataSourceFactory.JDBC_PORT_NUMBER, dbConfig.getPortNumberSt());
        props.setProperty(DataSourceFactory.JDBC_SERVER_NAME, dbConfig.getServerName());
        props.setProperty(ClientConnectionConstant.CREATE_DATABASE, "create");
        try (Connection con = dsf.createDataSource(props).getConnection()) {
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
    }

    @Test
    public void connectWithDefaultPort() throws SQLException {
        assumeThat(dbConfig.getPortNumber(), is(1527));
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, dbConfig.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_USER, dbConfig.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, dbConfig.getPassword());
        props.setProperty(DataSourceFactory.JDBC_SERVER_NAME, dbConfig.getServerName());
        props.setProperty(ClientConnectionConstant.CREATE_DATABASE, "create");
        try (Connection con = dsf.createDataSource(props).getConnection()) {
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
    }

    @Test
    public void connectWithDefaultHostAndPort() throws SQLException {
        assumeThat(dbConfig.getPortNumber(), is(1527));
        assumeThat(dbConfig.getServerName(), is("localhost"));
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, dbConfig.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_USER, dbConfig.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, dbConfig.getPassword());
        props.setProperty(ClientConnectionConstant.CREATE_DATABASE, "create");
        try (Connection con = dsf.createDataSource(props).getConnection()) {
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
    }

}
