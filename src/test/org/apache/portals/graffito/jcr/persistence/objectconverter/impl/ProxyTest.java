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
package org.apache.portals.graffito.jcr.persistence.objectconverter.impl;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.portals.graffito.jcr.RepositoryLifecycleTestSetup;
import org.apache.portals.graffito.jcr.TestBase;
import org.apache.portals.graffito.jcr.persistence.PersistenceManager;
import org.apache.portals.graffito.jcr.testmodel.proxy.Detail;
import org.apache.portals.graffito.jcr.testmodel.proxy.Main;

/**
 * Test inheritance with node type per concrete class (without  discreminator field)
 *
 * @author <a href="mailto:christophe.lombart@gmail.com">Christophe Lombart</a>
 */
public class ProxyTest extends TestBase {
	private final static Log log = LogFactory.getLog(ProxyTest.class);

	/**
	 * <p>Defines the test case name for junit.</p>
	 * @param testName The test case name.
	 */
	public ProxyTest(String testName) throws Exception {
		super(testName);

	}

	public static Test suite() {
		// All methods starting with "test" will be executed in the test suite.
		return new RepositoryLifecycleTestSetup(new TestSuite(
				ProxyTest.class));
	}

	public void tearDown() throws Exception {

		cleanUpRepisotory();
		super.tearDown();
		
	}

	public void testBeanProxy() {

		try {
			PersistenceManager persistenceManager = this.getPersistenceManager();

			Detail detail = new Detail();
			detail.setField("FieldValue");			
			
			Detail proxyDetail = new Detail();
			proxyDetail.setField("ProxyFieldValue");
			
			Main main = new Main();
			main.setPath("/test");
			main.setDetail(detail);
			main.setProxyDetail(proxyDetail);
							
            persistenceManager.insert(main);
			persistenceManager.save();
			
			
			//---------------------------------------------------------------------------------------------------------
			// Retrieve the main object
			//---------------------------------------------------------------------------------------------------------						

			main = (Main) persistenceManager.getObject(Main.class, "/test");
			assertNotNull("detail is null", main.getDetail());
			assertTrue("Invalid detail bean", main.getDetail().getField().equals("FieldValue"));

			assertNotNull("proxydetail is null", main.getProxyDetail());
			Object proxyObject = main.getProxyDetail();
			assertTrue("Invalid class specify for the proxy bean", proxyObject  instanceof Detail);
			assertTrue("Invalid proxy detail bean",proxyDetail .getField().equals("ProxyFieldValue"));
			
			Detail nullDetail = main.getNullDetail();
			assertNull("nulldetail is not  null",nullDetail );

			
			//---------------------------------------------------------------------------------------------------------
			// Update  
			//---------------------------------------------------------------------------------------------------------						
			 detail = new Detail();
			detail.setField("AnotherFieldValue");			
			
			proxyDetail = new Detail();
			proxyDetail.setField("AnotherProxyFieldValue");
			
			main.setDetail(detail);
			main.setProxyDetail(proxyDetail);
			
			persistenceManager.update(main);
			persistenceManager.save();

			//---------------------------------------------------------------------------------------------------------
			// Retrieve the main object
			//---------------------------------------------------------------------------------------------------------						

			main = (Main) persistenceManager.getObject(Main.class, "/test");
			assertNotNull("detail is null", main.getDetail());
			assertTrue("Invalid detail bean", main.getDetail().getField().equals("AnotherFieldValue"));

			assertNotNull("proxydetail is null", main.getProxyDetail());
			proxyObject = main.getProxyDetail();
			assertTrue("Invalid class specify for the proxy bean", proxyObject  instanceof Detail);
			assertTrue("Invalid proxy detail bean",proxyDetail .getField().equals("AnotherProxyFieldValue"));
						
			assertNull("nulldetail is not  null",main.getNullDetail());
				
	
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
       
	}
	



	    
}