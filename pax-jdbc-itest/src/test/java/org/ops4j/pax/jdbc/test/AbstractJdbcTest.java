/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ops4j.pax.jdbc.test;

import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.when;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public abstract class AbstractJdbcTest {

    @Inject
    protected BundleContext context;

    private boolean equinoxConsole;

    protected void assertAllBundlesResolved() {
        for (Bundle bundle : context.getBundles()) {
            if (bundle.getState() == Bundle.INSTALLED) {
                // Provoke exception
                try {
                    bundle.start();
                } catch (BundleException e) {
                    Assert.fail(e.getMessage());
                }
            }
        }
    }

    protected MavenArtifactProvisionOption mvnBundle(String groupId, String artifactId) {
        return mavenBundle(groupId, artifactId).versionAsInProject();
    }

    protected Bundle getBundle(String symbolicName) {
        for (Bundle bundle : context.getBundles()) {
            if (bundle.getSymbolicName().equals(symbolicName)) {
                return bundle;
            }
        }
        return null;
    }

    public Option regressionDefaults() {
        return composite //
        (
         // add SLF4J and logback bundles .. 1.7.0 is needed for aries transaction
         mavenBundle("org.slf4j", "slf4j-api").version("1.7.0").startLevel(START_LEVEL_SYSTEM_BUNDLES),
         mavenBundle("ch.qos.logback", "logback-core").versionAsInProject()
             .startLevel(START_LEVEL_SYSTEM_BUNDLES),
         mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject()
             .startLevel(START_LEVEL_SYSTEM_BUNDLES),
         systemProperty("pax.exam.osgi.unresolved.fail").value("true"),
         // Set logback configuration via system property.
         // This way, both the driver and the container use the same configuration
         systemProperty("logback.configurationFile")
             .value("file:" + PathUtils.getBaseDir() + "/src/test/resources/logback.xml"),
         when(equinoxConsole).useOptions(systemProperty("osgi.console").value("6666")), junitBundles(),
         mvnBundle("org.osgi", "org.osgi.service.jdbc"), //
         mvnBundle("org.apache.felix", "org.apache.felix.configadmin")
        );
    }

    public Option poolDefaults() {
        return composite //
        (//
         systemPackage("javax.transaction;version=1.1.0"),
         systemPackage("javax.transaction.xa;version=1.1.0"),
         mvnBundle("org.ops4j.pax.jdbc", "pax-jdbc-pool-common"),
         mvnBundle("org.apache.geronimo.specs", "geronimo-validation_1.0_spec"),
         mvnBundle("org.apache.aries", "org.apache.aries.util"),
         mvnBundle("org.apache.aries.transaction", "org.apache.aries.transaction.manager") //
        );
    }

}
