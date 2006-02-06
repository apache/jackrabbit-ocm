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
package org.apache.portals.graffito.jcr.mapper;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.portals.graffito.jcr.exception.JcrMappingException;
import org.apache.portals.graffito.jcr.mapper.impl.DigesterMapperImpl;
import org.apache.portals.graffito.jcr.mapper.model.BeanDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.ClassDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.CollectionDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.FieldDescriptor;
import org.apache.portals.graffito.jcr.persistence.objectconverter.impl.ObjectConverterImpl;
import org.apache.portals.graffito.jcr.testmodel.A;
import org.apache.portals.graffito.jcr.testmodel.B;

/**
 * Test Mapper
 *
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Christophe Lombart</a>
 */
public class DigesterMapperImplTest extends TestCase
{
    /**
     * <p>Defines the test case name for junit.</p>
     * @param testName The test case name.
     */
    public DigesterMapperImplTest(String testName)
    {
        super(testName);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public static Test suite()
    {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(DigesterMapperImplTest.class);
    }

    /**
     * Test for getConverter
     *
     */
    public void testMapper()    
    {
        try
        {
            DigesterMapperImpl mapper = new DigesterMapperImpl("./src/test-config/jcrmapping-testmappings.xml");
            assertNotNull("Mapper is null", mapper);
            
            ClassDescriptor classDescriptor = mapper.getClassDescriptor(A.class);
            assertNotNull("ClassDescriptor is null", classDescriptor);
            assertTrue("Invalid classname", classDescriptor.getClassName().equals(A.class.getName()));
            assertTrue("Invalid path field", classDescriptor.getPathFieldDescriptor().getFieldName().equals("path"));
            assertEquals("Invalid mixins", "mixin:a", classDescriptor.getJcrMixinTypes()[0] );
            
            FieldDescriptor fieldDescriptor = classDescriptor.getFieldDescriptor("a1");
            assertNotNull("FieldDescriptor is null", fieldDescriptor);
            assertTrue("Invalid jcrName for field a1", fieldDescriptor.getJcrName().equals("a1"));
            
            BeanDescriptor beanDescriptor = classDescriptor.getBeanDescriptor("b");
            assertNotNull("BeanDescriptor is null", beanDescriptor);
            assertTrue("Invalid jcrName for field b", beanDescriptor.getJcrName().equals("b"));
            assertEquals("Invalid bean-descriptor inline", true, beanDescriptor.isInline());
            assertEquals("Invalid bean default descriptor", 
                         ObjectConverterImpl.class.getName(),
                         beanDescriptor.getConverter());
            
            CollectionDescriptor collectionDescriptor = classDescriptor.getCollectionDescriptor("collection");
            assertNotNull("CollectionDescriptor is null", collectionDescriptor);
            assertTrue("Invalid jcrName for field collection", collectionDescriptor.getJcrName().equals("collection"));
        }
        catch (JcrMappingException e)
        {
              e.printStackTrace();
              fail("Impossible to retrieve the converter " + e);
        }
    }
    
    /**
     * Test for getConverter
     *
     */
    public void testMapperOptionalProperties()    
    {
        try
        {
            DigesterMapperImpl mapper = new DigesterMapperImpl("./src/test-config/jcrmapping.xml");
            assertNotNull("Mapper is null", mapper);
            
            ClassDescriptor classDescriptor = mapper.getClassDescriptor(B.class);
            assertNotNull("ClassDescriptor is null", classDescriptor);
            assertTrue("Invalid classname", classDescriptor.getClassName().equals(B.class.getName()));
            assertEquals(classDescriptor.getJcrSuperTypes(), "nt:base");
            
            FieldDescriptor b1Field = classDescriptor.getFieldDescriptor("b1");
            assertNotNull("FieldDescriptor is null", b1Field);
            assertEquals(b1Field.getFieldName(), "b1");
            assertEquals(b1Field.getJcrType(), "String");
            assertFalse(b1Field.isJcrAutoCreated());
            assertFalse(b1Field.isJcrMandatory());
            assertFalse(b1Field.isJcrProtected());
            assertFalse(b1Field.isJcrMultiple());
            assertEquals(b1Field.getJcrOnParentVersion(), "IGNORE");
            
            FieldDescriptor b2Field = classDescriptor.getFieldDescriptor("b2");
            assertNotNull("FieldDescriptor is null", b2Field);
            assertEquals(b2Field.getFieldName(), "b2");
            assertEquals(b2Field.getJcrType(), "String");
            assertTrue(b2Field.isJcrAutoCreated());
            assertTrue(b2Field.isJcrMandatory());
            assertTrue(b2Field.isJcrProtected());
            assertTrue(b2Field.isJcrMultiple());
            assertEquals(b2Field.getJcrOnParentVersion(), "COPY");
            
            ClassDescriptor classDescriptor2 = mapper.getClassDescriptor(A.class);
            assertNotNull("ClassDescriptor is null", classDescriptor2);
            assertTrue("Invalid classname", classDescriptor2.getClassName().equals(A.class.getName()));
            
            BeanDescriptor beanDescriptor = classDescriptor2.getBeanDescriptor("b");
            assertNotNull(beanDescriptor);
            assertEquals(beanDescriptor.getFieldName(), "b");
            assertEquals(beanDescriptor.getJcrNodeType(), "nt:unstructured");
            assertFalse(beanDescriptor.isJcrAutoCreated());
            assertFalse(beanDescriptor.isJcrMandatory());
            assertFalse(beanDescriptor.isJcrProtected());
            assertFalse(beanDescriptor.isJcrSameNameSiblings());
            assertEquals(beanDescriptor.getJcrOnParentVersion(), "IGNORE");
            
            CollectionDescriptor collectionDescriptor = classDescriptor2.getCollectionDescriptor("collection");
            assertNotNull(collectionDescriptor);
            assertEquals(collectionDescriptor.getJcrNodeType(), "graffito:C");
            assertFalse(collectionDescriptor.isJcrAutoCreated());
            assertFalse(collectionDescriptor.isJcrMandatory());
            assertFalse(collectionDescriptor.isJcrProtected());
            assertFalse(collectionDescriptor.isJcrSameNameSiblings());
            assertEquals(collectionDescriptor.getJcrOnParentVersion(), "IGNORE");
        }
        catch (JcrMappingException e)
        {
              e.printStackTrace();
              fail("Impossible to retrieve the converter " + e);
        }
    }
}