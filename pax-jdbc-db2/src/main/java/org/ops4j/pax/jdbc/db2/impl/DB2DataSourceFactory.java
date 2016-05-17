package org.ops4j.pax.jdbc.db2.impl;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.CommonDataSource;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.osgi.service.jdbc.DataSourceFactory;

public class DB2DataSourceFactory implements DataSourceFactory {
	
    private static final String DB2_DATASOURCE_CLASS = "com.ibm.db2.jcc.DB2SimpleDataSource";
    private static final String DB2_CONNECTIONPOOL_DATASOURCE_CLASS = "com.ibm.db2.jcc.DB2ConnectionPoolDataSource";
    private static final String DB2_XA_DATASOURCE_CLASS = "com.ibm.db2.jcc.DB2XADataSource";
    private static final String DB2_DRIVER_CLASS = "com.ibm.db2.jcc.DB2Driver";

    private final Class<?> db2DataSourceClass;
    private final Class<?> db2ConnectionPoolDataSourceClass;
    private final Class<?> db2XaDataSourceClass;
    private final Class<?> db2DriverClass;

    public DB2DataSourceFactory() throws ClassNotFoundException {
    	ClassLoader classLoader = DB2DataSourceFactory.class.getClassLoader();
    	this.db2DataSourceClass = classLoader.loadClass(DB2_DATASOURCE_CLASS);
    	this.db2ConnectionPoolDataSourceClass = classLoader.loadClass(DB2_CONNECTIONPOOL_DATASOURCE_CLASS);
    	this.db2XaDataSourceClass=classLoader.loadClass(DB2_XA_DATASOURCE_CLASS);
    	this.db2DriverClass = classLoader.loadClass(DB2_DRIVER_CLASS);
    }
    
    @Override
    public DataSource createDataSource(Properties props) throws SQLException {
        try {
            return setProperties(db2DataSourceClass.newInstance(), props);
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
    }
	
	@Override
	public ConnectionPoolDataSource createConnectionPoolDataSource(
			Properties props) throws SQLException {
        try {
            return setProperties(db2ConnectionPoolDataSourceClass.newInstance(), props);
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
	}

	@Override
	public XADataSource createXADataSource(Properties props)
			throws SQLException {
        try {
            return setProperties(db2XaDataSourceClass.newInstance(), props);
        }
        catch (Exception ex) {
            throw new SQLException(ex);
        }
	}

	@Override
	public Driver createDriver(Properties props) throws SQLException {
        try {
            return Driver.class.cast(db2DriverClass.newInstance());
        }
        catch (InstantiationException ex) {
            throw new SQLException(ex);
        }
        catch (IllegalAccessException ex) {
            throw new SQLException(ex);
        }
	}
	
	  @SuppressWarnings("unchecked")
	    private <T> T setProperties(Object dataSourceInstance, Properties properties) throws Exception {
		  	Properties props = (Properties) properties.clone();
		  	
	        String url = (String) props.remove(DataSourceFactory.JDBC_URL);
	        if (url != null) {
	        	setProperty(url, dataSourceInstance, "setURL");
	        }
	        
	        String databaseName = (String) props.remove(DataSourceFactory.JDBC_DATABASE_NAME);
	        if (databaseName == null && url == null) {
	            throw new SQLException("missing required property "+ DataSourceFactory.JDBC_DATABASE_NAME);
	        }
	        
	        setProperty(databaseName, dataSourceInstance,"setDatabaseName");
	        
	        
	        String serverName = (String) props.remove(DataSourceFactory.JDBC_SERVER_NAME);
	        setProperty(serverName, dataSourceInstance,"setServerName");
	        
	        String portNumber = (String) props.remove(DataSourceFactory.JDBC_PORT_NUMBER);
	        if (portNumber != null) {
	        	setIntProperty(portNumber, dataSourceInstance,"setPortNumber");
	        }
	        
	        String user = (String) props.remove(DataSourceFactory.JDBC_USER);
	        setProperty(user, dataSourceInstance, "setUser");
	        
	        String password = (String) props.remove(DataSourceFactory.JDBC_PASSWORD);
	        setProperty(password, dataSourceInstance,"setPassword");
	        return (T) dataSourceInstance;
	    }

	    private void setProperty(String value, Object instance, String methodName) throws Exception {
	        if (value != null) {
	            instance.getClass().getMethod(methodName, String.class).invoke(instance, value);
	        }
	    }
	    
	    private void setIntProperty(String value, Object instance, String methodName) throws Exception {
	        int iValue = new Integer(value);
	        instance.getClass().getMethod(methodName, int.class).invoke(instance, iValue);
	    }

}
