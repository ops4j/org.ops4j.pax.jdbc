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
package org.ops4j.pax.jdbc.test.mysql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.jdbc.test.AbstractJdbcTest;
import org.ops4j.pax.jdbc.test.ServerConfiguration;
import org.osgi.service.jdbc.DataSourceFactory;

import static org.ops4j.pax.exam.OptionUtils.combine;

public class MysqlDataSourceTest extends AbstractJdbcTest {

    @Rule
    public ServerConfiguration config = new ServerConfiguration("mysql");

    @Inject
    @Filter("(osgi.jdbc.driver.class=com.mysql.cj.jdbc.Driver)")
    private DataSourceFactory dsf;

    @Configuration
    public Option[] config() {
        return combine(regressionDefaults(), //
                mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc"), //
                mvnBundle("com.mysql", "mysql-connector-j") //
        );
    }

    @Test
    public void createDataSourceAndConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_URL, config.getUrl());
        props.setProperty(DataSourceFactory.JDBC_USER, config.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, config.getPassword());
        try (Connection con = dsf.createDataSource(props).getConnection()) {
            DatabaseMetaData md = con.getMetaData();
            LOG.info("DB: {}/{}", md.getDatabaseProductName(), md.getDatabaseProductVersion());

            try (Statement st = con.createStatement()) {
                try (ResultSet rs = st.executeQuery("select SCHEMA_NAME, CATALOG_NAME from INFORMATION_SCHEMA.SCHEMATA t")) {
                    while (rs.next()) {
                        LOG.info("Schema: {}, catalog: {}", rs.getString(1), rs.getString(2));
                    }
                }
            }
        }
    }

}
