/*
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
package org.ops4j.pax.jdbc.test.config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.ops4j.pax.jdbc.test.TestConfiguration.mvnBundle;
import static org.ops4j.pax.jdbc.test.TestConfiguration.regressionDefaults;

import java.io.IOException;
import java.sql.Connection;
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
import org.osgi.framework.BundleContext;
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
public class H2ConfigTest {

    private static final String JNDI_NAME = "osgi.jndi.service.name";

    @Inject
    ConfigurationAdmin configAdmin;

    @Inject
    BundleContext context;

    @Configuration
    public Option[] config() {
        return new Option[] { //
            regressionDefaults(), //
            mvnBundle("org.apache.felix", "org.apache.felix.configadmin"), //
            mvnBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.jasypt"), //
            mvnBundle("com.h2database", "h2"), //
            mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-config") //
        };
    }

    @Test
    public void testDataSourceFromConfig() throws SQLException, IOException,
        InvalidSyntaxException, InterruptedException {
        org.osgi.service.cm.Configuration config = createConfigForDataSource();
        ServiceTracker<DataSource, Object> tracker = new ServiceTracker<DataSource, Object>(
            context, DataSource.class, null);
        tracker.open();
        DataSource dataSource = (DataSource) tracker.waitForService(1000);
        assertDataSourceWorks(dataSource);
        assertServicePropertiesPresent(tracker);
        checkDataSourceIsDeletedWhenConfigIsDeleted(config, tracker);
    }

    private org.osgi.service.cm.Configuration createConfigForDataSource() throws IOException {
        org.osgi.service.cm.Configuration config = configAdmin.createFactoryConfiguration(
            "org.ops4j.datasource", null);
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, "org.h2.Driver");
        props.put(DataSourceFactory.JDBC_URL, "jdbc:h2:mem:pax");
        props.put(JNDI_NAME, "h2test"); // jndi name for aries jndi
        config.update(props);
        return config;
    }

    private void assertDataSourceWorks(DataSource dataSource) throws InterruptedException,
        SQLException {
        assertNotNull("No DataSource service found", dataSource);
        Connection connection = dataSource.getConnection();
        assertNotNull(connection);
        connection.close();
    }

    private void assertServicePropertiesPresent(ServiceTracker<DataSource, Object> tracker) {
        ServiceReference<DataSource> ref = tracker.getServiceReference();
        Assert.assertEquals("org.h2.Driver",
            ref.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS));
        Assert.assertEquals("jdbc:h2:mem:pax", ref.getProperty(DataSourceFactory.JDBC_URL));
        Assert.assertEquals("h2test", ref.getProperty(JNDI_NAME));
    }

    private void checkDataSourceIsDeletedWhenConfigIsDeleted(
        org.osgi.service.cm.Configuration config, ServiceTracker<DataSource, Object> tracker)
        throws IOException, InterruptedException {
        config.delete();
        Thread.sleep(1000);
        DataSource dataSource = (DataSource) tracker.getService();
        assertNull(dataSource);
    }
}
