package org.ops4j.pax.jdbc.test.pool;

import javax.inject.Inject;

import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.ops4j.pax.jdbc.test.AbstractJdbcTest;

/**
 * Checks that the pax-jdbc-pool-hikaricp module creates an XA pooled and a normal pooled
 * DataSourceFactory
 */
public class PoolHikaricpTest extends AbstractJdbcTest {

    @Inject @Filter("(pool=hikari)")
    PooledDataSourceFactory pdsf;

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

    @Test
    public void testDataSourceFactoryCreated() {
        // Just testing we get the service injected 
    }

}
