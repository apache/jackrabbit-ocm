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

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.portals.graffito.jcr.RepositoryLifecycleTestSetup;
import org.apache.portals.graffito.jcr.TestBase;
import org.apache.portals.graffito.jcr.persistence.PersistenceManager;
import org.apache.portals.graffito.jcr.query.Filter;
import org.apache.portals.graffito.jcr.query.Query;
import org.apache.portals.graffito.jcr.query.QueryManager;
import org.apache.portals.graffito.jcr.testmodel.Atomic;
import org.apache.portals.graffito.jcr.testmodel.inheritance.CmsObject;
import org.apache.portals.graffito.jcr.testmodel.inheritance.Content;
import org.apache.portals.graffito.jcr.testmodel.inheritance.DocumentStream;
import org.apache.portals.graffito.jcr.testmodel.inheritance.Document;
import org.apache.portals.graffito.jcr.testmodel.inheritance.Folder;

/**
 * Test inheritance with node type per concrete class (without  discreminator field)
 *
 * @author <a href="mailto:christophe.lombart@gmail.com">Christophe Lombart</a>
 */
public class PersistenceManagerNtConcreteClassTest extends TestBase {
	private final static Log log = LogFactory.getLog(PersistenceManagerNtConcreteClassTest.class);

	/**
	 * <p>Defines the test case name for junit.</p>
	 * @param testName The test case name.
	 */
	public PersistenceManagerNtConcreteClassTest(String testName) throws Exception {
		super(testName);

	}

	public static Test suite() {
		// All methods starting with "test" will be executed in the test suite.
		return new RepositoryLifecycleTestSetup(new TestSuite(
				PersistenceManagerNtConcreteClassTest.class));
	}

	public void tearDown() throws Exception {

		cleanUpRepisotory();
		super.tearDown();
		
	}


