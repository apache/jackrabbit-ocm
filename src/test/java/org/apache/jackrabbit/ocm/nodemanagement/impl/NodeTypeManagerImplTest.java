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
package org.apache.jackrabbit.ocm.nodemanagement.impl;

import java.io.IOException;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.jackrabbit.core.nodetype.InvalidNodeTypeDefException;
import org.apache.jackrabbit.ocm.AbstractRepositoryTestBase;
import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.apache.jackrabbit.ocm.mapper.model.BeanDescriptor;
import org.apache.jackrabbit.ocm.mapper.model.ClassDescriptor;
import org.apache.jackrabbit.ocm.mapper.model.CollectionDescriptor;
import org.apache.jackrabbit.ocm.mapper.model.FieldDescriptor;
import org.apache.jackrabbit.ocm.nodemanagement.exception.NamespaceCreationException;
import org.apache.jackrabbit.ocm.nodemanagement.exception.NodeTypeCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** JUnit test for NodeTypeManagerImpl.
 *
 * @author <a href="mailto:okiessler@apache.org">Oliver Kiessler</a>
 */
public class NodeTypeManagerImplTest extends AbstractRepositoryTestBase {

    /**
     * Logger instance.
     */
    private static final Logger log =
        LoggerFactory.getLogger(NodeTypeManagerImpl.class);

    /** Class to test.
     */
    private NodeTypeManagerImpl nodeTypeManagerImpl
                = new NodeTypeManagerImpl();

