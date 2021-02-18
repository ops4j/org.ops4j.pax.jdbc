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
package org.ops4j.pax.jdbc.test.oracle;

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

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

public class OracleNativeDataSourceTest extends AbstractJdbcTest {

    @Rule
    public ServerConfiguration config = new ServerConfiguration("oracle");

    @Inject
    @Filter("(osgi.jdbc.driver.name=oracle)")
    private DataSourceFactory dsf;

    @Configuration
    public Option[] config() {
        return combine(regressionDefaults(), //
                mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-oracle"), //
                bundle("wrap:mvn:com.oracle.database.jdbc/ojdbc8/" + System.getProperty("oracle.jdbc.version"))
        );
    }

    @Test
    public void createDataSourceAndConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_URL, "jdbc:oracle:thin:@localhost:1521:xe");
        props.setProperty(DataSourceFactory.JDBC_USER, "system");
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, "oracle");
        DataSource ds = dsf.createDataSource(props);
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
