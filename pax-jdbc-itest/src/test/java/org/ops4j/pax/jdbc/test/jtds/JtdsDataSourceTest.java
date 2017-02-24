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

public class JtdsDataSourceTest extends AbstractJdbcTest {
    @Rule
    public ServerConfiguration config = new ServerConfiguration("jtds");

    @Inject
    @Filter("(osgi.jdbc.driver.class=net.sourceforge.jtds.jdbc.Driver)")
    private DataSourceFactory dsf;

    @Configuration
    public Option[] config() {
        return options(regressionDefaults(), //
            mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc"), //
            wrappedBundle("mvn:net.sourceforge.jtds/jtds/") //
        );
    }

    @Test
    public void createDataSourceAndConnection() throws SQLException {	
        Properties props = new Properties();
        String url = String.format("jdbc:jtds:sqlserver://%s:%s/%s", config.getServerName(), config.getPortNumberSt(), config.getDatabaseName());
        props.setProperty(DataSourceFactory.JDBC_URL, url);
        props.setProperty(DataSourceFactory.JDBC_USER, config.getUser());
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, config.getPassword());
        dsf.createDataSource(props).getConnection().close();
    }

}
