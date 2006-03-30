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

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.portals.graffito.jcr.exception.JcrMappingException;
import org.apache.portals.graffito.jcr.mapper.impl.DigesterMapperImpl;
import org.apache.portals.graffito.jcr.mapper.model.BeanDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.ClassDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.CollectionDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.FieldDescriptor;
import org.apache.portals.graffito.jcr.testmodel.A;
import org.apache.portals.graffito.jcr.testmodel.B;
import org.apache.portals.graffito.jcr.testmodel.inheritance.Ancestor;
import org.apache.portals.graffito.jcr.testmodel.inheritance.CmsObject;
import org.apache.portals.graffito.jcr.testmodel.inheritance.Descendant;
import org.apache.portals.graffito.jcr.testmodel.inheritance.Document;
import org.apache.portals.graffito.jcr.testmodel.inheritance.SubDescendant;

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

    public static Test suite()
    {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(DigesterMapperImplTest.class);
    }

    /**
     * Simple test mapper
     *
     */
    public void testMapper()    
    {
        try
        {

    		
            Mapper mapper = new DigesterMapperImpl("./src/test-config/jcrmapping-testmappings.xml")
                .buildMapper();
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
            assertNull("Invalid bean default converter", beanDescriptor.getConverter());
            assertNull("Invalid bean converter", beanDescriptor.getBeanConverter());
            
            
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
     * Test optional mapping properties
     *
     */
    public void testMapperOptionalProperties()    
    {
        try
        {
            Mapper mapper = new DigesterMapperImpl("./src/test-config/jcrmapping.xml")
                    .buildMapper();
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
            assertFalse(b2Field.isJcrAutoCreated());
            assertFalse(b2Field.isJcrMandatory());
            assertFalse(b2Field.isJcrProtected());
            assertFalse(b2Field.isJcrMultiple());
            assertEquals(b2Field.getJcrOnParentVersion(), "IGNORE");
            
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
    
    /**
     *
     * Test Node Type per hierarchy setting
     */
    public void testMapperNtHierarchy()    
    {
        try
        {
    		String[] files = { "./src/test-config/jcrmapping.xml", 
                    "./src/test-config/jcrmapping-atomic.xml",
                    "./src/test-config/jcrmapping-beandescriptor.xml",
                    "./src/test-config/jcrmapping-inheritance.xml"};
    		    Mapper mapper = new DigesterMapperImpl(files) .buildMapper();            	
           
            assertNotNull("Mapper is null", mapper);
            
            ClassDescriptor classDescriptor = mapper.getClassDescriptor(Ancestor.class);
            assertNotNull("Classdescriptor is null", classDescriptor);
            assertEquals("Incorrect path field", classDescriptor.getPathFieldDescriptor().getFieldName(), "path");
            assertEquals("Incorrect discriminator field", classDescriptor.getDiscriminatorFieldDescriptor().getFieldName(), "discriminator");
            assertTrue("The ancestor class has no discriminator", classDescriptor.hasDiscriminatorField());
            assertTrue("The ancestor class is not abstract", classDescriptor.isAbstract());
            assertNull("The ancestor class has an ancestor", classDescriptor.getSuperClassDescriptor());
            assertEquals("Incorrect JcrName", classDescriptor.getJcrName("discriminator"),"discriminator");
            assertTrue("Ancestor class doesn't have a node type per hierarchy strategy", classDescriptor.usesNodeTypePerHierarchyStrategy());
            assertFalse("Ancestor class  have a node type per hierarchy strategy", classDescriptor.usesNodeTypePerConcreteClassStrategy());
            
            Collection descendandDescriptors = classDescriptor.getDescendantClassDescriptors();
            assertEquals("Invalid number of descendants", descendandDescriptors.size(), 2);
            
            classDescriptor = mapper.getClassDescriptor(Descendant.class);
            assertNotNull("Classdescriptor is null", classDescriptor);
            assertEquals("Incorrect path field", classDescriptor.getPathFieldDescriptor().getFieldName(), "path");
            assertEquals("Incorrect discriminator field", classDescriptor.getDiscriminatorFieldDescriptor().getFieldName(), "discriminator");
            assertTrue("The descendant  class has no discriminator", classDescriptor.hasDiscriminatorField());
            assertNotNull("ancerstorField is null in the descendant class", classDescriptor.getFieldDescriptor("ancestorField"));
            assertFalse("The descendant class is abstract", classDescriptor.isAbstract());
            assertNotNull("The descendant class has not an ancestor", classDescriptor.getSuperClassDescriptor());
            assertEquals("Invalid ancestor class for the descendant class", classDescriptor.getSuperClassDescriptor().getClassName(), "org.apache.portals.graffito.jcr.testmodel.inheritance.Ancestor");
            assertEquals("Incorrect JcrName", classDescriptor.getJcrName("discriminator"),"discriminator");
             descendandDescriptors = classDescriptor.getDescendantClassDescriptors();
            assertEquals("Invalid number of descendants", descendandDescriptors.size(), 1);
            assertTrue("Descendant  class doesn't have a node type per hierarchy strategy", classDescriptor.usesNodeTypePerHierarchyStrategy());
            assertFalse("Descendant class  have a node type per hierarchy strategy", classDescriptor.usesNodeTypePerConcreteClassStrategy());

            
            classDescriptor = mapper.getClassDescriptor(SubDescendant.class);
            assertNotNull("Classdescriptor is null", classDescriptor);
            assertEquals("Incorrect path field", classDescriptor.getPathFieldDescriptor().getFieldName(), "path");
            assertEquals("Incorrect discriminator field", classDescriptor.getDiscriminatorFieldDescriptor().getFieldName(), "discriminator");
            assertTrue("The subdescendant  class has no discriminator", classDescriptor.hasDiscriminatorField());
            assertNotNull("ancestorField is null in the descendant class", classDescriptor.getFieldDescriptor("ancestorField"));
            assertFalse("The subdescendant class is abstract", classDescriptor.isAbstract());
            assertNotNull("The subdescendant class has not an ancestor", classDescriptor.getSuperClassDescriptor());
            assertEquals("Invalid ancestor class for the descendant class", classDescriptor.getSuperClassDescriptor().getClassName(), "org.apache.portals.graffito.jcr.testmodel.inheritance.Descendant");
            assertEquals("Incorrect JcrName", classDescriptor.getJcrName("discriminator"),"discriminator");
             descendandDescriptors = classDescriptor.getDescendantClassDescriptors();
            assertEquals("Invalid number of descendants", descendandDescriptors.size(), 0);
            assertTrue("SubDescendant  class doesn't have a node type per hierarchy strategy", classDescriptor.usesNodeTypePerHierarchyStrategy());
            assertFalse("SubDescendant class  have a node type per hierarchy strategy", classDescriptor.usesNodeTypePerConcreteClassStrategy());
            
        }
        catch (JcrMappingException e)
        {
              e.printStackTrace();
              fail("Impossible to retrieve the converter " + e);
        }
    }
    
    
    /**
    *
    * Test Node Type per concrete class  setting
    */
   public void testMapperNtConcreteClass()    
   {
       try
       {
   		String[] files = { "./src/test-config/jcrmapping.xml", 
                   "./src/test-config/jcrmapping-atomic.xml",
                   "./src/test-config/jcrmapping-beandescriptor.xml",
                   "./src/test-config/jcrmapping-inheritance.xml"};
//      		String[] files = {  "./src/test-config/jcrmapping-inheritance.xml"};
    	   
   		    Mapper mapper = new DigesterMapperImpl(files) .buildMapper();            	
          
           assertNotNull("Mapper is null", mapper);
           
           ClassDescriptor classDescriptor = mapper.getClassDescriptor(CmsObject.class);
           assertNotNull("Classdescriptor is null", classDescriptor);
           assertEquals("Incorrect path field", classDescriptor.getPathFieldDescriptor().getFieldName(), "path");          
           assertFalse("The cms object class  has discriminator", classDescriptor.hasDiscriminatorField());
           assertNull("The cms object class has an discriminator field", classDescriptor.getDiscriminatorFieldDescriptor());
           assertTrue("The cmsobject class is not abstract", classDescriptor.isAbstract());
           assertNull("The cmsobject class has an ancestor", classDescriptor.getSuperClassDescriptor());           
           assertFalse("The cmsobject class  have a node type per hierarchy strategy", classDescriptor.usesNodeTypePerHierarchyStrategy());
           assertTrue("The cmsobject class  have not a node type per hierarchy strategy", classDescriptor.usesNodeTypePerConcreteClassStrategy());
           assertTrue ("The cmsobject class has no descendant ", classDescriptor.hasDescendants());
           assertEquals("Invalid number of descendants", classDescriptor.getDescendantClassDescriptors().size(), 2);
           
            classDescriptor = mapper.getClassDescriptor(Document.class);
           assertNotNull("Classdescriptor is null", classDescriptor);
           assertEquals("Incorrect path field", classDescriptor.getPathFieldDescriptor().getFieldName(), "path");          
           assertFalse("The document class  has discriminator", classDescriptor.hasDiscriminatorField());
           assertNull("The document has an discriminator field", classDescriptor.getDiscriminatorFieldDescriptor());
           assertFalse("The document class is abstract", classDescriptor.isAbstract());
           assertNotNull("The document class has not  an ancestor", classDescriptor.getSuperClassDescriptor());           
           assertEquals("The document class has an invalid ancestor ancestor", classDescriptor.getSuperClassDescriptor().getClassName(), "org.apache.portals.graffito.jcr.testmodel.inheritance.Content");
           assertFalse("The document class  have a node type per hierarchy strategy", classDescriptor.usesNodeTypePerHierarchyStrategy());
           assertTrue("The document class  have not a node type per hierarchy strategy", classDescriptor.usesNodeTypePerConcreteClassStrategy());
           assertFalse ("The document class has no descendant ", classDescriptor.hasDescendants());
           assertEquals("Invalid number of descendants", classDescriptor.getDescendantClassDescriptors().size(), 0);
           
           
           
           
//           Collection descendandDescriptors = classDescriptor.getDescendantClassDescriptors();
//           assertEquals("Invalid number of descendants", descendandDescriptors.size(), 2);
//           
//           classDescriptor = mapper.getClassDescriptor(Descendant.class);
//           assertNotNull("Classdescriptor is null", classDescriptor);
//           assertEquals("Incorrect path field", classDescriptor.getPathFieldDescriptor().getFieldName(), "path");
//           assertEquals("Incorrect discriminator field", classDescriptor.getDiscriminatorFieldDescriptor().getFieldName(), "discriminator");
//           assertTrue("The descendant  class has no discriminator", classDescriptor.hasDiscriminatorField());
//           assertNotNull("ancerstorField is null in the descendant class", classDescriptor.getFieldDescriptor("ancestorField"));
//           assertFalse("The descendant class is abstract", classDescriptor.isAbstract());
//           assertNotNull("The descendant class has not an ancestor", classDescriptor.getSuperClassDescriptor());
//           assertEquals("Invalid ancestor class for the descendant class", classDescriptor.getSuperClassDescriptor().getClassName(), "org.apache.portals.graffito.jcr.testmodel.inheritance.Ancestor");
//           assertEquals("Incorrect JcrName", classDescriptor.getJcrName("discriminator"),"discriminator");
//            descendandDescriptors = classDescriptor.getDescendantClassDescriptors();
//           assertEquals("Invalid number of descendants", descendandDescriptors.size(), 1);
//           assertTrue("Descendant  class doesn't have a node type per hierarchy strategy", classDescriptor.usesNodeTypePerHierarchyStrategy());
//           assertFalse("Descendant class  have a node type per hierarchy strategy", classDescriptor.usesNodeTypePerConcreteClassStrategy());
//
//           
//           classDescriptor = mapper.getClassDescriptor(SubDescendant.class);
//           assertNotNull("Classdescriptor is null", classDescriptor);
//           assertEquals("Incorrect path field", classDescriptor.getPathFieldDescriptor().getFieldName(), "path");
//           assertEquals("Incorrect discriminator field", classDescriptor.getDiscriminatorFieldDescriptor().getFieldName(), "discriminator");
//           assertTrue("The subdescendant  class has no discriminator", classDescriptor.hasDiscriminatorField());
//           assertNotNull("ancestorField is null in the descendant class", classDescriptor.getFieldDescriptor("ancestorField"));
//           assertFalse("The subdescendant class is abstract", classDescriptor.isAbstract());
//           assertNotNull("The subdescendant class has not an ancestor", classDescriptor.getSuperClassDescriptor());
//           assertEquals("Invalid ancestor class for the descendant class", classDescriptor.getSuperClassDescriptor().getClassName(), "org.apache.portals.graffito.jcr.testmodel.inheritance.Descendant");
//           assertEquals("Incorrect JcrName", classDescriptor.getJcrName("discriminator"),"discriminator");
//            descendandDescriptors = classDescriptor.getDescendantClassDescriptors();
//           assertEquals("Invalid number of descendants", descendandDescriptors.size(), 0);
//           assertTrue("SubDescendant  class doesn't have a node type per hierarchy strategy", classDescriptor.usesNodeTypePerHierarchyStrategy());
//           assertFalse("SubDescendant class  have a node type per hierarchy strategy", classDescriptor.usesNodeTypePerConcreteClassStrategy());
//           
       }
       catch (JcrMappingException e)
       {
             e.printStackTrace();
             fail("Impossible to retrieve the converter " + e);
       }
   }    
}