package org.ops4j.pax.jdbc.test.config;

import java.util.Dictionary;
import java.util.Hashtable;

import org.ops4j.pax.jdbc.hook.PreHook;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class MyPreHookActivator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(PreHook.KEY_NAME, "myprehook");
        context.registerService(PreHook.class, new MyPreHook(), props);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

}
