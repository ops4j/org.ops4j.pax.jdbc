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
package org.ops4j.pax.jdbc.test.config;

import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.factoryConfiguration;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.jdbc.hook.PreHook;
import org.ops4j.pax.jdbc.test.AbstractJdbcTest;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.Constants;
import org.osgi.service.jdbc.DataSourceFactory;

/**
 * Uses the pax-jdbc-config module to create an H2 DataSource from a configuration and validates the
 * DataSource is present as a service
 */
@RunWith(PaxExam.class)
public class H2ConfigPreHookTest extends AbstractJdbcTest {

    private static final String JNDI_NAME = "osgi.jndi.service.name";

    @Inject
    DataSource ds;
    
    @Configuration
    public Option[] config() {
        InputStream preHookBundle = TinyBundles.bundle() //
            .add(MyPreHook.class) //
            .add(MyPreHookActivator.class) //
            .set(Constants.BUNDLE_ACTIVATOR, MyPreHookActivator.class.getName())
            .build(TinyBundles.withBnd());
        return new Option[] { //
            regressionDefaults(), //
            poolDefaults(), //
            mvnBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.jasypt"), //
            mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-config"), //
            mvnBundle("com.h2database", "h2"), //
            provision(preHookBundle), //
            factoryConfiguration("org.ops4j.datasource")
                .put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, "org.h2.Driver")
                .put(DataSourceFactory.JDBC_URL, "jdbc:h2:mem:pax;DB_CLOSE_DELAY=-1")
                .put(PreHook.CONFIG_KEY_NAME, "myprehook")
                .put(JNDI_NAME, "h2test").asOption()
            
        };
    }

    @Test
    public void testDataSourceFromConfig() throws SQLException {
        try (Connection con = ds.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from PERSON")) {
            rs.next();
            Assert.assertEquals(1, rs.getInt(1));
            Assert.assertEquals("Chris", rs.getString(2));
        }
    }

}
