/*
 * Copyright 2012 Harald Wellmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ops4j.pax.jdbc.sqlite.impl;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.osgi.service.jdbc.DataSourceFactory;
import org.sqlite.JDBC;
import org.sqlite.SQLiteDataSource;

public class SqliteDataSourceFactory implements DataSourceFactory
{

  @Override
  public DataSource createDataSource(Properties props) throws SQLException
  {
    SQLiteDataSource dataSource = new SQLiteDataSource();
    String url = props.getProperty(JDBC_URL);
    if (url == null)
      dataSource.setUrl("jdbc:sqlite:" + props.getProperty(JDBC_DATABASE_NAME));
    else
      dataSource.setUrl(url);
    return dataSource;
  }

  @Override
  public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public XADataSource createXADataSource(Properties props) throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Driver createDriver(Properties props) throws SQLException
  {
    JDBC driver = new JDBC();
    return driver;
  }
}
