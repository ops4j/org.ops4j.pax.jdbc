package org.ops4j.pax.jdbc.pool.dbcp2.impl.ds;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;

import org.apache.commons.dbcp2.managed.ManagedDataSource;
import org.apache.commons.dbcp2.managed.TransactionRegistry;
import org.apache.commons.pool2.ObjectPool;

public class CloseableManagedDataSource<C extends Connection> extends ManagedDataSource<C>
    implements Closeable {

    public CloseableManagedDataSource(ObjectPool<C> pool, TransactionRegistry transactionRegistry) {
        super(pool, transactionRegistry);
    }

    @Override
    public void close() throws IOException {
        getPool().close();
    }

}
