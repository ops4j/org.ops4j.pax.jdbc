package org.ops4j.pax.jdbc.hook;

import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Register implementations of this interface as an OSGi service to be called by pax-jdbc-config before the 
 * DataSource is published.
 * A possible application is to do a database migration to a newly deployed code version.
 * 
 * The PreHook service must be named using a service property "name". 
 * In the DataSource config the hook to be called must be configured using the property preHook=myname using the name
 * of the registered PerHook service.
 */
public interface PreHook {
    /**
     * Service property key to name a PreHook service
     */
    public static final String KEY_NAME = "name";
    
    /**
     * Config key to refer to a PreHook service
     */
    public static final String CONFIG_KEY_NAME = "ops4j.preHook";
    
    /**
     * Will be called before publishing the DataSource
     * 
     * @param ds
     * @throws Exception in case of exception the DataSource will not be published
     */
    void prepare(DataSource ds) throws SQLException;
}
