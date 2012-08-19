package org.ops4j.pax.jdbc.test.postgresql;

import org.junit.Assume;
import org.junit.rules.ExternalResource;
import org.ops4j.pax.jdbc.test.TestConfiguration;

public class PostgresqlRule extends ExternalResource
{
    @Override
    protected void before() throws Throwable
    {
        Assume.assumeTrue( TestConfiguration.isPostgresqlAvailable() );
    }
}
