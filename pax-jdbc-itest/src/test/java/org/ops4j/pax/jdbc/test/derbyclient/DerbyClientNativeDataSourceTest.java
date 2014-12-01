/*
 * Copyright 2012 Charlie Mordant.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.jdbc.test.derbyclient;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeThat;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.jdbc.test.TestConfiguration.regressionDefaults;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.jdbc.derbyclient.constants.ClientConnectionConstant;
import org.ops4j.pax.jdbc.test.ServerConfiguration;
import org.osgi.service.jdbc.DataSourceFactory;

@RunWith(PaxExam.class)
public class DerbyClientNativeDataSourceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Inject
    @Filter(value = "(osgi.jdbc.driver.name=derbyclient)")
    private DataSourceFactory dsf;
    private ServerConfiguration dbConfig = new ServerConfiguration("derbyclient");

    @Configuration
    public Option[] config() {
        return options(
            regressionDefaults(),
            mavenBundle("org.ops4j.pax.jdbc", "pax-jdbc"),
            mavenBundle("org.ops4j.pax.jdbc", "pax-jdbc-derbyclient").versionAsInProject(),
            mavenBundle("net.osgiliath.wrappers", "net.osgiliath.wrapper.derby")
                .versionAsInProject(),
            mavenBundle("org.osgi", "org.osgi.enterprise").versionAsInProject(),
            mavenBundle("commons-logging", "commons-logging").versionAsInProject(),
            mavenBundle("org.apache.geronimo.specs", "geronimo-jta_1.1_spec").versionAsInProject(),
            mavenBundle("org.apache.servicemix.bundles",
                "org.apache.servicemix.bundles.avalon-framework").versionAsInProject(),
            mavenBundle("log4j", "log4j").versionAsInProject()

        );
    }

    @Test
    public void createDataSourceAndConnection() throws SQLException, InterruptedException {

        assertNotNull(dsf);
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, dbConfig.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_USER, dbConfig.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, dbConfig.getPassword());
        props.setProperty(DataSourceFactory.JDBC_PORT_NUMBER, dbConfig.getPortNumber());
        props.setProperty(DataSourceFactory.JDBC_SERVER_NAME, dbConfig.getServerName());
        props
            .setProperty(ClientConnectionConstant.AUTO_START_SERVER, dbConfig.getServerAutoStart());
        props.setProperty(ClientConnectionConstant.CREATE_DATABASE, dbConfig.getCreateDatabase());
        DataSource dataSource = dsf.createDataSource(props);
        assertNotNull(dataSource);
        Connection connection = dataSource.getConnection();
        assertNotNull(connection);
        connection.close();
    }

    @Test
    public void connectWithDefaultPort() throws SQLException {

        assumeThat(dbConfig.getPortNumber(), is("1527"));

        assertNotNull(dsf);
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, dbConfig.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_USER, dbConfig.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, dbConfig.getPassword());
        props.setProperty(DataSourceFactory.JDBC_SERVER_NAME, dbConfig.getServerName());
        props
            .setProperty(ClientConnectionConstant.AUTO_START_SERVER, dbConfig.getServerAutoStart());
        props.setProperty(ClientConnectionConstant.CREATE_DATABASE, dbConfig.getCreateDatabase());
        DataSource dataSource = dsf.createDataSource(props);
        assertNotNull(dataSource);
        Connection connection = dataSource.getConnection();
        assertNotNull(connection);
        connection.close();
    }

    @Test
    public void connectWithDefaultHostAndPort() throws SQLException {

        assumeThat(dbConfig.getPortNumber(), is("1527"));
        assumeThat(dbConfig.getServerName(), is("localhost"));

        assertNotNull(dsf);
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, dbConfig.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_USER, dbConfig.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, dbConfig.getPassword());
        props
            .setProperty(ClientConnectionConstant.AUTO_START_SERVER, dbConfig.getServerAutoStart());
        props.setProperty(ClientConnectionConstant.CREATE_DATABASE, dbConfig.getCreateDatabase());
        DataSource dataSource = dsf.createDataSource(props);
        assertNotNull(dataSource);
        Connection connection = dataSource.getConnection();
        assertNotNull(connection);
        connection.close();
    }

}
