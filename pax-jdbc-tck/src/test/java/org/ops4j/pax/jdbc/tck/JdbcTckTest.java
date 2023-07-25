/*
 * Copyright 2022 OPS4J.
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
package org.ops4j.pax.jdbc.tck;

import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.cases.jdbc.junit.JDBCTestCase;

import de.laeubisoft.osgi.junit5.framework.annotations.WithBundle;
import de.laeubisoft.osgi.junit5.framework.extension.FrameworkExtension;

//#### JDBC services to test ####
//some drivers that already have a custom implementation and we just want to include them to make sure they comply
@WithBundle(value = "com.h2database", start = true, isolated = true)
//pax-jdbc-sqlite
@WithBundle(value = "org.ops4j.pax.jdbc.sqlite", start = true, isolated = true)
@WithBundle(value = "org.xerial.sqlite-jdbc", start = true, isolated = true)
//pax-jdbc-mariadb --> actually obsolete see: https://github.com/ops4j/org.ops4j.pax.jdbc/issues/277
@WithBundle(value = "org.ops4j.pax.jdbc.mariadb", start = true, isolated = true)
@WithBundle(value = "mariadb-java-client", start = true, isolated = true)
//pax-db2 TODO how to test? seems not OpenSource?
// @WithBundle(value = "org.ops4j.pax.jdbc.db2", start = true, isolated = true)

//### basic setup ###
@ExtendWith(FrameworkExtension.class)
@WithBundle("org.osgi.service.jdbc")
public class JdbcTckTest extends JDBCTestCase {

}
