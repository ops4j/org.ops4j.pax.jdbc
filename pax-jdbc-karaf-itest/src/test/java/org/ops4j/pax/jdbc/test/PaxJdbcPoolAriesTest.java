package org.ops4j.pax.jdbc.test;

import java.io.File;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.osgi.service.jdbc.DataSourceFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.Filter;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;

/**
 * Tests the automatic creation of pooled DataSourceFactory service from an existing DataSourceFactory
 * 
 * We assume that h2 publishes a DataSourceFactory with "osgi.jdbc.driver.name=h2".
 * 
 * pax-jdbc-pool should then create a pooled DataSourceFactory with "osgi.jdbc.driver.name=h2-pool".
 * If a TransactionManager service is available it will also create a XA pooled DataSourceFactory 
 * with "osgi.jdbc.driver.name=h2-pool-xa". Keep in mind that you need to use createDataSource() to create 
 * a transactional DataSource not createXADataSource().
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class PaxJdbcPoolAriesTest extends AbstractJdbcTest {

    @Inject
    @Filter("(osgi.jdbc.driver.name=h2-pool)")
    DataSourceFactory pooledDataSourceFactory;

    @Inject
    @Filter("(osgi.jdbc.driver.name=h2-pool-xa)")
    DataSourceFactory pooledXADataSourceFactory;

    @Configuration
    public Option[] config() {
        MavenUrlReference paxJdbcRepo = maven()
            .groupId("org.ops4j.pax.jdbc")
            .artifactId("pax-jdbc-features")
            .classifier("features")
            .type("xml")
            .versionAsInProject();
        return new Option[]{
//                KarafDistributionOption.debugConfiguration("5005", true),
                karafDistributionConfiguration()
                        .frameworkUrl(karafUrl)
                        .unpackDirectory(new File("target/exam"))
                        .useDeployFolder(false),
                keepRuntimeFolder(),
                KarafDistributionOption.features(paxJdbcRepo, "transaction", "pax-jdbc-h2", "pax-jdbc-pool-aries"),
        };
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