/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.ocm;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.apache.jackrabbit.ocm.manager.impl.ObjectContentManagerImpl;

/**
 * Base class for testcases. Provides priviledged access to the jcr test
 * repository.
 *
 * @author <a href="mailto:christophe.lombart@gmail.com">Christophe Lombart</a>
 *
 *
 */
public abstract class DigesterRepositoryTestBase extends AbstractRepositoryTestBase
{

    @Override
    protected ObjectContentManager createObjectContentManager(Session session) throws RepositoryException
    {
		String[] files = { "./src/test/test-config/jcrmapping.xml",
						   "./src/test/test-config/jcrmapping-proxy.xml",
						   "./src/test/test-config/jcrmapping-atomic.xml",
                           "./src/test/test-config/jcrmapping-default.xml",
                           "./src/test/test-config/jcrmapping-beandescriptor.xml",
                           "./src/test/test-config/jcrmapping-inheritance.xml",
                           "./src/test/test-config/jcrmapping-jcrnodetypes.xml",
                           "./src/test/test-config/jcrmapping-uuid.xml",
                           "./src/test/test-config/jcrmapping-complex-collections.xml",
                           "./src/test/test-config/jcrmapping-Enum.xml"
		};

	    return new ObjectContentManagerImpl(session, files);
		
	}

}
