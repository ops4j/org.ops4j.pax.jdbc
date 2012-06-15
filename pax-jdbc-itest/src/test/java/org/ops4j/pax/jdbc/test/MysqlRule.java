package org.ops4j.pax.jdbc.test;

import org.junit.Assume;
import org.junit.rules.ExternalResource;

public class MysqlRule extends ExternalResource
{
    @Override
    protected void before() throws Throwable
    {
        Assume.assumeTrue( TestConfiguration.isMysqlAvailable() );
    }
}
