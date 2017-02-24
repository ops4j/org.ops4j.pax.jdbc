package org.ops4j.pax.jdbc.test.jtds;

import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

import java.sql.SQLException;
import java.util.Properties;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.jdbc.test.AbstractJdbcTest;
import org.ops4j.pax.jdbc.test.ServerConfiguration;
import org.osgi.service.jdbc.DataSourceFactory;

public class JtdsNativeDataSourceTest extends AbstractJdbcTest {
    @Rule
    public ServerConfiguration config = new ServerConfiguration("jtds");

    @Inject
    @Filter("(osgi.jdbc.driver.name=jtds)")
    private DataSourceFactory dsf;

    @Configuration
    public Option[] config() {
        return options(regressionDefaults(), //
            mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-jtds"), //
            wrappedBundle("mvn:net.sourceforge.jtds/jtds/") //
        );
    }

    @Test
    public void createDataSourceAndConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_SERVER_NAME, config.getServerName());
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, config.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_PORT_NUMBER, config.getPortNumberSt());
        props.setProperty(DataSourceFactory.JDBC_DATASOURCE_NAME, "testds");
        props.setProperty(DataSourceFactory.JDBC_USER, config.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, config.getPassword());
        
        dsf.createDataSource(props).getConnection().close();
    }

}

