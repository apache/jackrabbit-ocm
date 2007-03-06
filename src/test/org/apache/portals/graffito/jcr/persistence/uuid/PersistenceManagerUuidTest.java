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
package org.apache.portals.graffito.jcr.persistence.uuid;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.portals.graffito.jcr.RepositoryLifecycleTestSetup;
import org.apache.portals.graffito.jcr.TestBase;
import org.apache.portals.graffito.jcr.persistence.PersistenceManager;
import org.apache.portals.graffito.jcr.testmodel.uuid.A;
import org.apache.portals.graffito.jcr.testmodel.uuid.B;


/**
 * Test JcrSession
 *
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Christophe Lombart</a>
 */
public class PersistenceManagerUuidTest extends TestBase
{
    private final static Log log = LogFactory.getLog(PersistenceManagerUuidTest.class);

    /**
     * <p>Defines the test case name for junit.</p>
     * @param testName The test case name.
     */
    public PersistenceManagerUuidTest(String testName)  throws Exception
    {
        super(testName);
    }

    public static Test suite()
    {
        // All methods starting with "test" will be executed in the test suite.
        return new RepositoryLifecycleTestSetup(
                new TestSuite(PersistenceManagerUuidTest.class));
    }


    /**
     * @see junit.framework.TestCase#tearDown()
     */
    public void tearDown() throws Exception
    {
    	if (getPersistenceManager().objectExists("/testB"))
    	{
    	   getPersistenceManager().remove("/testB");
    	   getPersistenceManager().save();
    	}
    	
    	if (getPersistenceManager().objectExists("/test"))
    	{
    	   getPersistenceManager().remove("/test");
    	   getPersistenceManager().save();
    	}
    	
        super.tearDown();
    }
    
    public void testClassA()
    {
        try
        {
        	PersistenceManager persistenceManager = getPersistenceManager();


            // --------------------------------------------------------------------------------
            // Create and store an object A in the repository
            // --------------------------------------------------------------------------------
            A a = new A();
            a.setPath("/test");
            a.setStringData("testdata");
            persistenceManager.insert(a);
            persistenceManager.save();           

            // --------------------------------------------------------------------------------
            // Get the object
            // --------------------------------------------------------------------------------           
            a = (A) persistenceManager.getObject( "/test");
            assertNotNull("a is null", a);
            String uuidA = a.getUuid();
            assertNotNull("uuid is null", uuidA);
            System.out.println("UUID : " + uuidA);
            
            // --------------------------------------------------------------------------------
            // Update the object
            // --------------------------------------------------------------------------------
            a.setStringData("testdata2");
            persistenceManager.update(a);
            persistenceManager.save();

            // --------------------------------------------------------------------------------
            // Get the object
            // --------------------------------------------------------------------------------           
            a = (A) persistenceManager.getObject("/test");
            assertNotNull("a is null", a);
            assertTrue("The uuid has been modified", uuidA.equals(a.getUuid()));
            
            // --------------------------------------------------------------------------------
            // Create and store an object B in the repository which has a reference on A
            // --------------------------------------------------------------------------------
            B b = new B();
            b.setReference2A(uuidA);
            b.setPath("/testB");
            persistenceManager.insert(b);
            persistenceManager.save();
            
            // --------------------------------------------------------------------------------
            // Update the object B with an invalid reference 
            // --------------------------------------------------------------------------------
            b = (B) persistenceManager.getObject("/testB");
            b.setReference2A("1245");
            try
            {
            	persistenceManager.update(b);            	
            	fail("Exception not throw");
            }
            catch(Exception e)
            {
            	//Exception has to triggered due to an invalid uuid
            	System.out.println("Invalid uuid : " + e);
            	
            }
            
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Exception occurs during the unit test : " + e);
        }
        
    }
    
        

}