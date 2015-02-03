package org.ops4j.pax.jdbc.test;

import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
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

/**
 * Tests the automatic creation of a non pooled DataSource from config admin configs
 * 
 * We assume that h2 publishes a DataSourceFactory with "osgi.jdbc.driver.name=H2". We let
 * pax-jdbc-config create a DataSource for these DataSourceFactories by supplying a config.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class PaxJdbcConfigTest extends AbstractJdbcTest {

    private static final String DS_CONFIG = "etc/org.ops4j.datasource-test2.cfg";

    @Inject
    @Filter("(osgi.jndi.service.name=test2)")
    DataSource dataSource;

    @Configuration
    public Option[] config() {
        return new Option[] {
            karafDefaults(),
            features(paxJdbcRepo(), "pax-jdbc-h2", "pax-jdbc-config"),
            editConfigurationFilePut(DS_CONFIG, DataSourceFactory.OSGI_JDBC_DRIVER_NAME, "H2"),
            editConfigurationFilePut(DS_CONFIG, DataSourceFactory.JDBC_DATABASE_NAME, "test2"),
            editConfigurationFilePut(DS_CONFIG, DataSourceFactory.JDBC_USER, "sa"),
            editConfigurationFilePut(DS_CONFIG, DataSourceFactory.JDBC_PASSWORD, ""),
            editConfigurationFilePut(DS_CONFIG, "osgi.jndi.service.name", "test2")
        };
    }

    @Test
    public void testDataSourceFromConfig() throws SQLException {
        checkDataSource(dataSource);
    }

}
