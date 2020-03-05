package org.ops4j.pax.jdbc.iotdb.impl;

import org.apache.iotdb.jdbc.IoTDBDriver;
import org.ops4j.pax.jdbc.common.BeanConfig;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

public class IoTDbDataSourceFactory implements DataSourceFactory {
    private final Logger logger = LoggerFactory.getLogger(IoTDbDataSourceFactory.class);
    @Override
    public DataSource createDataSource(Properties properties) throws SQLException {
       IoTDbDataSource ds = new IoTDbDataSource();
       setProperties(ds, properties);
       return  ds;
    }
    public void setProperties(IoTDbDataSource ds, Properties prop){
        Properties properties = (Properties)prop.clone();
        String url = (String)properties.remove(DataSourceFactory.JDBC_URL);
        if(url!=null){
            ds.setUrl(url);
            logger.info("URL set {}",url);
        }

        String user = (String) properties.remove(DataSourceFactory.JDBC_USER);
        ds.setUser(user);
        logger.info("User set {}",user);


        String password = (String) properties.remove(DataSourceFactory.JDBC_PASSWORD);
        ds.setPassword(password);
        logger.info("Password set {}",password);


        logger.info("Remaining properties {}", properties.size());

        if (!properties.isEmpty()) {
            BeanConfig.configure(ds, properties);
        }
    }

    @Override
    public ConnectionPoolDataSource createConnectionPoolDataSource(Properties properties) throws SQLException {
        return null;
    }

    @Override
    public XADataSource createXADataSource(Properties properties) throws SQLException {
        return null;
    }

    @Override
    public Driver createDriver(Properties properties) throws SQLException {
        org.apache.iotdb.jdbc.IoTDBDriver driver = new IoTDBDriver();
        return driver;
    }
}
