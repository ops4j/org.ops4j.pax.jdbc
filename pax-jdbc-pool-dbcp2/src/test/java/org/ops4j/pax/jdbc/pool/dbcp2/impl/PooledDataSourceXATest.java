/*
 * Copyright 2021 OPS4J.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.jdbc.pool.dbcp2.impl;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.xa.XAResource;

import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.common.util.propertyservice.PropertiesFactory;
import org.jboss.narayana.osgi.jta.internal.OsgiTransactionManager;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.ops4j.pax.jdbc.oracle.impl.OracleDataSourceFactory;
import org.osgi.service.jdbc.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;

public class PooledDataSourceXATest {

    public static final Logger LOG = LoggerFactory.getLogger(PooledDataSourceXATest.class);

    // javax.transaction API
    private UserTransaction ut = null;
    private TransactionManager tm = null;

    /*
        A bit unofficial
        $ podman run -itd --name pax.jdbc.oracle -p 1521:1521 --privileged=true oracleinanutshell/oracle-xe-11g
     */

    @Rule
    public ExternalResource resource = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            try (Socket socket = new Socket()) {
                InetSocketAddress endpoint = new InetSocketAddress("localhost", 1521);
                socket.connect(endpoint, (int) TimeUnit.SECONDS.toMillis(5));
                Assume.assumeTrue(true);
            } catch (Exception ex) {
                Assume.assumeTrue(false);
            }
        }

        @Override
        protected void after() {
        }
    };

    @Before
    public void initialization() throws Exception {
        DataSourceFactory factory = new OracleDataSourceFactory();
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_URL, "jdbc:oracle:thin:@localhost:1521:xe");
        props.setProperty(DataSourceFactory.JDBC_USER, "system");
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, "oracle");
        DataSource ds = factory.createDataSource(props);
        try (Connection con = ds.getConnection()) {
            try (Statement st = con.createStatement()) {
                try {
                    st.execute("CREATE USER admin1 IDENTIFIED BY tiger");
                } catch (SQLException ignore) {
                }
                try {
                    st.execute("GRANT CONNECT, RESOURCE to admin1");
                } catch (SQLException ignore) {
                }
                try {
                    st.execute("CREATE USER user1 IDENTIFIED BY password1");
                } catch (SQLException ignore) {
                }
                try {
                    st.execute("GRANT CONNECT, RESOURCE to user1");
                } catch (SQLException ignore) {
                }
                try {
                    st.execute("CREATE USER user2 IDENTIFIED BY password2");
                } catch (SQLException ignore) {
                }
                try {
                    st.execute("GRANT CONNECT, RESOURCE to user2");
                } catch (SQLException ignore) {
                }
            }
        }

        factory = new OracleDataSourceFactory();
        props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_URL, "jdbc:oracle:thin:@localhost:1521:xe");
        props.setProperty(DataSourceFactory.JDBC_USER, "admin1");
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, "tiger");
        ds = factory.createDataSource(props);
        try (Connection con = ds.getConnection()) {
            try (Statement st = con.createStatement()) {
                try {
                    st.execute("CREATE SEQUENCE events_seq START WITH 1");
                    st.execute("CREATE TABLE events (id INTEGER NOT NULL PRIMARY KEY, name VARCHAR2(128))");
                    st.execute("GRANT SELECT ON events TO user1");
                    st.execute("GRANT INSERT ON events TO user1");
                    st.execute("GRANT SELECT ON events TO user2");
                    st.execute("GRANT INSERT ON events TO user2");
                    st.execute("GRANT SELECT ON events_seq TO user1");
                    st.execute("GRANT SELECT ON events_seq TO user2");
                } catch (SQLException e) {
//                    LOG.warn(e.getMessage(), e);
                }
                try {
                    st.execute("DELETE FROM events");
                    st.execute("INSERT INTO events (id, name) VALUES (events_seq.nextval, 'Initial Event')");
                } catch (SQLException e) {
                    LOG.warn(e.getMessage(), e);
                }
            }
        }

        Properties properties = PropertiesFactory.getDefaultProperties();
        properties.setProperty("com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean.recoveryBackoffPeriod", "1");

        // there are 3 stores actually
        properties.setProperty("com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.objectStoreType", "com.arjuna.ats.internal.arjuna.objectstore.ShadowNoFileLockStore");
        properties.setProperty("com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.objectStoreDir", "target/tx");
        properties.setProperty("com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.localOSRoot", "defaultStore");
        properties.setProperty("com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.communicationStore.objectStoreType", "com.arjuna.ats.internal.arjuna.objectstore.ShadowNoFileLockStore");
        properties.setProperty("com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.communicationStore.objectStoreDir", "target/tx");
        properties.setProperty("com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.communicationStore.localOSRoot", "communicationStore");
        properties.setProperty("com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.stateStore.objectStoreType", "com.arjuna.ats.internal.arjuna.objectstore.ShadowNoFileLockStore");
        properties.setProperty("com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.stateStore.objectStoreDir", "target/tx");
        properties.setProperty("com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.stateStore.localOSRoot", "stateStore");

        // Arjuna/Narayana objects
        JTAEnvironmentBean env = jtaPropertyManager.getJTAEnvironmentBean();
        OsgiTransactionManager tmimpl = new OsgiTransactionManager();
        env.setUserTransaction(tmimpl);
        env.setTransactionManager(tmimpl);

        // javax.transaction API
        ut = tmimpl;
        tm = tmimpl;
    }

    @Test
    public void jtaTest() throws Exception {
        DataSourceFactory factory1 = new OracleDataSourceFactory();
        Properties props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_URL, "jdbc:oracle:thin:user1/@localhost:1521:xe");
        props.setProperty(DataSourceFactory.JDBC_USER, "user1");
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, "password1");
        XADataSource ds1 = factory1.createXADataSource(props);

        DataSourceFactory factory2 = new OracleDataSourceFactory();
        props = new Properties();
        props.setProperty(DataSourceFactory.JDBC_URL, "jdbc:oracle:thin:user2/@localhost:1521:xe");
        props.setProperty(DataSourceFactory.JDBC_USER, "user2");
        props.setProperty(DataSourceFactory.JDBC_PASSWORD, "password2");
        XADataSource ds2 = factory2.createXADataSource(props);

        ut.begin();
        Transaction tx = tm.getTransaction();

        XAConnection con1 = ds1.getXAConnection();
        XAConnection con2 = ds2.getXAConnection();
        XAResource xaResource1 = con1.getXAResource();
        XAResource xaResource2 = con2.getXAResource();
        assertFalse(xaResource1.isSameRM(xaResource2));
        tx.enlistResource(xaResource1);
        tx.enlistResource(xaResource2);

        try (Connection con = con1.getConnection()) {
            try (Statement st = con.createStatement()) {
                st.execute("INSERT INTO admin1.events (id, name) VALUES (admin1.events_seq.nextval, 'ev 1')");
            }
        }

        try (Connection con = con2.getConnection()) {
            try (Statement st = con.createStatement()) {
                st.execute("INSERT INTO admin1.events (id, name) VALUES (admin1.events_seq.nextval, 'ev 2')");
            }
        }

        ut.commit();

        try (Connection con = ds1.getXAConnection().getConnection()) {
            DatabaseMetaData md = con.getMetaData();
            LOG.info("DB: {}/{}", md.getDatabaseProductName(), md.getDatabaseProductVersion());

            try (Statement st = con.createStatement()) {
                try (ResultSet rs = st.executeQuery("select id, name from admin1.events order by id asc")) {
                    while (rs.next()) {
                        LOG.info("{} | {}", rs.getInt(1), rs.getString(2));
                    }
                }
            }
        }
        try (Connection con = ds2.getXAConnection().getConnection()) {
            DatabaseMetaData md = con.getMetaData();
            LOG.info("DB: {}/{}", md.getDatabaseProductName(), md.getDatabaseProductVersion());

            try (Statement st = con.createStatement()) {
                try (ResultSet rs = st.executeQuery("select id, name from admin1.events order by id asc")) {
                    while (rs.next()) {
                        LOG.info("{} | {}", rs.getInt(1), rs.getString(2));
                    }
                }
            }
        }

    }

}
