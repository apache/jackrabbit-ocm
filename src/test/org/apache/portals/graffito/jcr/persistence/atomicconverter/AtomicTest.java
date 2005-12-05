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
package org.apache.portals.graffito.jcr.persistence.atomicconverter;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.portals.graffito.jcr.TestBase;
import org.apache.portals.graffito.jcr.persistence.PersistenceManager;
import org.apache.portals.graffito.jcr.testmodel.Atomic;

/**
 * Test Atomic perisstence fields
 *
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Christophe Lombart</a>
 */
public class AtomicTest extends TestBase
{
    private final static Log log = LogFactory.getLog(AtomicTest.class);

    /**
     * <p>Defines the test case name for junit.</p>
     * @param testName The test case name.
     */
    public AtomicTest(String testName) throws Exception
    {
        super(testName);
    }

    public static Test suite()
    {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(AtomicTest.class);
    }


    /**
     * @see junit.framework.TestCase#tearDown()
     */
    public void tearDown() throws Exception
    {
        if (getPersistenceManager().objectExists("/test"))
        {
            getPersistenceManager().remove("/test");
            getPersistenceManager().save();
        }        
    	
        super.tearDown();
    }
    
    public void testAtomicFields()
    {
        try
        {
        	PersistenceManager persistenceManager = getPersistenceManager();
        	Date date = new Date();
        	Calendar calendar = Calendar.getInstance();
            // --------------------------------------------------------------------------------
            // Create and store an object graph in the repository
            // --------------------------------------------------------------------------------
            Atomic a = new Atomic();
            a.setPath("/test");
            a.setBooleanObject(new Boolean(true));
            a.setBooleanPrimitive(true);
            a.setIntegerObject(new Integer(100));
            a.setIntPrimitive(200);
            a.setString("Test String");
            a.setDate(date);
            
            byte[] content = "Test Byte".getBytes();
            a.setByteArray(content);
            a.setCalendar(calendar);
            a.setDoubleObject(new Double(2.12));
            a.setDoublePrimitive(1.23);
            long now = System.currentTimeMillis();
            a.setTimestamp(new Timestamp(now));
            
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("Test Stream".getBytes());
            a.setInputStream(byteArrayInputStream);
            
            persistenceManager.insert(a);
            persistenceManager.save();

             
            // --------------------------------------------------------------------------------
            // Get the object
            // --------------------------------------------------------------------------------
            a = null;
            a = (Atomic) persistenceManager.getObject(Atomic.class, "/test");
            assertNotNull("a is null", a);
            assertNotNull("Boolean object is null", a.getBooleanObject());
            assertTrue("Incorrect boolean object", a.getBooleanObject().booleanValue());
            assertTrue("Incorrect boolean primitive", a.isBooleanPrimitive());
            assertNotNull("Integer Object is null", a.getIntegerObject());
            assertTrue("Incorrect Integer object", a.getIntegerObject().intValue() == 100);
            assertTrue("Incorrect int primitive", a.getIntPrimitive() == 200);
            assertNotNull("String object is null", a.getString());
            assertTrue("Incorrect boolean object", a.getString().equals("Test String"));
            assertNotNull("Byte array object is null", a.getByteArray());
            assertTrue("Incorrect byte object", new String(a.getByteArray()).equals("Test Byte"));
            
            assertNotNull("date object is null", a.getDate());
            assertTrue("Invalid date", a.getDate().equals(date));            
            assertNotNull("calendar object is null", a.getCalendar());
            
            log.debug("Calendar : " + a.getCalendar().get(Calendar.YEAR) + "-" + a.getCalendar().get(Calendar.MONTH) + "-" + a.getCalendar().get(Calendar.DAY_OF_MONTH));
            assertTrue("Invalid calendar object", a.getCalendar().equals(calendar));
            
            assertNotNull("Double object is null", a.getDoubleObject());
            assertTrue("Incorrect double object", a.getDoubleObject().doubleValue() == 2.12);
            assertTrue("Incorrect double primitive", a.getDoublePrimitive() == 1.23);
            
            assertNotNull("Incorrect input stream primitive", a.getInputStream());
            assertNotNull("Incorrect timestamp", a.getTimestamp());
            assertTrue("Invalid timestamp value ", a.getTimestamp().getTime() == now);            
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Exception occurs during the unit test : " + e);
        }
        
    }
    
}