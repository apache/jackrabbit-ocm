/* ========================================================================
 * Copyright 2004 The Apache Software Foundation
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
 * ========================================================================
 */
package org.apache.portals.graffito.jcr;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.portals.graffito.jcr.mapper.DigesterMapperImplTest;
import org.apache.portals.graffito.jcr.query.impl.QueryManagerTest;
import org.apache.portals.graffito.jcr.repository.RepositoryUtilTest;


/**
 * OCM suite definition. Bundles together all independent and package level test suites.
 * 
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class AllTests {

    public static Test suite() throws Exception {
        return new RepositoryLifecycleTestSetup(buildSuite());
    }

    public static Test buildSuite() throws Exception {
        TestSuite suite= new TestSuite("Graffito OCM Tests");
        // individual tests
        suite.addTestSuite(DigesterMapperImplTest.class);
        suite.addTestSuite(RepositoryUtilTest.class);
        suite.addTestSuite(QueryManagerTest.class);
        
        
        // package level tests
        suite.addTest(org.apache.portals.graffito.jcr.persistence.objectconverter.impl.AllTests.buildSuite());
        suite.addTest(org.apache.portals.graffito.jcr.persistence.atomicconverter.AllTests.buildSuite());
        suite.addTest(org.apache.portals.graffito.jcr.persistence.collectionconverter.impl.AllTests.buildSuite());
        suite.addTest(org.apache.portals.graffito.jcr.persistence.impl.AllTests.buildSuite());

        return suite;
    }
}
