package org.ops4j.pax.jdbc.pool.impl.ds;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;

import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;

public class CloseablePoolingDataSource<C extends Connection> extends PoolingDataSource<C> implements Closeable {

    public CloseablePoolingDataSource(ObjectPool<C> pool) {
        super(pool);
    }

    @Override
    public void close() throws IOException {
        getPool().close();
    }

}
