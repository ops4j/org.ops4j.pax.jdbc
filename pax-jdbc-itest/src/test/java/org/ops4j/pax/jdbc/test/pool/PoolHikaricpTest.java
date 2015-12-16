package org.ops4j.pax.jdbc.test.pool;

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

import static org.ops4j.pax.jdbc.test.TestConfiguration.mvnBundle;
import static org.ops4j.pax.jdbc.test.TestConfiguration.regressionDefaults;

/**
 * Checks that the pax-jdbc-pool-hikaricp module creates an XA pooled and a normal pooled
 * DataSourceFactory
 */
@RunWith(PaxExam.class)
public class PoolHikaricpTest {

    @Inject
    BundleContext context;

    @Configuration
    public Option[] config() {
        return new Option[] { regressionDefaults(), //
                mvnBundle("com.h2database", "h2"), mvnBundle("commons-logging", "commons-logging"),
                mvnBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.cglib"),
                mvnBundle("org.apache.geronimo.specs", "geronimo-jta_1.1_spec"),
                mvnBundle("org.apache.aries", "org.apache.aries.util"),
                mvnBundle("org.apache.aries.transaction", "org.apache.aries.transaction.manager")
                        .noStart(),

                mvnBundle("com.zaxxer", "HikariCP"),
                mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-pool-common"),
                mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-pool-hikaricp"), };
    }

    @Test
    public void testDataSourceFactoryCreated() {
        assertAllBundlesResolved();
        final ServiceTracker<DataSourceFactory, Object> tracker = new ServiceTracker<DataSourceFactory, Object>(
                context, DataSourceFactory.class, null);
        tracker.open();
        Assert.assertEquals(
                "No TransactionManager service installed."
                        + "So we expect only the original DataSourceFactory and our pooling one",
                2, tracker.getServiceReferences().length);
        final Set<String> names = getDataFactoryNames(tracker);
        final Set<String> expectedNames = asSet("H2", "H2-pool");
        Assert.assertEquals(expectedNames, names);
        printDataSourceFactories(tracker);
    }

    @Test
    public void testXADataSourceFactoryCreated() throws BundleException, InterruptedException {
        assertAllBundlesResolved();
        final Bundle tmBundle = getBundle("org.apache.aries.transaction.manager");
        tmBundle.start();
        final ServiceTracker<DataSourceFactory, Object> tracker = new ServiceTracker<DataSourceFactory, Object>(
                context, DataSourceFactory.class, null);
        tracker.open();
        final Set<String> names = getDataFactoryNames(tracker);
        Assert.assertEquals(asSet("H2", "H2-pool", "H2-pool-xa"), names);
        tmBundle.stop();
        Assert.assertNull(context.getServiceReference(TransactionManager.class));
        Thread.sleep(1000);
        final Set<String> names2 = getDataFactoryNames(tracker);
        Assert.assertEquals(asSet("H2", "H2-pool"), names2);
        printDataSourceFactories(tracker);
    }

    private Set<String> asSet(final String... values) {
        return new HashSet<String>(Arrays.asList(values));
    }

    private Bundle getBundle(final String symbolicName) {
        for (final Bundle bundle : context.getBundles()) {
            if (bundle.getSymbolicName().equals(symbolicName)) {
                return bundle;
            }
        }
        return null;
    }

    private Set<String> getDataFactoryNames(
            final ServiceTracker<DataSourceFactory, Object> tracker) {
        final Set<String> results = new HashSet<String>();
        for (final ServiceReference<DataSourceFactory> ref : tracker.getServiceReferences()) {
            results.add((String) ref.getProperty(DataSourceFactory.OSGI_JDBC_DRIVER_NAME));
        }
        return results;
    }

    @SuppressWarnings("unused")
    private void printDataSourceFactories(final ServiceTracker<DataSourceFactory, Object> tracker) {
        for (final ServiceReference<DataSourceFactory> ref : tracker.getServiceReferences()) {
            System.out.println("DataSourceFactory Service");
            final String[] keys = ref.getPropertyKeys();
            Arrays.sort(keys);
            for (final String key : keys) {
                System.out.println("  " + key + ":" + ref.getProperty(key));
            }
        }
    }

    private void assertAllBundlesResolved() {
        for (final Bundle bundle : context.getBundles()) {
            if (bundle.getState() == Bundle.INSTALLED) {
                // Provoke exception
                try {
                    bundle.start();
                } catch (final BundleException e) {
                    Assert.fail(e.getMessage());
                }
            }
        }
    }

}
