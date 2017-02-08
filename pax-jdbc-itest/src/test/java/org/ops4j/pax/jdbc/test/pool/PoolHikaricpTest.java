package org.ops4j.pax.jdbc.test.pool;

import javax.inject.Inject;

import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.ops4j.pax.jdbc.test.AbstractJdbcTest;

public class PoolHikaricpTest extends AbstractJdbcTest {

    /**
     * Hikari only provides a non xa pooling support
     */
    @Inject @Filter("(pool=hikari)(xa=false)")
    PooledDataSourceFactory pool;
    
    @Configuration
    public Option[] config() {
        return new Option[] { //
                regressionDefaults(), //
                poolDefaults(), //
                mvnBundle("commons-logging", "commons-logging"),
                mvnBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.cglib"),
                mvnBundle("com.zaxxer", "HikariCP"),
                mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-pool-hikaricp"),
                };
    }

    @Test
    public void testPooledDataSourceFactoryServicesPresent() {
    }

}
