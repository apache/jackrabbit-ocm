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
package org.apache.jackrabbit.ocm.manager.auto;

//import javax.jcr.Repository;
//import javax.jcr.UnsupportedRepositoryOperationException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.jackrabbit.ocm.DigesterRepositoryTestBase;
import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.apache.jackrabbit.ocm.manager.impl.ObjectContentManagerImpl;
import org.apache.jackrabbit.ocm.testmodel.auto.Document;
import org.apache.jackrabbit.ocm.testmodel.auto.Folder;
import org.apache.jackrabbit.ocm.testmodel.auto.impl.DocumentImpl;
import org.apache.jackrabbit.ocm.testmodel.auto.impl.DocumentStream;
import org.apache.jackrabbit.ocm.testmodel.auto.impl.FolderImpl;

/**
 * Test autoupdate setting
 *
 * @author <a href="mailto:christophe.lombart@gmail.com">Christophe Lombart</a>
 */
public class DigesterAutoTest extends DigesterRepositoryTestBase {

	public static Test suite() {
		// All methods starting with "test" will be executed in the test suite.
		return new TestSuite(
				DigesterAutoTest.class);
	}

	
	public void testAuto() {
		
		ObjectContentManager ocm = this.getObjectContentManager();

		//---------------------------------------------------------------------------------------------------------
		// Insert cmsobjects
		//---------------------------------------------------------------------------------------------------------
	    Folder  folder = new FolderImpl();
	    folder.setPath("/folder2");
	    folder.setName("folder2");
		
	    Document document = new DocumentImpl();
	    document.setPath("/folder2/document4");
	    document.setName("document4");
	    document.setContentType("plain/text");
	    DocumentStream documentStream = new DocumentStream();
	    documentStream.setEncoding("utf-8");
	    documentStream.setContent("Test Content 4".getBytes());
	    document.setDocumentStream(documentStream);

	    Folder subFolder = new FolderImpl();
	    subFolder.setName("subfolder");
	    subFolder.setPath("/folder2/subfolder");
	    	    	
	    folder.addChild(document);
	    folder.addChild(subFolder);
	    ocm.insert(folder);               		
		ocm.save();
		
		//---------------------------------------------------------------------------------------------------------	
		// Retrieve folder2
		//---------------------------------------------------------------------------------------------------------	
		Folder folder2 = (Folder) ocm.getObject( "/folder2");
		assertNotNull("folder 2 is null", folder2);
		assertEquals("Invalid number of cms object  found in folder2 children", folder2.getChildren().size() ,0); // autoRetrieve = false
		
		//---------------------------------------------------------------------------------------------------------	
		// Insert nested objects
		//---------------------------------------------------------------------------------------------------------
		ocm.insert(subFolder);
		ocm.insert(document);
		ocm.save();
		
		//---------------------------------------------------------------------------------------------------------	
		// Retrieve folder2
		//---------------------------------------------------------------------------------------------------------	
		 folder2 = (Folder) ocm.getObject( "/folder2");
		assertNotNull("folder 2 is null", folder2);
		assertEquals("Invalid number of cms object  found in folder2 children", folder2.getChildren().size() ,0); // autoRetrieve = false

		//---------------------------------------------------------------------------------------------------------	
		// Retrieve children attribute
		//---------------------------------------------------------------------------------------------------------			
		ocm.retrieveMappedAttribute(folder2, "children");
		assertNotNull("folder 2 is null", folder2);
		assertEquals("Invalid number of cms object  found in folder2 children", folder2.getChildren().size() ,2);
		assertTrue("Invalid item in the collection", this.contains(folder2.getChildren(), "/folder2/document4", DocumentImpl.class));
		assertTrue("Invalid item in the collection", this.contains(folder2.getChildren(), "/folder2/subfolder", FolderImpl.class));		
		
		//---------------------------------------------------------------------------------------------------------	
		// Update
		//---------------------------------------------------------------------------------------------------------	
		folder2.setChildren(null);
        ocm.update(folder2); // autoupdate = false for the children attribute. So no update on the children collection
		ocm.save();

		//---------------------------------------------------------------------------------------------------------	
		// Retrieve children attribute
		//---------------------------------------------------------------------------------------------------------			
		ocm.retrieveMappedAttribute(folder2, "children");
		assertNotNull("folder 2 is null", folder2);
		assertEquals("Invalid number of cms object  found in folder2 children", folder2.getChildren().size() ,2);
		assertTrue("Invalid item in the collection", this.contains(folder2.getChildren(), "/folder2/document4", DocumentImpl.class));
		assertTrue("Invalid item in the collection", this.contains(folder2.getChildren(), "/folder2/subfolder", FolderImpl.class));		
		
		
	}

    @Override
	protected ObjectContentManager createObjectContentManager(Session session) throws RepositoryException
	{
		String[] files = {"./src/test/test-config/jcrmapping-auto.xml"};
		return new ObjectContentManagerImpl(session, files);
		
	}	
}