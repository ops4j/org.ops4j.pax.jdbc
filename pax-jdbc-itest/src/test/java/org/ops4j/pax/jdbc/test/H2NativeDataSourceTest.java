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
package org.ops4j.pax.jdbc.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.util.Filter;
import org.osgi.service.jdbc.DataSourceFactory;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.jdbc.test.TestConfiguration.regressionDefaults;

@RunWith( PaxExam.class )
public class H2NativeDataSourceTest
{
    @Inject
    @Filter( "(osgi.jdbc.driver.name=h2)" )
    private DataSourceFactory dsf;

    @Configuration
    public Option[] config()
    {
        return options(
            regressionDefaults(),
            mavenBundle( "org.ops4j.pax.jdbc", "pax-jdbc-h2" ).versionAsInProject(),
            mavenBundle( "com.h2database", "h2" ).versionAsInProject(),
            mavenBundle( "org.osgi", "org.osgi.enterprise" ).versionAsInProject() );
    }

    @Test
    public void createDataSourceAndConnection() throws SQLException
    {
        assertNotNull( dsf );
        Properties props = new Properties();
        props.setProperty( DataSourceFactory.JDBC_DATABASE_NAME, "mem:pax" );
        props.setProperty( DataSourceFactory.JDBC_USER, "pax" );
        props.setProperty( DataSourceFactory.JDBC_PASSWORD, "pax" );
        DataSource dataSource = dsf.createDataSource( props );
        assertNotNull( dataSource );
        Connection connection = dataSource.getConnection();
        assertNotNull( connection );
        connection.close();
    }
}
