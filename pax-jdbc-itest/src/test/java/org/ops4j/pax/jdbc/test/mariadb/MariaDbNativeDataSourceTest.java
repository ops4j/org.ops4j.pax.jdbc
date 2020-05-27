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
package org.ops4j.pax.jdbc.test.mariadb;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.jdbc.test.AbstractJdbcTest;
import org.ops4j.pax.jdbc.test.ServerConfiguration;
import org.osgi.service.jdbc.DataSourceFactory;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeThat;
import static org.ops4j.pax.exam.CoreOptions.options;

public class MariaDbNativeDataSourceTest extends AbstractJdbcTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public ServerConfiguration dbConfig = new ServerConfiguration("mariadb");

    @Inject
    @Filter(value = "(osgi.jdbc.driver.name=mariadb)", timeout = 1000000)
    private DataSourceFactory dsf;

    @Configuration
    public Option[] config() {
        return options(regressionDefaults(), //
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
        dsf.createDataSource(props).getConnection().close();
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
        dsf.createDataSource(props).getConnection().close();
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
        dsf.createDataSource(props).getConnection().close();
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
        thrown.expect(SQLException.class);
        thrown.expectMessage("Access denied");
        thrown.expectMessage("using password: NO");
        dataSource.getConnection();
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
        thrown.expect(SQLException.class);
        thrown.expectMessage("Access denied");
        thrown.expectMessage("using password: YES");
        dataSource.getConnection();
    }
}