    /** Returns testsuite.
     * @return suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(NodeTypeManagerImplTest.class);
        return suite;
    }

    @Override
    protected ObjectContentManager createObjectContentManager(Session session) throws RepositoryException {
        // no ObjectContentManager needed for this test
        return null;
    }

    @Override
    protected void registerNodeTypes(Session session)
            throws InvalidNodeTypeDefException, javax.jcr.RepositoryException, IOException {
         // do not register any node types at startup hence override the one from AbstractRepositoryTestBase
        return;
    }

    public void testCreateNamespace() throws Exception
    {

        Session session = getSession();
        nodeTypeManagerImpl.createNamespace(session,
                "ocmtest", "http://www.test.com/test-uri");

        assertEquals(session.getWorkspace().getNamespaceRegistry().getPrefix("http://www.test.com/test-uri"), "ocmtest");
        assertEquals(session.getWorkspace().getNamespaceRegistry().getURI("ocmtest"), "http://www.test.com/test-uri");

        boolean failed = false;

        try
        {
            nodeTypeManagerImpl.createNamespace(session,
                    "ocmtest", "http://www.test.com/test-uri");
        } catch (NamespaceCreationException nce) {
            // expected
            failed = true;
        }

        assertTrue(failed);
    }

    public void testCreateSingleNodeType() throws Exception
    {
        ClassDescriptor classDescriptor = new ClassDescriptor();
        classDescriptor.setClassName("test.TestClass");
        classDescriptor.setJcrType("ocm:test2");
        classDescriptor.setJcrSuperTypes("nt:base");

        FieldDescriptor field1 = new FieldDescriptor();
        field1.setFieldName("a");
        field1.setJcrName("ocm:a");
        field1.setJcrType("String");
        field1.setJcrAutoCreated(true);
        field1.setJcrMandatory(true);
        field1.setJcrMultiple(true);
        classDescriptor.addFieldDescriptor(field1);

        FieldDescriptor field2 = new FieldDescriptor();
        field2.setFieldName("b");
        field2.setJcrName("ocm:b");
        field2.setJcrType("Long");
        field1.setJcrAutoCreated(false);
        field1.setJcrMandatory(true);
        field1.setJcrMultiple(false);
        classDescriptor.addFieldDescriptor(field2);

        nodeTypeManagerImpl.createSingleNodeType(getSession(), classDescriptor);

        NodeType testNodeType = getSession().getWorkspace().getNodeTypeManager().getNodeType("ocm:test2");
        assertNotNull(testNodeType);
        assertFalse(testNodeType.isMixin());
        assertEquals(testNodeType.getName(), "ocm:test2");
        assertEquals(testNodeType.getSupertypes().length, 1);
        assertEquals(testNodeType.getSupertypes()[0].getName(), "nt:base");

        // 2 defined in ocm:test2 and 2 inherited from nt:base
        assertEquals(testNodeType.getPropertyDefinitions().length, 4);

        assertTrue(containsProperty("ocm:a", testNodeType.getPropertyDefinitions()));
        assertTrue(containsProperty("ocm:b", testNodeType.getPropertyDefinitions()));
        assertTrue(containsProperty("jcr:primaryType", testNodeType.getPropertyDefinitions()));
        assertTrue(containsProperty("jcr:mixinTypes", testNodeType.getPropertyDefinitions()));

        PropertyDefinition propDef1 = getPropertyDefinition(testNodeType.getPropertyDefinitions(), "ocm:a");
        log.info(showPropertyDefinition(propDef1));
        // TODO test all properties

        PropertyDefinition propDef2 = getPropertyDefinition(testNodeType.getPropertyDefinitions(), "ocm:b");
        log.info(showPropertyDefinition(propDef2));
        // TODO test all properties
    }

    public void testCreateSingleNodeTypeNoNamespace() throws Exception
    {
        ClassDescriptor classDescriptor = new ClassDescriptor();
        classDescriptor.setClassName("test.Test3Class");
        classDescriptor.setJcrType("test3");
        classDescriptor.setJcrSuperTypes("nt:base");

        FieldDescriptor field1 = new FieldDescriptor();
        field1.setFieldName("a");
        field1.setJcrName("a");
        field1.setJcrType("String");
        classDescriptor.addFieldDescriptor(field1);

        nodeTypeManagerImpl.createSingleNodeType(getSession(), classDescriptor);

        NodeType test3 = getSession().getWorkspace().getNodeTypeManager().getNodeType("test3");
        assertNotNull(test3);
        assertFalse(test3.isMixin());
        assertEquals(test3.getName(), "test3");
        assertEquals(test3.getSupertypes().length, 1);
        assertEquals(test3.getSupertypes()[0].getName(), "nt:base");
    }

    public void testCreateSingleNodeTypeNoJcrNodeTypeSet() throws Exception
    {
        ClassDescriptor classDescriptor = new ClassDescriptor();
        classDescriptor.setClassName("test.Test4Class");
        classDescriptor.setJcrSuperTypes("nt:base");

        FieldDescriptor field1 = new FieldDescriptor();
        field1.setFieldName("a");
        field1.setJcrName("a");
        field1.setJcrType("String");
        classDescriptor.addFieldDescriptor(field1);

        nodeTypeManagerImpl.createSingleNodeType(getSession(), classDescriptor);

        NodeType test4 = getSession().getWorkspace().getNodeTypeManager().getNodeType("test.Test4Class");
        assertNotNull(test4);
        assertFalse(test4.isMixin());
        assertEquals(test4.getName(), "test.Test4Class");
        assertEquals(test4.getSupertypes().length, 1);
        assertEquals(test4.getSupertypes()[0].getName(), "nt:base");
    }

    public void testCreateSingleNodeTypeIncompleteFieldDescriptorProperties() throws Exception
    {
        ClassDescriptor classDescriptor = new ClassDescriptor();
        classDescriptor.setClassName("test.Test5Class");
        classDescriptor.setJcrType("ocm:test5");
        classDescriptor.setJcrSuperTypes("ocm:test2");

        FieldDescriptor field1 = new FieldDescriptor();
        field1.setFieldName("abc");
        classDescriptor.addFieldDescriptor(field1);

        nodeTypeManagerImpl.createSingleNodeType(getSession(), classDescriptor);

        NodeType test5 = getSession().getWorkspace().getNodeTypeManager().getNodeType("ocm:test5");
        assertNotNull(test5);
        assertFalse(test5.isMixin());
        assertEquals(test5.getName(), "ocm:test5");
        // nt:base and ocm:test2
        assertEquals(test5.getSupertypes().length, 2);
        assertTrue(containsSuperType("ocm:test2", test5.getSupertypes()));
        assertTrue(containsSuperType("nt:base", test5.getSupertypes()));
        assertTrue(containsProperty("abc", test5.getPropertyDefinitions()));
    }

    public void testCreateSingleNodeTypeNtNamespace() throws Exception
    {
        ClassDescriptor classDescriptor = new ClassDescriptor();
        classDescriptor.setClassName("test.Test6Class");
        classDescriptor.setJcrType("nt:test3");
        classDescriptor.setJcrSuperTypes("nt:base");

        FieldDescriptor field1 = new FieldDescriptor();
        field1.setFieldName("a");
        field1.setJcrName("a");
        field1.setJcrType("String");
        classDescriptor.addFieldDescriptor(field1);

        boolean failed = false;

        try
        {
            nodeTypeManagerImpl.createSingleNodeType(getSession(), classDescriptor);
        }
        catch (NodeTypeCreationException nce)
        {
            // excepted
            failed = true;
        }

        assertTrue(failed);
    }

    public void testCreateSingleNodeTypeWithPropertyForCollection() throws Exception
    {
        ClassDescriptor classDescriptor = new ClassDescriptor();
        classDescriptor.setClassName("test.Test9Class");
        classDescriptor.setJcrType("ocm:test9");
        classDescriptor.setJcrSuperTypes("nt:base");

        CollectionDescriptor collection1 = new CollectionDescriptor();
        collection1.setFieldName("a");
        collection1.setJcrName("a");
        collection1.setJcrType("String");

        classDescriptor.addCollectionDescriptor(collection1);

        nodeTypeManagerImpl.createSingleNodeType(getSession(), classDescriptor);

        NodeType test9 = getSession().getWorkspace().getNodeTypeManager().getNodeType("ocm:test9");
        assertNotNull(test9);
        // not check node type definition, assuming other tests have done that

        // assert property definition a
        PropertyDefinition propDef = getPropertyDefinition(test9.getPropertyDefinitions(), "a");
        assertNotNull(propDef);
        assertEquals(propDef.getRequiredType(), PropertyType.STRING);
    }

    public void testCreateSingleNodeTypeWithPropertyForBean() throws Exception
    {
        ClassDescriptor classDescriptor = new ClassDescriptor();
        classDescriptor.setClassName("test.Test10Class");
        classDescriptor.setJcrType("ocm:test10");
        classDescriptor.setJcrSuperTypes("nt:base");

        BeanDescriptor bean1 = new BeanDescriptor();
        bean1.setFieldName("a");
        bean1.setJcrName("a");
        bean1.setJcrType("String");
        classDescriptor.addBeanDescriptor(bean1);

        nodeTypeManagerImpl.createSingleNodeType(getSession(), classDescriptor);

        NodeType test10 = getSession().getWorkspace().getNodeTypeManager().getNodeType("ocm:test10");
        assertNotNull(test10);
        // not check node type definition, assuming other tests have done that

        // assert property definition a
        PropertyDefinition propDef = getPropertyDefinition(test10.getPropertyDefinitions(), "a");
        assertNotNull(propDef);
        assertEquals(propDef.getRequiredType(), PropertyType.STRING);

    }

    public void testCreateSingleNodeTypeWithPropertyForCollectionDefinitionConflict() throws Exception
    {
        ClassDescriptor classDescriptor = new ClassDescriptor();
        classDescriptor.setClassName("test.Test13Class");
        classDescriptor.setJcrType("ocm:test13");
        classDescriptor.setJcrSuperTypes("nt:base");

        CollectionDescriptor collection1 = new CollectionDescriptor();
        collection1.setFieldName("a");
        collection1.setJcrName("a");
        collection1.setJcrType("String");
        classDescriptor.addCollectionDescriptor(collection1);

        nodeTypeManagerImpl.createSingleNodeType(getSession(), classDescriptor);

        NodeType test13 = getSession().getWorkspace().getNodeTypeManager().getNodeType("ocm:test13");
        assertNotNull(test13);
        // not check node type definition, assuming other tests have done that

        // assert property definition a
        PropertyDefinition propDef = getPropertyDefinition(test13.getPropertyDefinitions(), "a");
        assertNotNull(propDef);
        assertEquals(propDef.getRequiredType(), PropertyType.STRING);
    }

    public void testCreateSingleNodeTypeWithPropertyForBeanDefinitionConflict() throws Exception
    {
        ClassDescriptor classDescriptor = new ClassDescriptor();
        classDescriptor.setClassName("test.Test14Class");
        classDescriptor.setJcrType("ocm:test14");
        classDescriptor.setJcrSuperTypes("nt:base");

        BeanDescriptor bean1 = new BeanDescriptor();
        bean1.setFieldName("a");
        bean1.setJcrName("a");
        bean1.setJcrType("String");
        classDescriptor.addBeanDescriptor(bean1);

        nodeTypeManagerImpl.createSingleNodeType(getSession(), classDescriptor);

        NodeType test14 = getSession().getWorkspace().getNodeTypeManager().getNodeType("ocm:test14");
        assertNotNull(test14);
        // not check node type definition, assuming other tests have done that

        // assert property definition a
        PropertyDefinition propDef = getPropertyDefinition(test14.getPropertyDefinitions(), "a");
        assertNotNull(propDef);
        assertEquals(propDef.getRequiredType(), PropertyType.STRING);

    }

    public void testCreateSingleNodeTypeWithChildNodeForCollection() throws Exception
    {
        ClassDescriptor classDescriptor = new ClassDescriptor();
        classDescriptor.setClassName("test.Test11Class");
        classDescriptor.setJcrType("ocm:test11");
        classDescriptor.setJcrSuperTypes("nt:base");

        CollectionDescriptor collection1 = new CollectionDescriptor();
        collection1.setFieldName("a");
        collection1.setJcrName("b");
        collection1.setJcrType("nt:unstructured");
        classDescriptor.addCollectionDescriptor(collection1);

        nodeTypeManagerImpl.createSingleNodeType(getSession(), classDescriptor);

        NodeType test11 = getSession().getWorkspace().getNodeTypeManager().getNodeType("ocm:test11");
        assertNotNull(test11);
        // not check node type definition, assuming other tests have done that

        // assert child node definition a
        NodeDefinition nodeDef = getChildNodeDefinition(test11.getChildNodeDefinitions(), "b");
        assertNotNull(nodeDef);
        assertNotNull(nodeDef.getRequiredPrimaryTypes());
        assertEquals(nodeDef.getRequiredPrimaryTypes().length, 1);
        assertEquals(nodeDef.getRequiredPrimaryTypes()[0].getName(), "nt:unstructured");
    }

    public void testCreateSingleNodeTypeWithChildNodeForBean() throws Exception
    {
        ClassDescriptor classDescriptor = new ClassDescriptor();
        classDescriptor.setClassName("test.Test12Class");
        classDescriptor.setJcrType("ocm:test12");
        classDescriptor.setJcrSuperTypes("nt:base");

        BeanDescriptor bean1 = new BeanDescriptor();
        bean1.setFieldName("a");
        bean1.setJcrName("b");
        bean1.setJcrType("nt:unstructured");
        classDescriptor.addBeanDescriptor(bean1);

        nodeTypeManagerImpl.createSingleNodeType(getSession(), classDescriptor);

        NodeType test12 = getSession().getWorkspace().getNodeTypeManager().getNodeType("ocm:test12");
        assertNotNull(test12);
        // not check node type definition, assuming other tests have done that

        // assert property definition a
        NodeDefinition nodeDef = getChildNodeDefinition(test12.getChildNodeDefinitions(), "b");
        assertNotNull(nodeDef);
        assertNotNull(nodeDef.getRequiredPrimaryTypes());
        assertEquals(nodeDef.getRequiredPrimaryTypes().length, 1);
        assertEquals(nodeDef.getRequiredPrimaryTypes()[0].getName(), "nt:unstructured");
    }

    public void testCreateNodeTypes() throws Exception
    {
        ClassDescriptor classDescriptor = new ClassDescriptor();
        classDescriptor.setClassName("test.Test6Class");
        classDescriptor.setJcrType("ocm:test6");
        classDescriptor.setJcrSuperTypes("nt:base");

        FieldDescriptor field1 = new FieldDescriptor();
        field1.setFieldName("a");
        field1.setJcrName("ocm:a");
        field1.setJcrType("String");
        classDescriptor.addFieldDescriptor(field1);

        FieldDescriptor field2 = new FieldDescriptor();
        field2.setFieldName("b");
        field2.setJcrName("ocm:b");
        field2.setJcrType("Long");
        classDescriptor.addFieldDescriptor(field2);

        ClassDescriptor classDescriptor2 = new ClassDescriptor();
        classDescriptor2.setClassName("test.Test7Class");
        classDescriptor2.setJcrType("ocm:test7");
        classDescriptor2.setJcrSuperTypes("nt:base");

        FieldDescriptor field3 = new FieldDescriptor();
        field3.setFieldName("a");
        field3.setJcrName("ocm:a");
        field3.setJcrType("String");
        classDescriptor2.addFieldDescriptor(field3);

        FieldDescriptor field4 = new FieldDescriptor();
        field4.setFieldName("b");
        field4.setJcrName("ocm:b");
        field4.setJcrType("Long");
        classDescriptor2.addFieldDescriptor(field4);

        ClassDescriptor[] classDescriptorArray = new ClassDescriptor[2];
        classDescriptorArray[0] = classDescriptor;
        classDescriptorArray[1] = classDescriptor2;

        nodeTypeManagerImpl.createNodeTypes(getSession(), classDescriptorArray);

        NodeType test6 = getSession().getWorkspace().getNodeTypeManager().getNodeType("ocm:test6");
        assertNotNull(test6);

        NodeType test7 = getSession().getWorkspace().getNodeTypeManager().getNodeType("ocm:test7");
        assertNotNull(test7);
    }

    /** Returns true if a given property is found in an array of property
     * definitions.
     *
     * @param propertyName Name of property to find
     * @param propDefs Properties of a node type
     * @return true/false
     */
    protected boolean containsProperty(String propertyName,
        PropertyDefinition[] propDefs)
    {
        boolean found = false;

        for (int i = 0; i < propDefs.length; i++)
        {
            if (propDefs[i].getName().equals(propertyName))
            {
                found = true;
                break;
            }
        }

        return found;
    }

