/*
 * Copyright 2000-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.portals.graffito.jcr.persistence.collectionconverter.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.portals.graffito.jcr.exception.PersistenceException;
import org.apache.portals.graffito.jcr.mapper.Mapper;
import org.apache.portals.graffito.jcr.mapper.model.ClassDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.CollectionDescriptor;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.ManageableCollection;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.ManageableCollectionUtil;
import org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter;

/** 
 * Default Collection Mapping/convertion implementation.
 * 
 * This collection mapping strategy maps a collection under an extra JCR node (specify by the jcrName in the CollectionDescriptor).
 * It is usefull when the node type "nt:unstructured" is applied to the collection elements. By this way, it is possible 
 * to distinguish the collection elements from the other main object fields.
 * 
 * If the collection element class contains an id (see the FieldDescriptor definition), this id value is used to build the collection element node.
 * Otherwise, the element node name is a simple indexed constant.
 * 
 * Example - without an id attribute:                 
 *   /test (Main object containing the collection field )
 *     /mycollection (extra node used to store the entire collection)
 *          /collection-element1 (node used to store the first collection element)
 *                /item-prop       
 *                ....
 *          /collection-element2 (node used to store the second collection element) 
 *          ...
 *          
 * Example - with an id attribute:                 
 *   /test (Main object containing the collection field )
 *     /mycollection (extra node used to store the entire collection)
 *          /aValue (id value assigned to the first element)
 *                /item-prop       
 *                ....
 *          /anotherValue (id value assigned to the first element) 
 *          ...

 * @author <a href="mailto:christophe.lombart@gmail.com">Christophe Lombart</a>
 * 
 */
public class DefaultCollectionConverterImpl extends AbstractCollectionConverterImpl 
{

    private static final String COLLECTION_ELEMENT_NAME = "collection-element";

    /**
     * Constructor 
     * @param atomicTypeConverters
     * @param objectConverter
     * @param mapper
     */
    public DefaultCollectionConverterImpl(Map atomicTypeConverters, ObjectConverter objectConverter, Mapper mapper)
    {
        super(atomicTypeConverters, objectConverter, mapper);
    }

    /**
     * 
     * @see org.apache.portals.graffito.jcr.persistence.collectionconverter.CollectionConverter#insertCollection(javax.jcr.Session, javax.jcr.Node, org.apache.portals.graffito.jcr.mapper.model.CollectionDescriptor, org.apache.portals.graffito.jcr.persistence.collectionconverter.ManageableCollection)
     */
    public void insertCollection(Session session, Node parentNode,  CollectionDescriptor collectionDescriptor, ManageableCollection collection)  
    {

        try
        {

            if (collection == null)
            {
                return;
            }

            String jcrName = collectionDescriptor.getJcrName();

            if (jcrName == null)
            {
                throw new PersistenceException("The JcrName attribute is not defined for the CollectionDescriptor : "
                        + collectionDescriptor.getCollectionConverterClassName());
            }

            Node collectionNode = parentNode.addNode(jcrName);

            Iterator collectionIterator = collection.getIterator();
            ClassDescriptor elementClassDescriptor = mapper.getClassDescriptor(Class.forName(collectionDescriptor
                    .getElementClassName()));

            int elementCollectionCount = 0;
            while (collectionIterator.hasNext())
            {
                Object item = collectionIterator.next();
                String elementJcrName = null;

                // If the element object has a unique id => the element jcr node name = the id value 
                if (elementClassDescriptor.hasIdField())
                {
                    String idFieldName = elementClassDescriptor.getIdFieldDescriptor().getFieldName();
                    elementJcrName = PropertyUtils.getNestedProperty(item, idFieldName).toString();
                }
                else
                {

                    elementCollectionCount++;
                    elementJcrName = COLLECTION_ELEMENT_NAME + elementCollectionCount;
                }

                objectConverter.insert(session, collectionNode, elementJcrName, item);
            }
        }
        catch (Exception e)
        {
            throw new PersistenceException("Impossible to insert the collection field : " + collectionDescriptor.getFieldName()
                    + "for " + collectionDescriptor.getElementClassName(), e);
        }

    }

