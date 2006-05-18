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
package org.apache.portals.graffito.jcr.persistence.impl;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.portals.graffito.jcr.RepositoryLifecycleTestSetup;
import org.apache.portals.graffito.jcr.TestBase;
import org.apache.portals.graffito.jcr.persistence.PersistenceManager;
import org.apache.portals.graffito.jcr.testmodel.Atomic;
import org.apache.portals.graffito.jcr.testmodel.inheritance.impl.DocumentImpl;
import org.apache.portals.graffito.jcr.testmodel.inheritance.impl.DocumentStream;
import org.apache.portals.graffito.jcr.testmodel.inheritance.impl.FolderImpl;
import org.apache.portals.graffito.jcr.testmodel.interfaces.Document;
import org.apache.portals.graffito.jcr.testmodel.interfaces.Folder;

/**
 * Test autoupdate setting
 *
 * @author <a href="mailto:christophe.lombart@gmail.com">Christophe Lombart</a>
 */
public class PersistenceManagerAutoUpdateTest extends TestBase {
	private final static Log log = LogFactory.getLog(PersistenceManagerAutoUpdateTest.class);

	/**
	 * <p>Defines the test case name for junit.</p>
	 * @param testName The test case name.
	 */
	public PersistenceManagerAutoUpdateTest(String testName) throws Exception {
		super(testName);

	}

	public static Test suite() {
		// All methods starting with "test" will be executed in the test suite.
		return new RepositoryLifecycleTestSetup(new TestSuite(
				PersistenceManagerAutoUpdateTest.class));
	}

	public void tearDown() throws Exception {

		cleanUpRepisotory();
		super.tearDown();
		
	}

	
	public void testAutoUpdate() {
		PersistenceManager persistenceManager = this.getPersistenceManager();

		//---------------------------------------------------------------------------------------------------------
		// Insert cmsobjects
		//---------------------------------------------------------------------------------------------------------			
	    Document document = new DocumentImpl();        
	    document.setName("document4");
	    document.setContentType("plain/text"); 
	    DocumentStream documentStream = new DocumentStream();
	    documentStream.setEncoding("utf-8");
	    documentStream.setContent("Test Content 4".getBytes());
	    document.setDocumentStream(documentStream);       

	    Folder subFolder = new FolderImpl();
	    subFolder.setName("subfolder");
	    
	    Folder  folder = new FolderImpl();
	    folder.setPath("/folder2");
	    folder.setName("folder2");        
	    folder.addChild(document);
	    folder.addChild(subFolder);
	    persistenceManager.insert(folder);               		
		persistenceManager.save();
		
		//---------------------------------------------------------------------------------------------------------	
		// Retrieve folder2 
		//---------------------------------------------------------------------------------------------------------	
		Folder folder2 = (Folder) persistenceManager.getObject( "/folder2");
		assertNotNull("folder 2 is null", folder2);
		assertEquals("Invalid number of cms object  found in folder2 children", folder2.getChildren().size() ,2);
		assertTrue("Invalid item in the collection", this.contains(folder2.getChildren(), "/folder2/document4", DocumentImpl.class));
		assertTrue("Invalid item in the collection", this.contains(folder2.getChildren(), "/folder2/subfolder", FolderImpl.class));		
		
		//---------------------------------------------------------------------------------------------------------	
		// Update  folder2 
		//---------------------------------------------------------------------------------------------------------	
		folder2.setChildren(null);  // This  modification should be ignored because the field children has autoUpdate = false
		persistenceManager.update(folder2);
		persistenceManager.save();

		
		//---------------------------------------------------------------------------------------------------------	
		// Retrieve folder2 
		//---------------------------------------------------------------------------------------------------------	
		 folder2 = (Folder) persistenceManager.getObject( "/folder2");
		assertNotNull("folder 2 is null", folder2);
		assertEquals("Invalid number of cms object  found in folder2 children", folder2.getChildren().size() ,2);
		assertTrue("Invalid item in the collection", this.contains(folder2.getChildren(), "/folder2/document4", DocumentImpl.class));
		assertTrue("Invalid item in the collection", this.contains(folder2.getChildren(), "/folder2/subfolder", FolderImpl.class));		
		
		//---------------------------------------------------------------------------------------------------------	
		// Update  folder2 
		//---------------------------------------------------------------------------------------------------------	
		folder2.setChildren(null);  // This  modification should be ignored because the field children has autoUpdate = false
		persistenceManager.save();
		
	}
	
}