    /** Returns a property defintion identified by its name.
     *
     * @param propDefs All property definitions of a node type
     * @param propertyName Name of property definition
     * @return found
     */
    protected PropertyDefinition getPropertyDefinition(PropertyDefinition[] propDefs,
        String propertyName)
    {
        PropertyDefinition found = null;

        for (int i = 0; i < propDefs.length; i++)
        {
            if (propDefs[i].getName().equals(propertyName))
            {
                found = propDefs[i];
                break;
            }
        }

        return found;
    }

    /** Returns true if a given child node is found in an array of child node
     * definitions.
     *
     * @param childNodeName Name of child node to find
     * @param childNodeDefs Child nodes of a node type
     * @return true/false
     */
    protected boolean containsChildNode(String childNodeName,
            NodeDefinition[] childNodeDefs)
    {
        boolean found = false;

        for (int i = 0; i < childNodeDefs.length; i++)
        {
           if (childNodeDefs[i].getName().equals(childNodeName))
           {
               found = true;
               break;
           }
        }

        return found;
    }

    /** Returns a property defintion identified by its name.
     *
     * @param childNodeDefs Child nodes of a node type
     * @param childNodeName Name of child node to find
     * @return found
     */
    protected NodeDefinition getChildNodeDefinition(NodeDefinition[] childNodeDefs,
            String childNodeName)
    {
        NodeDefinition found = null;

        for (int i = 0; i < childNodeDefs.length; i++)
        {
           if (childNodeDefs[i].getName().equals(childNodeName))
           {
               found = childNodeDefs[i];
               break;
           }
        }

        return found;
    }