    /**
     * 
     * @see org.apache.portals.graffito.jcr.persistence.collectionconverter.CollectionConverter#updateCollection(javax.jcr.Session, javax.jcr.Node, org.apache.portals.graffito.jcr.mapper.model.CollectionDescriptor, org.apache.portals.graffito.jcr.persistence.collectionconverter.ManageableCollection)
     */
    public void updateCollection(Session session, Node parentNode, CollectionDescriptor collectionDescriptor,  ManageableCollection collection)
    {
        try
        {

            String jcrName = collectionDescriptor.getJcrName();

            if (jcrName == null)
            {
                throw new PersistenceException("The JcrName attribute is not defined for the CollectionDescriptor : "
                        + collectionDescriptor.getCollectionConverterClassName());
            }

            if (collection == null)
            {
                if (parentNode.hasNode(jcrName))
                {
                    parentNode.getNode(jcrName).remove();

                }
                return;
            }

            Node collectionNode = parentNode.getNode(jcrName);
            ClassDescriptor elementClassDescriptor = mapper.getClassDescriptor(Class.forName(collectionDescriptor
                    .getElementClassName()));
            Iterator collectionIterator = collection.getIterator();
            int elementCollectionCount = 0;

            //  If the collection elements have not an id, it is not possible to find the matching JCR nodes => delete the complete collection             
            if (!elementClassDescriptor.hasIdField())
            {
                collectionNode.remove();
                collectionNode = parentNode.addNode(jcrName);
            }

            HashMap updatedItems = new HashMap();
            while (collectionIterator.hasNext())
            {
                Object item = collectionIterator.next();
                
                elementCollectionCount++;
                String elementJcrName = null;

                if (elementClassDescriptor.hasIdField())
                {

                    String idFieldName = elementClassDescriptor.getIdFieldDescriptor().getFieldName();
                    elementJcrName = PropertyUtils.getNestedProperty(item, idFieldName).toString();

                    // Update existing JCR Nodes
                    if (collectionNode.hasNode(elementJcrName))
                    {
                        objectConverter.update(session, collectionNode, elementJcrName, item);
                    }
                    else
                    {
                        // Add new collection elements
                        objectConverter.insert(session, collectionNode, elementJcrName, item);
                    }
                    
                    updatedItems.put(elementJcrName, item);

                }
                else
                {

                    elementCollectionCount++;
                    elementJcrName = COLLECTION_ELEMENT_NAME + elementCollectionCount;
                    objectConverter.insert(session, collectionNode, elementJcrName, item);
                }

            }

            // Delete JCR nodes that are not present in the collection
            if (elementClassDescriptor.hasIdField())
            {
                NodeIterator nodeIterator = collectionNode.getNodes();
                while (nodeIterator.hasNext())
                {
                    Node child = nodeIterator.nextNode();
                    if (! updatedItems.containsKey(child.getName()))
                    {
                        child.remove();
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new PersistenceException("Impossible to update the collection field : " + collectionDescriptor.getFieldName()
                    + "for " + collectionDescriptor.getElementClassName(), e);
        }

    }

    /**
     * @see org.apache.portals.graffito.jcr.persistence.collectionconverter.CollectionConverter#getCollection(javax.jcr.Session, javax.jcr.Node, org.apache.portals.graffito.jcr.mapper.model.CollectionDescriptor, java.lang.Class)
     */
    public ManageableCollection getCollection(Session session, Node parentNode, CollectionDescriptor collectionDescriptor, Class collectionFieldClass)
    {
        try
        {
            String jcrName = collectionDescriptor.getJcrName();
            if (!parentNode.hasNode(jcrName))
            {
                return null;
            }

            Node collectionNode = parentNode.getNode(jcrName);
            NodeIterator children = collectionNode.getNodes();

            ManageableCollection collection = ManageableCollectionUtil.getManageableCollection(collectionFieldClass);

            while (children.hasNext())
            {
                Node itemNode = children.nextNode();
                Object item = objectConverter.getObject(session, Class.forName(collectionDescriptor.getElementClassName()),
                        itemNode.getPath());
                collection.addObject(item);
            }

            return collection;
        }
        catch (Exception e)
        {
            throw new PersistenceException("Impossible to get the collection field : " + collectionDescriptor.getFieldName()
                    + "for " + collectionDescriptor.getElementClassName(), e);
        }
    }

}
