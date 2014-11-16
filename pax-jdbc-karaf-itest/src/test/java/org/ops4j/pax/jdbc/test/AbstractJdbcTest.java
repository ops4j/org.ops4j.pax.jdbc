package org.ops4j.pax.jdbc.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.maven;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import org.ops4j.pax.exam.options.MavenUrlReference;
import org.osgi.service.jdbc.DataSourceFactory;

public class AbstractJdbcTest {

    MavenUrlReference karafUrl = maven().groupId("org.apache.karaf").artifactId("apache-karaf")
        .version("3.0.1").type("tar.gz");

    protected DataSource createDataSource(DataSourceFactory dsf) throws SQLException {
        assertNotNull(dsf);
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, "test");
        props.setProperty(DataSourceFactory.JDBC_USER, "SA");
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, "");
        return dsf.createDataSource(props);
    }

    protected void checkDataSource(DataSource dataSource) throws SQLException {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement
            .execute("CREATE TABLE PUBLIC.T1 (col1 INTEGER NOT NULL, col2 CHAR(25), PRIMARY KEY (COL1)) ");
        statement.executeUpdate("insert into t1 (col1, col2) values(101, 'pax-jdbc-h2')");
        ResultSet result = statement.executeQuery("select col1 from t1 where col2 = 'pax-jdbc-h2'");

        while (result.next()) {
            assertEquals(101, result.getInt("col1"));
        }
        result.close();

        statement.execute("DROP TABLE PUBLIC.T1");

        statement.close();
        connection.close();
    }
}
