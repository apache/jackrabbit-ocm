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
package org.apache.jackrabbit.ocm.manager.enumeration;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.jackrabbit.ocm.DigesterRepositoryTestBase;

/**
 * Test Persisting and retrieving Enum values.
 *
 * @author <a href="mailto:boni.g@bioimagene.com">Boni Gopalan</a>
 */
public class DigesterSimpleEnumerationTest extends DigesterRepositoryTestBase
{

    public static Test suite()
    {
        // All methods starting with "test" will be executed in the test suite.
        return
                new TestSuite(DigesterSimpleEnumerationTest.class);
    }

    public void testMapSimpleEnumeration()
    {
    	try {
			new SimpleEnumerationTestBase(getObjectContentManager()).testMapSimpleEnumeration();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("Tests resulted in exception");
		}
    }

}
