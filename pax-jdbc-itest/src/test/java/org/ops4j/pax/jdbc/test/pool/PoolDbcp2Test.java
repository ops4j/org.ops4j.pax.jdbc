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

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.jdbc.test.TestConfiguration.regressionDefaults;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.transaction.TransactionManager;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Checks that the pax-jdbc-pool-dbcp2 module creates an XA pooled and a normal
 * pooled DataSourceFactory
 */
@RunWith(PaxExam.class)
public class PoolDbcp2Test {

    @Inject
    BundleContext context;

    @Configuration
    public Option[] config() {
        //System.setProperty("pax.exam.osgi.unresolved.fail", "true");
        return new Option[] {
            regressionDefaults(),
            mavenBundle("org.osgi", "org.osgi.enterprise").versionAsInProject(),
            mavenBundle("com.h2database", "h2").versionAsInProject(),
            mavenBundle("org.apache.commons", "commons-pool2").versionAsInProject(),
            mavenBundle("commons-logging", "commons-logging").versionAsInProject(),
            mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.cglib").versionAsInProject(),
            mavenBundle("org.apache.geronimo.specs", "geronimo-jta_1.1_spec").versionAsInProject(),
            mavenBundle("org.apache.commons", "commons-dbcp2").versionAsInProject(),
            mavenBundle("org.apache.aries", "org.apache.aries.util").version("1.1.0"),
            mavenBundle("org.apache.aries.transaction", "org.apache.aries.transaction.manager").version("1.1.1").noStart(),
            mavenBundle("org.ops4j.pax.jdbc", "pax-jdbc-pool-dbcp2").versionAsInProject(),
        };
    }

    @Test
    public void testDataSourceFactoryCreated() {
        assertAllBundlesResolved();
        ServiceTracker<DataSourceFactory, Object> tracker = 
            new ServiceTracker<DataSourceFactory, Object>(context, DataSourceFactory.class, null);
        tracker.open();
        Assert.assertEquals("No TransactionManager service installed."
                            + "So we expect only the original DataSourceFactory and our pooling one",
                            2, tracker.getServiceReferences().length);
        Set<String> names = getDataFactoryNames(tracker);
        Set<String> expectedNames = asSet("H2", "H2-pool");
        Assert.assertEquals(expectedNames, names);
        //printDataSourceFactories(tracker);
    }

    @Test
    public void testXADataSourceFactoryCreated() throws BundleException, InterruptedException {
        assertAllBundlesResolved();
        Bundle tmBundle = getBundle("org.apache.aries.transaction.manager");
        tmBundle.start();
        ServiceTracker<DataSourceFactory, Object> tracker = 
            new ServiceTracker<DataSourceFactory, Object>(context, DataSourceFactory.class, null);
        tracker.open();
        Set<String> names = getDataFactoryNames(tracker);
        Assert.assertEquals(asSet("H2", "H2-pool", "H2-pool-xa"), names);
        tmBundle.stop();
        Assert.assertNull(context.getServiceReference(TransactionManager.class));
        Thread.sleep(1000);
        Set<String> names2 = getDataFactoryNames(tracker);
        Assert.assertEquals(asSet("H2", "H2-pool"), names2);
        //printDataSourceFactories(tracker);
    }
    
    private Set<String> asSet(String ... values) {
        return new HashSet<String>(Arrays.asList(values));
    }

    private Bundle getBundle(String symbolicName) {
        for (Bundle bundle : context.getBundles()) {
            if (bundle.getSymbolicName().equals(symbolicName)) {
                return bundle;
            }
        } 
        return null;
    }
    
    private Set<String> getDataFactoryNames(ServiceTracker<DataSourceFactory, Object> tracker) {
        Set<String> results = new HashSet<String>();
        for (ServiceReference<DataSourceFactory> ref : tracker.getServiceReferences()) {
            results.add((String)ref.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_NAME));
        }
        return results;
    }

    @SuppressWarnings("unused")
    private void printDataSourceFactories(ServiceTracker<DataSourceFactory, Object> tracker) {
        for (ServiceReference<DataSourceFactory> ref : tracker.getServiceReferences()) {
            System.out.println("DataSourceFactory Service");
            String[] keys = ref.getPropertyKeys();
            Arrays.sort(keys);
            for (String key : keys) {
                System.out.println("  " + key + ":" + ref.getProperty(key));
            }
        }
    }

    private void assertAllBundlesResolved() {
        for (Bundle bundle : context.getBundles()) {
            if (bundle.getState() == Bundle.INSTALLED) {
                // Provoke exception
                try {
                    bundle.start();
                } catch (BundleException e) {
                    Assert.fail(e.getMessage());
                }
            }
        }
    }

}
