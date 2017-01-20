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
import org.osgi.service.jdbc.DataSourceFactory;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class PaxJdbcFeaturesTest extends AbstractJdbcTest {

    @Inject
    @Filter("(osgi.jdbc.driver.name=H2)")
    DataSourceFactory h2DataSourceFactory;

    @Configuration
    public Option[] config() {
        return new Option[] {
            karafDefaults(),
            features(paxJdbcRepo(), "pax-jdbc-h2", "pax-jdbc-derby", "pax-jdbc-sqlite",
                "pax-jdbc-mariadb", "pax-jdbc-mysql", "pax-jdbc-postgresql", "pax-jdbc-pool-dbcp2",
                "pax-jdbc-pool-dbcp2", "pax-jdbc-pool-aries"), };
    }

    @Test
    public void testPaxJdbcH2FeatureInstalls() throws Exception {
        assertFeatureInstalled("pax-jdbc-h2");
        assertFeatureInstalled("pax-jdbc-derby");
        assertFeatureInstalled("pax-jdbc-sqlite");
        assertFeatureInstalled("pax-jdbc-mariadb");
        assertFeatureInstalled("pax-jdbc-mysql");
        assertFeatureInstalled("pax-jdbc-postgresql");
        assertFeatureInstalled("pax-jdbc-pool-dbcp2");
    }
    
    @Test
    public void testH2FeatureIsDeployedAndUsable() throws SQLException {
        DataSource dataSource = createDataSource(h2DataSourceFactory);
        checkDataSource(dataSource);
    }

}
