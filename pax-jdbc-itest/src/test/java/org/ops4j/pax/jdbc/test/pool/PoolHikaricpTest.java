package org.ops4j.pax.jdbc.test.pool;

import static org.ops4j.pax.jdbc.test.TestConfiguration.mvnBundle;
import static org.ops4j.pax.jdbc.test.TestConfiguration.regressionDefaults;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

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
                mvnBundle("com.zaxxer", "HikariCP"),
                mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-pool-common"),
                mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-pool-hikaricp"), };
    }

    @SuppressWarnings({
     "unchecked", "rawtypes"
    })
    @Test
    public void testDataSourceFactoryCreated() {
        final ServiceTracker tracker = new ServiceTracker(context, PooledDataSourceFactory.class, null);
        tracker.open();
        Assert.assertEquals(1, tracker.getServiceReferences().length);
        String poolName = (String)tracker.getServiceReferences()[0].getProperty("pool");
        Assert.assertEquals("hikari", poolName);
        tracker.close();
    }

}
