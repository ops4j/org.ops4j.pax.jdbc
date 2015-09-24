/*
 * Copyright 2015 Vincenzo Mazzeo.
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
 package org.ops4j.pax.jdbc.hsqldb.impl;

import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import org.hsqldb.jdbc.JDBCPool;

public class PoolDataSourceWrapper extends JDBCPool implements ConnectionPoolDataSource {

    private static final long serialVersionUID = -2293621933197058388L;

    @Override
    public PooledConnection getPooledConnection() throws SQLException {
        return this.getPooledConnection();
    }

    @Override
    public PooledConnection getPooledConnection(String user, String password) throws SQLException {
        return this.getPooledConnection(user, password);
    }

}
