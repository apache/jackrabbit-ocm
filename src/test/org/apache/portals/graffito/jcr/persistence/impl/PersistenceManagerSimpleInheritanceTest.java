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

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

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
import org.apache.portals.graffito.jcr.testmodel.Paragraph;
import org.apache.portals.graffito.jcr.testmodel.inheritance.Ancestor;
import org.apache.portals.graffito.jcr.testmodel.inheritance.AnotherDescendant;
import org.apache.portals.graffito.jcr.testmodel.inheritance.Descendant;
import org.apache.portals.graffito.jcr.testmodel.inheritance.Folder;

/**
 * Test inheritance with node type per hierarchy stategy (with discreminator field)
 *
 * @author <a href="mailto:christophe.lombart@gmail.com">Christophe Lombart</a>
 */
public class PersistenceManagerSimpleInheritanceTest extends TestBase {
	private final static Log log = LogFactory.getLog(PersistenceManagerSimpleInheritanceTest.class);

	/**
	 * <p>Defines the test case name for junit.</p>
	 * @param testName The test case name.
	 */
	public PersistenceManagerSimpleInheritanceTest(String testName) throws Exception {
		super(testName);

	}

	public static Test suite() {
		// All methods starting with "test" will be executed in the test suite.
		return new RepositoryLifecycleTestSetup(new TestSuite(
				PersistenceManagerSimpleInheritanceTest.class));
	}

	public void tearDown() throws Exception {

		super.tearDown();
	}

	public void testRetrieveSingleton() {

		try {
			PersistenceManager persistenceManager = this.getPersistenceManager();

			//---------------------------------------------------------------------------------------------------------
			// Insert a descendant object
			//---------------------------------------------------------------------------------------------------------			
			Descendant descendant = new Descendant();
			descendant.setDescendantField("descendantValue");
			descendant.setAncestorField("ancestorValue");
			descendant.setPath("/test");
			persistenceManager.insert(descendant);
			persistenceManager.save();

			//---------------------------------------------------------------------------------------------------------
			// Retrieve a descendant object
			//---------------------------------------------------------------------------------------------------------						
			descendant = null;
			descendant = (Descendant) persistenceManager.getObject(	Descendant.class, "/test");
			assertEquals("Descendant path is invalid", descendant.getPath(), "/test");
			assertEquals("Descendant ancestorField is invalid", descendant.getAncestorField(), "ancestorValue");
			assertEquals("Descendant descendantField is invalid", descendant	.getDescendantField(), "descendantValue");

			//---------------------------------------------------------------------------------------------------------
			// Update  a descendant object
			//---------------------------------------------------------------------------------------------------------						
			descendant.setAncestorField("anotherAncestorValue");
			persistenceManager.update(descendant);
			persistenceManager.save();

			//---------------------------------------------------------------------------------------------------------
			// Retrieve the updated descendant object
			//---------------------------------------------------------------------------------------------------------						
			descendant = null;
			descendant = (Descendant) persistenceManager.getObject(	Descendant.class, "/test");
			assertEquals("Descendant path is invalid", descendant.getPath(), "/test");
			assertEquals("Descendant ancestorField is invalid", descendant.getAncestorField(), "anotherAncestorValue");
			assertEquals("Descendant descendantField is invalid", descendant	.getDescendantField(), "descendantValue");


// UNCOMMENT
//			Ancestor ancestor = (Ancestor) persistenceManager.getObject(Ancestor.class,"/test");
//			assertTrue("Invalid object instance", ancestor instanceof Descendant );
//		    assertEquals("ancestor path is invalid", ancestor.getPath(), "/test");
//   		    assertEquals("Desancestorcendant ancestorField is invalid", ancestor.getAncestorField(), "ancestorValue");

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}

	/*
	public void testRetrieveCollection() {
		PersistenceManager persistenceManager = this.getPersistenceManager();

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
		anotherDescendant.setAnotherDescendantField("anotherDescendantValue");
		anotherDescendant.setAncestorField("ancestorValue5");
		anotherDescendant.setPath("/anotherdescendant3");
		persistenceManager.insert(anotherDescendant);

		Atomic a = new Atomic();
		a.setPath("/atomic");
		a.setBooleanPrimitive(true);
		persistenceManager.insert(a);

		persistenceManager.save();

		QueryManager queryManager = persistenceManager.getQueryManager();
		Filter filter = queryManager.createFilter(Descendant.class);
		Query query = queryManager.createQuery(filter);

		Collection result = persistenceManager.getObjects(query);
		assertEquals("Invalid number of Descendant found", result.size(), 2);

	}
	*/

}