	public void testRetrieveSingleton() {

		try {
			PersistenceManager persistenceManager = this.getPersistenceManager();

			//---------------------------------------------------------------------------------------------------------
			// Insert a  Document 
			//---------------------------------------------------------------------------------------------------------			
            Document document = new Document();
            document.setPath("/document1");
            document.setName("document name");
            document.setContentType("plain/text"); 
            DocumentStream documentStream = new DocumentStream();
            documentStream.setEncoding("utf-8");
            documentStream.setContent("Test Content".getBytes());
            document.setDocumentStream(documentStream);
            
            persistenceManager.insert(document);
			persistenceManager.save();
			
			
			//---------------------------------------------------------------------------------------------------------
			// Retrieve a document object
			//---------------------------------------------------------------------------------------------------------						

			document = (Document) persistenceManager.getObject(Document.class, "/document1");
			assertEquals("Document path is invalid", document.getPath(), "/document1");
			assertEquals("Content type  is invalid", document.getContentType(), "plain/text");
			assertNotNull("document stream is null", document.getDocumentStream());
			assertTrue("Invalid document stream ", document.getDocumentStream().getEncoding().equals("utf-8"));
			
			
			//---------------------------------------------------------------------------------------------------------
			// Update  a descendant object
			//---------------------------------------------------------------------------------------------------------						
			document.setName("anotherName");
			persistenceManager.update(document);
			persistenceManager.save();

			//---------------------------------------------------------------------------------------------------------
			// Retrieve the updated descendant object
			//---------------------------------------------------------------------------------------------------------						
			document = (Document) persistenceManager.getObject(Document.class, "/document1");
			assertEquals("document name is incorrect", document.getName(), "anotherName");
			assertEquals("Document path is invalid", document.getPath(), "/document1");
			assertEquals("Content type  is invalid", document.getContentType(), "plain/text");
			assertNotNull("document stream is null", document.getDocumentStream());
			assertTrue("Invalid document stream", document.getDocumentStream().getEncoding().equals("utf-8"));

			CmsObject cmsObject = (CmsObject) persistenceManager.getObject(CmsObject.class, "/document1");
			assertEquals("cmsObject name is incorrect", cmsObject.getName(), "anotherName");
			assertEquals("cmsObject path is invalid", cmsObject.getPath(), "/document1");
           			
	
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}

	
	public void testRetrieveCollection() {
		PersistenceManager persistenceManager = this.getPersistenceManager();

		//---------------------------------------------------------------------------------------------------------
		// Insert descendant objects
		//---------------------------------------------------------------------------------------------------------			
        Document document = new Document();
        document.setPath("/document1");
        document.setName("document name 1");
        document.setContentType("plain/text"); 
        DocumentStream documentStream = new DocumentStream();
        documentStream.setEncoding("utf-8");
        documentStream.setContent("Test Content".getBytes());
        document.setDocumentStream(documentStream);        
        persistenceManager.insert(document);
        
        document = new Document();
        document.setPath("/document2");        
        document.setName("document name 2");
        document.setContentType("plain/text"); 
        documentStream = new DocumentStream();
        documentStream.setEncoding("utf-8");
        documentStream.setContent("Test Content".getBytes());
        document.setDocumentStream(documentStream);       
        persistenceManager.insert(document);

        document = new Document();
        document.setPath("/document3");        
        document.setName("document 3");
        document.setContentType("plain/text"); 
        documentStream = new DocumentStream();
        documentStream.setEncoding("utf-8");
        documentStream.setContent("Test Content 3".getBytes());
        document.setDocumentStream(documentStream);       
        persistenceManager.insert(document);
        
        Folder folder = new Folder();
        folder.setPath("/folder1");
        folder.setName("folder1");
        persistenceManager.insert(folder);
 
         folder = new Folder();
        folder.setPath("/folder2");
        folder.setName("folder2");
        persistenceManager.insert(folder);               		

		Atomic a = new Atomic();
		a.setPath("/atomic");
		a.setBooleanPrimitive(true);
		persistenceManager.insert(a);

		persistenceManager.save();

		//---------------------------------------------------------------------------------------------------------	
		// Retrieve Folders
		//---------------------------------------------------------------------------------------------------------			
		QueryManager queryManager = persistenceManager.getQueryManager();
		Filter filter = queryManager.createFilter(Folder.class);
		Query query = queryManager.createQuery(filter);

		Collection result = persistenceManager.getObjects(query);
		assertEquals("Invalid number of folders found", result.size(), 2);
		assertTrue("Invalid item in the collection", this.contains(result, "/folder1",Folder.class));
		assertTrue("Invalid item in the collection", this.contains(result, "/folder2", Folder.class));		
		
	
		//---------------------------------------------------------------------------------------------------------	
		// Retrieve Documents 
		//---------------------------------------------------------------------------------------------------------			
		queryManager = persistenceManager.getQueryManager();
		filter = queryManager.createFilter(Document.class);
		filter.addLike("name", "document name%");
		query = queryManager.createQuery(filter);

		result = persistenceManager.getObjects(query);
		assertEquals("Invalid number of documents  found", result.size(),2);
		assertTrue("Invalid item in the collection", this.contains(result, "/document1", Document.class));
		assertTrue("Invalid item in the collection", this.contains(result, "/document2", Document.class));

		//---------------------------------------------------------------------------------------------------------	
		// Retrieve Contents (ancestor of Documents) 
		//---------------------------------------------------------------------------------------------------------			
		queryManager = persistenceManager.getQueryManager();
		filter = queryManager.createFilter(Content.class);
		filter.addLike("name", "document name%");
		query = queryManager.createQuery(filter);

		result = persistenceManager.getObjects(query);
		assertEquals("Invalid number of documents  found", result.size(),2);
		assertTrue("Invalid item in the collection", this.contains(result, "/document1", Document.class));
		assertTrue("Invalid item in the collection", this.contains(result, "/document2", Document.class));
		
				
		//---------------------------------------------------------------------------------------------------------	
		// Retrieve all cmsobjects
		//---------------------------------------------------------------------------------------------------------					
		queryManager = persistenceManager.getQueryManager();
		filter = queryManager.createFilter(CmsObject.class);		
		query = queryManager.createQuery(filter);

		result = persistenceManager.getObjects(query);
		assertEquals("Invalid ancestor object found", result.size(),5);
		assertTrue("Invalid item in the collection", this.contains(result, "/document1", Document.class));
		assertTrue("Invalid item in the collection", this.contains(result, "/document2", Document.class));	
		assertTrue("Invalid item in the collection", this.contains(result, "/document3", Document.class));
		assertTrue("Invalid item in the collection", this.contains(result, "/folder1",Folder.class));	
		assertTrue("Invalid item in the collection", this.contains(result, "/folder2",Folder.class));
	
	}
	    
}