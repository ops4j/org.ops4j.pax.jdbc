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

/**
 * Tests the automatic creation of pooled DataSourceFactory service from an existing
 * DataSourceFactory
 * 
 * We assume that h2 publishes a DataSourceFactory with "osgi.jdbc.driver.name=h2".
 * 
 * pax-jdbc-pool should then create a pooled DataSourceFactory with "osgi.jdbc.driver.name=h2-pool".
 * If a TransactionManager service is available it will also create a XA pooled DataSourceFactory
 * with "osgi.jdbc.driver.name=h2-pool-xa". Keep in mind that you need to use createDataSource() to
 * create a transactional DataSource not createXADataSource().
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class PaxJdbcPoolTest extends AbstractJdbcTest {

    @Inject
    @Filter("(osgi.jdbc.driver.name=H2-pool)")
    DataSourceFactory pooledDataSourceFactory;

    @Inject
    @Filter("(osgi.jdbc.driver.name=H2-pool-xa)")
    DataSourceFactory pooledXADataSourceFactory;

    @Configuration
    public Option[] config() {
        return new Option[] { karafDefaults(),
            features(paxJdbcRepo(), "transaction", "pax-jdbc-h2", "pax-jdbc-pool-dbcp2") };
    }

    @Test
    public void testPooledDataSourceFactory() throws SQLException {
        DataSource dataSource = createDataSource(pooledDataSourceFactory);
        checkDataSource(dataSource);
    }

    @Test
    public void testPooledXADataSourceFactory() throws SQLException {
        DataSource dataSource = createDataSource(pooledXADataSourceFactory);
        checkDataSource(dataSource);
    }

}
