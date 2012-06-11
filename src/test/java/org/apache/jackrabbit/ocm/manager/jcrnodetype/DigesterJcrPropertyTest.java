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
package org.apache.jackrabbit.ocm.manager.jcrnodetype;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.jackrabbit.ocm.DigesterRepositoryTestBase;
import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.apache.jackrabbit.ocm.testmodel.OcmTestProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test inheritance with node type per concrete class (without  discreminator field)
 *
 * @author <a href="mailto:christophe.lombart@gmail.com">Christophe Lombart</a>
 */
public class DigesterJcrPropertyTest extends DigesterRepositoryTestBase {
	private final static Logger log = LoggerFactory.getLogger(DigesterJcrPropertyTest.class);


	public static Test suite() {
		// All methods starting with "test" will be executed in the test suite.
		return new TestSuite(DigesterJcrPropertyTest.class);
	}

	public void testRequiredProperty()
	{

		try
		{
			ObjectContentManager ocm = getObjectContentManager();
			//---------------------------------------------------------------------------------------------------------
			// Insert without the mandatory field
			//---------------------------------------------------------------------------------------------------------			
			
            OcmTestProperty ocmTestProperty = new OcmTestProperty();
            ocmTestProperty.setPath("/test");
            ocmTestProperty.setRequiredProp("requiredPropValue");
            ocmTestProperty.setRequiredWithConstraintsProp("abc");

            try
            {
                 ocm.insert(ocmTestProperty);
                 fail("Incorrect insert operation - the mandatory fields have no value");
            }
            catch(Exception e)
            {
               // Normal behaviour 	
            	ocm.refresh(false);
            }

			//---------------------------------------------------------------------------------------------------------
			// Insert with the mandatory fields
			//---------------------------------------------------------------------------------------------------------			
            ocmTestProperty.setMandatoryProp("mandatoryValue");
            ocmTestProperty.setMandatoryWithConstaintsProp("xx");
            ocm.insert(ocmTestProperty);
            ocm.save();

			//---------------------------------------------------------------------------------------------------------
			// Retrieve
			//---------------------------------------------------------------------------------------------------------			
            ocmTestProperty = (OcmTestProperty) ocm.getObject("/test");
            assertTrue("Invalid required property", ocmTestProperty.getRequiredProp().equals("requiredPropValue"));
            assertTrue("Invalid required property with constraints", ocmTestProperty.getRequiredWithConstraintsProp().equals("abc"));
            assertTrue("Invalid autocreated property", ocmTestProperty.getAutoCreatedProp().equals("aaa"));
            assertTrue("Invalid autocreated property", ocmTestProperty.getAutoCreatedWithConstraintsProp().equals("ccc"));
            assertTrue("Invalid protected property", ocmTestProperty.getProtectedWithDefaultValueProp().equals("protectedValue"));

            //---------------------------------------------------------------------------------------------------------
			// update the property requiredWithConstraintsProp with bad value
			//---------------------------------------------------------------------------------------------------------			
            ocmTestProperty = (OcmTestProperty) ocm.getObject("/test");
            ocmTestProperty.setRequiredWithConstraintsProp("invalid value");
            try
            {
            	ocm.update(ocmTestProperty);
            	ocm.save();
            	fail("Invalid value was accepted for requiredWithConstraintsProp");
            }
            catch(Exception e)
            {                	
               // Do nothing - normal behaviour, the value               	
            }

            //---------------------------------------------------------------------------------------------------------
			// update the property AutoCreatedWithConstraintsProp with bad value
			//---------------------------------------------------------------------------------------------------------			
            ocmTestProperty = (OcmTestProperty) ocm.getObject("/test");
            ocmTestProperty.setAutoCreatedWithConstraintsProp("invalid value");
            try
            {
            	ocm.update(ocmTestProperty);
            	ocm.save();
            	fail("Invalid value was accepted for autoCreatedWithConstraintsProp ");
            }
            catch(Exception e)
            {             	
               // Do nothing - normal behaviour, the value is not valid
               	
            }

            //---------------------------------------------------------------------------------------------------------
			// update the property mandatoryWithConstaintsProp with bad value
			//---------------------------------------------------------------------------------------------------------			
            ocmTestProperty = (OcmTestProperty) ocm.getObject("/test");
            ocmTestProperty.setMandatoryWithConstaintsProp("yy");
            try
            {
            	ocm.update(ocmTestProperty);
            	ocm.save();
            	fail("Invalid value was accepted for mandatoryWithConstaintsProp");
            }
            catch(Exception e)
            {
                // expected;
            }
			
			
		}
		catch (Exception e)
		{			
			log.error("testRequiredProperty failed", e);
			fail();
		}
			
	}
	
	
	
}