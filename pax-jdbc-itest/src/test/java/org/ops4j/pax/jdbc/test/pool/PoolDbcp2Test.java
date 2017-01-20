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
package org.ops4j.pax.jdbc.test.pool;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.transaction.TransactionManager;

import org.junit.Assert;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.ops4j.pax.jdbc.test.AbstractJdbcTest;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Checks that the pax-jdbc-pool-dbcp2 module creates an XA pooled and a normal pooled
 * DataSourceFactory
 */
public class PoolDbcp2Test extends AbstractJdbcTest {

    @Inject
    BundleContext context;

    @Configuration
    public Option[] config() {
        return new Option[] {
            regressionDefaults(), //
            mvnBundle("com.h2database", "h2"), //
            mvnBundle("org.apache.commons", "commons-pool2"), //
            mvnBundle("commons-logging", "commons-logging"), //
            mvnBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.cglib"), //
            mvnBundle("org.apache.geronimo.specs", "geronimo-jta_1.1_spec"), //
            mvnBundle("org.apache.commons", "commons-dbcp2"), //
            mvnBundle("org.apache.aries", "org.apache.aries.util"), //
            mvnBundle("org.apache.aries.transaction", "org.apache.aries.transaction.manager")
                .noStart(), //
            mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-pool-common"), //
            mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-pool-dbcp2"), //
        };
    }

    @SuppressWarnings({
     "unchecked", "rawtypes"
    })
    @Test
    public void testDataSourceFactoryCreated() {
        assertAllBundlesResolved();
        ServiceTracker tracker = new ServiceTracker(context, PooledDataSourceFactory.class, null);
        tracker.open();
        Object[] services = tracker.getServices();
        Assert.assertEquals("No TransactionManager service installed."
            + "So we expect only the pooling", 1,
            services.length);
        Set<String> names = getProp(tracker, "pool");
        Set<String> expectedNames = asSet("dbcp2");
        Assert.assertEquals(expectedNames, names);
        // printDataSourceFactories(tracker);
    }

    @Test
    public void testXADataSourceFactoryCreated() throws BundleException, InterruptedException, InvalidSyntaxException {
        assertAllBundlesResolved();
        Bundle tmBundle = getBundle("org.apache.aries.transaction.manager");
        tmBundle.start();
        Filter filter = FrameworkUtil.createFilter("(&(objectClass="+PooledDataSourceFactory.class.getName()+")(xa=true)(pool=dbcp2))");
        ServiceTracker<DataSourceFactory, Object> tracker = new ServiceTracker<DataSourceFactory, Object>(
            context, filter, null);
        tracker.open();
        Assert.assertEquals(1, tracker.getServiceReferences().length);
        tmBundle.stop();
        Assert.assertNull(context.getServiceReference(TransactionManager.class));
        Thread.sleep(1000);
        Assert.assertNull(tracker.getServiceReferences());
        tracker.close();
        // printDataSourceFactories(tracker);
    }

    private Set<String> asSet(String... values) {
        return new HashSet<String>(Arrays.asList(values));
    }


    private Set<String> getProp(ServiceTracker<DataSourceFactory, Object> tracker, String key) {
        Set<String> results = new HashSet<String>();
        for (ServiceReference<DataSourceFactory> ref : tracker.getServiceReferences()) {
            results.add((String) ref.getProperty(key));
        }
        return results;
    }

}
