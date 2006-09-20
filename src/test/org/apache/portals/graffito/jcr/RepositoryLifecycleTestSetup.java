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

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.apache.portals.graffito.jcr.repository.RepositoryUtil;
/**
 * A TestSetup that opens/close the JCR repository.
 * 
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class RepositoryLifecycleTestSetup extends TestSetup {
    public RepositoryLifecycleTestSetup(Test test) {
        super(test);
    }

    /**
     * @see junit.extensions.TestSetup#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("registering repository ... ");
        RepositoryUtil.registerRepository("repositoryTest", 
                "./src/test-config/repository-derby.xml", "./target/repository");
    }

    /**
     * @see junit.extensions.TestSetup#tearDown()
     */
    protected void tearDown() throws Exception {
        RepositoryUtil.unRegisterRepository("repositoryTest");
        System.out.println("repository shutdown");
        super.tearDown();
    }
    
    
}
