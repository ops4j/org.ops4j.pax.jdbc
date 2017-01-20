/*
 * Copyright 2012 Harald Wellmann.
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
package org.ops4j.pax.jdbc.test.h2;

import static org.ops4j.pax.exam.CoreOptions.options;

import java.sql.SQLException;
import java.util.Properties;

import javax.inject.Inject;

import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.jdbc.test.AbstractJdbcTest;
import org.osgi.service.jdbc.DataSourceFactory;

public class H2NativeDataSourceTest extends AbstractJdbcTest {

    @Inject
    @Filter("(osgi.jdbc.driver.name=H2)")
    private DataSourceFactory dsf;

    @Configuration
    public Option[] config() {
        return options(regressionDefaults(), //
            mvnBundle("com.h2database", "h2") //
        );
    }

    @Test
    public void createDataSourceAndConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, "mem:pax");
        props.setProperty(DataSourceFactory.JDBC_USER, "pax");
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, "pax");
        dsf.createDataSource(props).getConnection().close();
    }
}
