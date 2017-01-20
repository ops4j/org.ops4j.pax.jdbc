package org.ops4j.pax.jdbc.test;

import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.Filter;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class PaxJdbcConfigPoolTest extends AbstractJdbcTest {

    @Inject
    @Filter("(osgi.jndi.service.name=withpool)")
    DataSource dataSource;

    @Configuration
    public Option[] config() {
        return new Option[] //
        { //
          karafDefaults(), //
          features(paxJdbcRepo(), "pax-jdbc-h2", "pax-jdbc-config", "pax-jdbc-pool-dbcp2"),
          applyConfig("org.ops4j.datasource-withpool.cfg")
        };
    }

    

    /**
     * Test that pooled DataSource was created from given config and is usable
     */
    @Test
    public void testDataSourceFromConfig() throws SQLException {
        checkDataSource(dataSource);
    }

}
