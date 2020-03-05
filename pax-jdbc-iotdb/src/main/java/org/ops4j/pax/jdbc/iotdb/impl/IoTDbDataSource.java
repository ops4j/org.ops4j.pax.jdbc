package org.ops4j.pax.jdbc.iotdb.impl;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import org.apache.iotdb.jdbc.*;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoTDbDataSource implements DataSource {

    private final Logger logger = LoggerFactory.getLogger(IoTDbDataSource.class);

    private String url;
    private String user;
    private String password;
    private Properties properties;
    private Integer port = 6667;

    public IoTDbDataSource() {
        properties = new Properties(2);
    }

    public IoTDbDataSource(String url, String user, String password, Integer port) {
        this.url = url;
        this.properties = new Properties(5);
        properties.setProperty("user",user);
        properties.setProperty("password",password);
        if(port!=0) {
            this.port = port;
        }
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
        properties.setProperty("user",user);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        properties.setProperty("password",password);
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            return new IoTDBConnection(url, properties);
        } catch (TTransportException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
       try {
           Properties newProp = new Properties(2);
           newProp.setProperty("user",username);
           newProp.setProperty("password",password);
           return new IoTDBConnection(url, newProp);
       }
       catch (Exception e){
           e.printStackTrace();
       }
       return null;
    }


    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter printWriter) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int i) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> aClass) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return false;
    }
}
