/*
 * Copyright 2013 Harald Wellmann.
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
package org.ops4j.pax.jdbc.test.mariadb;

import static org.junit.Assert.assertNotNull;
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
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.jdbc.test.ServerConfiguration;
import org.osgi.service.jdbc.DataSourceFactory;

@RunWith(PaxExam.class)
public class MariaDbDataSourceTest {

    @Rule
    public MariaDbRule mariadb = new MariaDbRule();

    @Inject
    @Filter("(osgi.jdbc.driver.class=org.mariadb.jdbc.Driver)")
    private DataSourceFactory dsf;

    @Configuration
    public Option[] config() {
        return options(regressionDefaults(), mavenBundle("org.ops4j.pax.jdbc", "pax-jdbc")
            .versionAsInProject(),
            mavenBundle("org.jumpmind.symmetric.jdbc", "mariadb-java-client").versionAsInProject(),
            mavenBundle("org.osgi", "org.osgi.enterprise").versionAsInProject());
    }

    @Test
    public void createDataSourceAndConnection() throws SQLException {
        assertNotNull(dsf);
        ServerConfiguration config = new ServerConfiguration("mariadb");

        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_URL, config.getUrl());
        props.setProperty(DataSourceFactory.JDBC_USER, config.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, config.getPassword());
        DataSource dataSource = dsf.createDataSource(props);
        assertNotNull(dataSource);
        Connection connection = dataSource.getConnection();
        assertNotNull(connection);
        connection.close();
    }
}
