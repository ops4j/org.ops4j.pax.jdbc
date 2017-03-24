package org.ops4j.pax.jdbc.test.pool;

import javax.inject.Inject;

import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.jdbc.pool.common.PooledDataSourceFactory;
import org.ops4j.pax.jdbc.test.AbstractJdbcTest;

public class PoolAriesTest extends AbstractJdbcTest {

    @Inject @Filter("(pool=aries)(xa=false)")
    PooledDataSourceFactory pool;
    
    @Configuration
    public Option[] config() {
        return new Option[] { //
                regressionDefaults(), //
                poolDefaults(), //
                mvnBundle("commons-logging", "commons-logging"),
                mvnBundle("org.apache.geronimo.components", "geronimo-connector"),
                mvnBundle("org.apache.geronimo.specs", "geronimo-j2ee-connector_1.6_spec"),
                mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-pool-aries"),
                };
    }

    @Test
    public void testPooledDataSourceFactoryServicesPresent() {
    }

}