    /** Returns true if a given super type is found in an arry of super types.
     *
     * @param superType Name of super type to find
     * @param nodeTypes
     * @return true/false
     */
    protected boolean containsSuperType(String superType,
            NodeType[] nodeTypes)
    {
        boolean found = false;

        for (int i = 0; i < nodeTypes.length; i++)
        {
           if (nodeTypes[i].getName().equals(superType))
           {
               found = true;
               break;
           }
        }

        return found;
    }

    /** Returns true if a property was found in an array of property defintions.
     *
     * @param definitions PropertyDefinition[]
     * @param propertyName Name of property to find
     * @return true/false
     */
    public boolean containsPropertyDefintion(PropertyDefinition[] definitions,
                                             String propertyName)
    {
        boolean found = false;

        if (definitions != null && definitions.length > 0)
        {
            for (int i = 0; i < definitions.length; i++)
            {
                if (definitions[i].getName().equals(propertyName))
                {
                    found = true;
                }
            }
        }

        return found;
    }


    /**
     *
     * @param propDef
     * @return
     */
    protected String showPropertyDefinition(PropertyDefinition propDef)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("----");
        sb.append("\nName: " + propDef.getName());
        sb.append("\nAutocreated: " + propDef.isAutoCreated());
        sb.append("\nMandatory: " + propDef.isMandatory());
        sb.append("\n----");
        return sb.toString();
    }
}
