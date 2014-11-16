package org.ops4j.pax.jdbc.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.karaf.features.FeaturesService;
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
import org.osgi.service.jdbc.DataSourceFactory;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class PaxJdbcFeaturesTest extends AbstractJdbcTest {

    @Inject
    FeaturesService featuresService;

    @Inject
    @Filter("(osgi.jdbc.driver.name=h2)")
    DataSourceFactory h2DataSourceFactory;

    @Configuration
    public Option[] config() {
        MavenUrlReference paxJdbcRepo = maven().groupId("org.ops4j.pax.jdbc")
            .artifactId("pax-jdbc-features").classifier("features").type("xml")
            .versionAsInProject();
        return new Option[] {
            // KarafDistributionOption.debugConfiguration("5005", true),
            karafDistributionConfiguration().frameworkUrl(karafUrl)
                .unpackDirectory(new File("target/exam")).useDeployFolder(false),
            keepRuntimeFolder(),
            KarafDistributionOption.features(paxJdbcRepo, "pax-jdbc-h2", "pax-jdbc-derby",
                "pax-jdbc-sqlite", "pax-jdbc-mariadb", "pax-jdbc-mysql", "pax-jdbc-postgresql",
                "pax-jdbc-pool-dbcp2"), };
    }

    @Test
    public void testPaxJdbcH2FeatureInstalls() throws Exception {
        assertFeatureInstalled("pax-jdbc-h2");
    }

    @Test
    public void testPaxJdbcDerbyFeatureInstalls() throws Exception {
        assertFeatureInstalled("pax-jdbc-derby");
    }

    @Test
    public void testPaxJdbcSqliteFeatureInstalls() throws Exception {
        assertFeatureInstalled("pax-jdbc-sqlite");
    }

    @Test
    public void testPaxJdbcMariaDbFeatureInstalls() throws Exception {
        assertFeatureInstalled("pax-jdbc-mariadb");
    }

    @Test
    public void testPaxJdbcMysqlFeatureInstalls() throws Exception {
        assertFeatureInstalled("pax-jdbc-mysql");
    }

    @Test
    public void testPaxJdbcPostgreSqlFeatureInstalls() throws Exception {
        assertFeatureInstalled("pax-jdbc-postgresql");
    }

    @Test
    public void testPaxJdbcPoolDbcp2FeatureInstalls() throws Exception {
        assertFeatureInstalled("pax-jdbc-pool-dbcp2");
    }

    @Test
    public void testH2FeatureIsDeployedAndUsable() throws SQLException {
        DataSource dataSource = createDataSource();
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement
            .execute("CREATE TABLE PUBLIC.T1 (col1 INTEGER NOT NULL, col2 CHAR(25), PRIMARY KEY (COL1)) ");
        statement.executeUpdate("insert into t1 (col1, col2) values(101, 'pax-jdbc-h2')");
        ResultSet result = statement.executeQuery("select col1 from t1 where col2 = 'pax-jdbc-h2'");

        while (result.next()) {
            assertEquals(101, result.getInt("col1"));
        }

        statement.close();
        connection.close();
    }

    private DataSource createDataSource() throws SQLException {
        assertNotNull(h2DataSourceFactory);
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, "test");
        props.setProperty(DataSourceFactory.JDBC_USER, "SA");
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, "");
        return h2DataSourceFactory.createDataSource(props);
    }

    private void assertFeatureInstalled(String featureName) throws Exception {
        assertTrue(featuresService.isInstalled(featuresService.getFeature(featureName)));
    }
}
