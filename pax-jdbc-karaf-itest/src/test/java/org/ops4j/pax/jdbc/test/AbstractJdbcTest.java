package org.ops4j.pax.jdbc.test;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.maven;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.ops4j.pax.exam.options.MavenUrlReference;
import org.osgi.service.jdbc.DataSourceFactory;

public class AbstractJdbcTest {
    MavenUrlReference karafUrl = maven()
        .groupId("org.apache.karaf")
        .artifactId("apache-karaf")
        .version("3.0.1")
        .type("tar.gz");

    protected DataSource createDataSource(DataSourceFactory dsf) throws SQLException {
        assertNotNull(dsf);
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, "test");
        props.setProperty(DataSourceFactory.JDBC_USER, "SA");
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, "");
        return dsf.createDataSource(props);
    }

}
