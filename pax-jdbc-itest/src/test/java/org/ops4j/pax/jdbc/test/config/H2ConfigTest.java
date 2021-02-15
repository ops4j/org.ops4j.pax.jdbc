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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.jdbc.config.ConfigLoader;
import org.ops4j.pax.jdbc.test.AbstractJdbcTest;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Uses the pax-jdbc-config module to create an H2 DataSource from a configuration and validates the
 * DataSource is present as a service
 */
@RunWith(PaxExam.class)
public class H2ConfigTest extends AbstractJdbcTest {

    private static final String JNDI_NAME = "osgi.jndi.service.name";

    @Inject
    ConfigurationAdmin configAdmin;
    
    @Configuration
    public Option[] config() {
        return new Option[] { //
            regressionDefaults(), //
            poolDefaults(), //
            mvnBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.jasypt"), //
            mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-config"), //
            mvnBundle("com.h2database", "h2") //
        };
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testDataSourceFromConfig() throws SQLException, IOException,
        InvalidSyntaxException, InterruptedException {
        org.osgi.service.cm.Configuration config = createConfigForDataSource();
        ServiceTracker tracker = new ServiceTracker(context, DataSource.class, null);
        tracker.open();
        DataSource dataSource = (DataSource)tracker.waitForService(2000);
        assertDataSourceWorks(dataSource);
        assertServicePropertiesPresent(tracker.getServiceReference());
        checkDataSourceIsDeletedWhenConfigIsDeleted(config, tracker);
        tracker.close();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testTwoDataSourcesFromConfig() throws Exception {
        org.osgi.service.cm.Configuration config1 = createConfigForDataSource("d1");
        org.osgi.service.cm.Configuration config2 = createConfigForDataSource("d2");
        ServiceTracker tracker1 = new ServiceTracker(context, context.createFilter("(&(objectClass=" + DataSource.class.getName() + ")(osgi.jndi.service.name=d1))"), null);
        ServiceTracker tracker2 = new ServiceTracker(context, context.createFilter("(&(objectClass=" + DataSource.class.getName() + ")(osgi.jndi.service.name=d2))"), null);
        tracker1.open();
        tracker2.open();
        DataSource dataSource1 = (DataSource)tracker1.waitForService(2000);
        DataSource dataSource2 = (DataSource)tracker2.waitForService(2000);
        assertDataSourceWorks(dataSource1);
        assertDataSourceWorks(dataSource2);
        assertServicePropertiesPresent(tracker1.getServiceReference(), "d1");
        assertServicePropertiesPresent(tracker2.getServiceReference(), "d2");

        FrameworkUtil.getBundle(ConfigLoader.class).stop();

        tracker1.close();
        tracker2.close();
    }

    private org.osgi.service.cm.Configuration createConfigForDataSource() throws IOException {
        return createConfigForDataSource("h2test");
    }

    private org.osgi.service.cm.Configuration createConfigForDataSource(String jndiName) throws IOException {
        org.osgi.service.cm.Configuration config = configAdmin.createFactoryConfiguration(
            "org.ops4j.datasource", null);
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, "org.h2.Driver");
        props.put(DataSourceFactory.JDBC_URL, "jdbc:h2:mem:pax");
        props.put(JNDI_NAME, jndiName); // jndi name for aries jndi
        config.update(props);
        return config;
    }

    private void assertDataSourceWorks(DataSource dataSource) throws SQLException {
        assertNotNull("No DataSource service found", dataSource);
        dataSource.getConnection().close();
    }

    private void assertServicePropertiesPresent(ServiceReference ref) {
        assertServicePropertiesPresent(ref, "h2test");
    }

    private void assertServicePropertiesPresent(ServiceReference ref, String jndiName) {
        Assert.assertEquals("org.h2.Driver", ref.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS));
        Assert.assertEquals("jdbc:h2:mem:pax", ref.getProperty(DataSourceFactory.JDBC_URL));
        Assert.assertEquals(jndiName, ref.getProperty(JNDI_NAME));
    }

    private void checkDataSourceIsDeletedWhenConfigIsDeleted(
        org.osgi.service.cm.Configuration config, ServiceTracker<DataSource, DataSource> tracker)
        throws IOException, InterruptedException {
        config.delete();
        Thread.sleep(200);
        assertNull(tracker.getService());
    }
}
