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
package org.ops4j.pax.jdbc.test.hsqldb;

import java.sql.SQLException;
import java.util.Properties;
import javax.inject.Inject;

import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.jdbc.test.AbstractJdbcTest;
import org.ops4j.pax.jdbc.test.ServerConfiguration;
import org.osgi.service.jdbc.DataSourceFactory;

import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

public class HsqldbNativeDataSourceTest extends AbstractJdbcTest {

    @Inject
    @Filter("(osgi.jdbc.driver.name=hsqldb)")
    private DataSourceFactory dsf;

    @Configuration
    public Option[] config() {
        return combine(regressionDefaults(), //
                mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc"), //
                mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-hsqldb"), //
                wrappedBundle("mvn:org.hsqldb/hsqldb/" + System.getProperty("hsqldb.jdbc.version") + "/jar/jdk8") //
        );
    }

    @Test
    public void createDataSourceAndConnection() throws SQLException {
        ServerConfiguration config = new ServerConfiguration("hsqldb");

        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, "mem:.");
        props.setProperty(DataSourceFactory.JDBC_USER, config.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, config.getPassword());
        dsf.createDataSource(props).getConnection().close();
    }

}
