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
package org.ops4j.pax.jdbc.test.mariadb;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.inject.Inject;
import javax.sql.DataSource;

import org.junit.Rule;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.jdbc.test.AbstractJdbcTest;
import org.ops4j.pax.jdbc.test.ServerConfiguration;
import org.osgi.service.jdbc.DataSourceFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;
import static org.ops4j.pax.exam.OptionUtils.combine;

public class MariaDbNativeDataSourceTest extends AbstractJdbcTest {

    @Rule
    public ServerConfiguration dbConfig = new ServerConfiguration("mariadb");

    @Inject
    @Filter(value = "(osgi.jdbc.driver.name=mariadb)", timeout = 1000000)
    private DataSourceFactory dsf;

    @Configuration
    public Option[] config() {
        return combine(regressionDefaults(), //
                mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-mariadb"), //
                mvnBundle("org.mariadb.jdbc", "mariadb-java-client"));
    }

    @Test
    public void createDataSourceAndConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_SERVER_NAME, dbConfig.getServerName());
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, dbConfig.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_PORT_NUMBER, dbConfig.getPortNumberSt());
        props.setProperty(DataSourceFactory.JDBC_USER, dbConfig.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, dbConfig.getPassword());
        try (Connection con = dsf.createDataSource(props).getConnection()) {
            DatabaseMetaData md = con.getMetaData();
            LOG.info("DB: {}/{}", md.getDatabaseProductName(), md.getDatabaseProductVersion());

            try (Statement st = con.createStatement()) {
                try (ResultSet rs = st.executeQuery("select SCHEMA_NAME, CATALOG_NAME from INFORMATION_SCHEMA.SCHEMATA")) {
                    while (rs.next()) {
                        LOG.info("Schema: {}, catalog: {}", rs.getString(1), rs.getString(2));
                    }
                }
            }
        }
    }

    @Test
    public void connectWithDefaultPort() throws SQLException {
        assumeThat(dbConfig.getPortNumber(), is(3306));

        assertNotNull(dsf);
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_SERVER_NAME, dbConfig.getServerName());
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, dbConfig.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_USER, dbConfig.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, dbConfig.getPassword());
        try (Connection con = dsf.createDataSource(props).getConnection()) {
            DatabaseMetaData md = con.getMetaData();
            LOG.info("DB: {}/{}", md.getDatabaseProductName(), md.getDatabaseProductVersion());

            try (Statement st = con.createStatement()) {
                try (ResultSet rs = st.executeQuery("select SCHEMA_NAME, CATALOG_NAME from INFORMATION_SCHEMA.SCHEMATA")) {
                    while (rs.next()) {
                        LOG.info("Schema: {}, catalog: {}", rs.getString(1), rs.getString(2));
                    }
                }
            }
        }
    }

    @Test
    public void connectWithDefaultHostAndPort() throws SQLException {
        assumeThat(dbConfig.getPortNumber(), is(3306));
        assumeThat(dbConfig.getServerName(), is("localhost"));

        assertNotNull(dsf);
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, dbConfig.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_USER, dbConfig.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, dbConfig.getPassword());
        try (Connection con = dsf.createDataSource(props).getConnection()) {
            DatabaseMetaData md = con.getMetaData();
            LOG.info("DB: {}/{}", md.getDatabaseProductName(), md.getDatabaseProductVersion());

            try (Statement st = con.createStatement()) {
                try (ResultSet rs = st.executeQuery("select SCHEMA_NAME, CATALOG_NAME from INFORMATION_SCHEMA.SCHEMATA")) {
                    while (rs.next()) {
                        LOG.info("Schema: {}, catalog: {}", rs.getString(1), rs.getString(2));
                    }
                }
            }
        }
    }

    @Test
    public void failOnMissingPassword() throws SQLException {
        assertNotNull(dsf);
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_SERVER_NAME, dbConfig.getServerName());
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, dbConfig.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_PORT_NUMBER, dbConfig.getPortNumberSt());
        props.setProperty(DataSourceFactory.JDBC_USER, dbConfig.getUser());
        DataSource dataSource = dsf.createDataSource(props);
        try {
            dataSource.getConnection();
            fail();
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("Access denied") && e.getMessage().contains("using password: NO"));
        }
    }

    @Test
    public void failOnWrongPassword() throws SQLException {
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_SERVER_NAME, dbConfig.getServerName());
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, dbConfig.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_PORT_NUMBER, dbConfig.getPortNumberSt());
        props.setProperty(DataSourceFactory.JDBC_USER, dbConfig.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, "wrong");
        DataSource dataSource = dsf.createDataSource(props);
        try {
            dataSource.getConnection();
            fail();
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("Access denied") && e.getMessage().contains("using password: YES"));
        }
    }

}
