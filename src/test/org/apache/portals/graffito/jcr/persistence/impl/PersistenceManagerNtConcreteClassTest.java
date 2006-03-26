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
import org.apache.portals.graffito.jcr.testmodel.inheritance.CmsObject;
import org.apache.portals.graffito.jcr.testmodel.inheritance.Content;
import org.apache.portals.graffito.jcr.testmodel.inheritance.Document;

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
            Content content = new Content();
            content.setEncoding("utf-8");
            content.setContent("Test Content".getBytes());
            document.setContent(content);
            
            persistenceManager.insert(document);
			persistenceManager.save();
			
			
			//---------------------------------------------------------------------------------------------------------
			// Retrieve a document object
			//---------------------------------------------------------------------------------------------------------						

			document = (Document) persistenceManager.getObject(Document.class, "/document1");
			assertEquals("Document path is invalid", document.getPath(), "/document1");
			assertEquals("Content type  is invalid", document.getContentType(), "plain/text");
			assertNotNull("Content is null", document.getContent());
			assertTrue("Invalid content", document.getContent().getEncoding().equals("utf-8"));
			
			
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
			assertNotNull("Content is null", document.getContent());
			assertTrue("Invalid content", document.getContent().getEncoding().equals("utf-8"));

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
/*
		//---------------------------------------------------------------------------------------------------------	
		// Insert  descendant objects
		//---------------------------------------------------------------------------------------------------------			
		Descendant descendant = new Descendant();
		descendant.setDescendantField("descendantValue");
		descendant.setAncestorField("ancestorValue");
		descendant.setPath("/descendant1");
		persistenceManager.insert(descendant);

		descendant = new Descendant();
		descendant.setDescendantField("descendantValue2");
		descendant.setAncestorField("ancestorValue2");
		descendant.setPath("/descendant2");
		persistenceManager.insert(descendant);

		SubDescendant subDescendant = new SubDescendant();
		subDescendant.setDescendantField("descendantValue2");
		subDescendant.setAncestorField("ancestorValue2");
		subDescendant.setPath("/subdescendant");
		subDescendant.setSubDescendantField("subdescendantvalue");
		persistenceManager.insert(subDescendant);		

		 subDescendant = new SubDescendant();
		subDescendant.setDescendantField("descendantValue3");
		subDescendant.setAncestorField("ancestorValue2");
		subDescendant.setPath("/subdescendant2");
		subDescendant.setSubDescendantField("subdescendantvalue1");
		persistenceManager.insert(subDescendant);		
		
		
		AnotherDescendant anotherDescendant = new AnotherDescendant();
		anotherDescendant.setAnotherDescendantField("anotherDescendantValue");
		anotherDescendant.setAncestorField("ancestorValue3");
		anotherDescendant.setPath("/anotherdescendant1");
		persistenceManager.insert(anotherDescendant);

		anotherDescendant = new AnotherDescendant();
		anotherDescendant.setAnotherDescendantField("anotherDescendantValue");
		anotherDescendant.setAncestorField("ancestorValue4");
		anotherDescendant.setPath("/anotherdescendant2");
		persistenceManager.insert(anotherDescendant);

		anotherDescendant = new AnotherDescendant();
		anotherDescendant.setAnotherDescendantField("anotherDescendantValue2");
		anotherDescendant.setAncestorField("ancestorValue5");
		anotherDescendant.setPath("/anotherdescendant3");
		persistenceManager.insert(anotherDescendant);

		
		Atomic a = new Atomic();
		a.setPath("/atomic");
		a.setBooleanPrimitive(true);
		persistenceManager.insert(a);

		persistenceManager.save();

		//---------------------------------------------------------------------------------------------------------	
		// Retrieve Descendant class
		//---------------------------------------------------------------------------------------------------------			
		QueryManager queryManager = persistenceManager.getQueryManager();
		Filter filter = queryManager.createFilter(Descendant.class);
		Query query = queryManager.createQuery(filter);

		Collection result = persistenceManager.getObjects(query);
		assertEquals("Invalid number of Descendant found", result.size(), 4);
		assertTrue("Invalid item in the collection", this.contains(result, "/descendant1", Descendant.class));
		assertTrue("Invalid item in the collection", this.contains(result, "/descendant2", Descendant.class));
		assertTrue("Invalid item in the collection", this.contains(result, "/subdescendant", SubDescendant.class));
		assertTrue("Invalid item in the collection", this.contains(result, "/subdescendant2", SubDescendant.class));
		

		//---------------------------------------------------------------------------------------------------------	
		// Retrieve AnotherDescendant class
		//---------------------------------------------------------------------------------------------------------			
		queryManager = persistenceManager.getQueryManager();
		filter = queryManager.createFilter(AnotherDescendant.class);
		filter.addEqualTo("anotherDescendantField", "anotherDescendantValue");
		query = queryManager.createQuery(filter);

		result = persistenceManager.getObjects(query);
		assertEquals("Invalid number of AnotherDescendant found", result.size(),2);
		assertTrue("Invalid item in the collection", this.contains(result, "/anotherdescendant1", AnotherDescendant.class));
		assertTrue("Invalid item in the collection", this.contains(result, "/anotherdescendant2", AnotherDescendant.class));

		//---------------------------------------------------------------------------------------------------------	
		// Retrieve some descendants & subdescendants
		//---------------------------------------------------------------------------------------------------------			
		queryManager = persistenceManager.getQueryManager();
		filter = queryManager.createFilter(Descendant.class);		
		filter.addEqualTo("descendantField","descendantValue2");
		query = queryManager.createQuery(filter);

		result = persistenceManager.getObjects(query);
		assertEquals("Invalid ancestor object found", result.size(),2);
		assertTrue("Invalid item in the collection", this.contains(result, "/descendant2", Descendant.class));
		assertTrue("Invalid item in the collection", this.contains(result, "/subdescendant", SubDescendant.class));
		
		//---------------------------------------------------------------------------------------------------------	
		// Retrieve all class
		//---------------------------------------------------------------------------------------------------------			
		queryManager = persistenceManager.getQueryManager();
		filter = queryManager.createFilter(Ancestor.class);		
		query = queryManager.createQuery(filter);

		result = persistenceManager.getObjects(query);
		assertEquals("Invalid ancestor object found", result.size(),7);
		assertTrue("Invalid item in the collection", this.contains(result, "/descendant1", Descendant.class));
		assertTrue("Invalid item in the collection", this.contains(result, "/descendant2", Descendant.class));
		assertTrue("Invalid item in the collection", this.contains(result, "/subdescendant", SubDescendant.class));
		assertTrue("Invalid item in the collection", this.contains(result, "/subdescendant2", SubDescendant.class));
		assertTrue("Invalid item in the collection", this.contains(result, "/anotherdescendant1", AnotherDescendant.class));
		assertTrue("Invalid item in the collection", this.contains(result, "/anotherdescendant2", AnotherDescendant.class));
		assertTrue("Invalid item in the collection", this.contains(result, "/anotherdescendant3", AnotherDescendant.class));		

 */
	}
	    
}