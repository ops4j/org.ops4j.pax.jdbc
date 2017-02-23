package org.ops4j.pax.jdbc.test.jtds;

import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

import java.sql.SQLException;
import java.util.Properties;

import javax.inject.Inject;

import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.jdbc.test.AbstractJdbcTest;
import org.osgi.service.jdbc.DataSourceFactory;

public class JtdsNativeDataSourceTest extends AbstractJdbcTest {
	
	@Inject
    @Filter("(osgi.jdbc.driver.name=jtds)")
    private DataSourceFactory dsf;
	
	private final String SERVER_NAME="localhost";
	private final String DB_NAME = "test";
	private final int PORT=1433;
	private final String USER = "pax";
	private final String PASSWORD = "pax";
	
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
        //props.setProperty(DataSourceFactory.JDBC_URL, "jdbc:jtds:sqlserver://" + SERVER_NAME + ":" + PORT + "/" + DB_NAME);
        props.setProperty(DataSourceFactory.JDBC_SERVER_NAME, SERVER_NAME);
        props.setProperty(DataSourceFactory.JDBC_DATABASE_NAME, DB_NAME);
        props.setProperty(DataSourceFactory.JDBC_PORT_NUMBER, "" + PORT);
        props.setProperty(DataSourceFactory.JDBC_DATASOURCE_NAME, "testds");
        		
        
        props.setProperty(DataSourceFactory.JDBC_USER, USER);
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, PASSWORD);
                		
        dsf.createDataSource(props).getConnection().close();
    }

}

