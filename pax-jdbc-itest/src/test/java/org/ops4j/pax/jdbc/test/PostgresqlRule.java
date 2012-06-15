package org.ops4j.pax.jdbc.test;

import org.junit.Assume;
import org.junit.rules.ExternalResource;

public class PostgresqlRule extends ExternalResource
{
    @Override
    protected void before() throws Throwable
    {
        Assume.assumeTrue( TestConfiguration.isPostgresqlAvailable() );
    }
}
