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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.portals.graffito.jcr.exception.PersistenceException;
import org.apache.portals.graffito.jcr.mapper.Mapper;
import org.apache.portals.graffito.jcr.mapper.model.ClassDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.CollectionDescriptor;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.ManageableCollection;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.ManageableCollectionUtil;
import org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter;

/** 
 * Collection Mapping/convertion based on node type.
 * 
 * This collection mapping strategy maps a collection into several nodes based on specific node type.
 * 
 * 
 * If the collection element class contains an id (see the FieldDescriptor definition), this id value is used to build the collection element node.
 * Otherwise, the element node name is a simple indexed constant.
 * 
 * Example - without an id attribute:                 
 *   /test (Main object containing the collection field )
 *          /collection-element1 (node used to store the first collection element)
 *                /item-prop       
 *                ....
 *          /collection-element2 (node used to store the second collection element) 
 *          ...
 *          
 * Example - with an id attribute:                 
 *   /test (Main object containing the collection field ) 
 *          /aValue (id value assigned to the first element)
 *                /item-prop       
 *                ....
 *          /anotherValue (id value assigned to the first element) 
 *          ...
 *
 * @author <a href="mailto:christophe.lombart@gmail.com">Christophe Lombart</a>
 * 
 */
public class NTCollectionConverterImpl extends AbstractCollectionConverterImpl
{

	private final static Log log = LogFactory.getLog(NTCollectionConverterImpl.class);
	
    private static final String COLLECTION_ELEMENT_NAME = "collection-element";

    /**
     * Constructor
     * 
     * @param atomicTypeConverters
     * @param objectConverter
     * @param mapper
     */
    public NTCollectionConverterImpl(Map atomicTypeConverters, ObjectConverter objectConverter, Mapper mapper)
    {
        super(atomicTypeConverters, objectConverter, mapper);
    }

    /**
     * 
     * @see org.apache.portals.graffito.jcr.persistence.collectionconverter.CollectionConverter#insertCollection(javax.jcr.Session, javax.jcr.Node, org.apache.portals.graffito.jcr.mapper.model.CollectionDescriptor, org.apache.portals.graffito.jcr.persistence.collectionconverter.ManageableCollection)
     */
    public void insertCollection(Session session, Node parentNode, CollectionDescriptor collectionDescriptor, ManageableCollection collection)
    {

        try
        {

            if (collection == null)
            {                
                return;
            }


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

                objectConverter.insert(session, parentNode, elementJcrName, item);
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
    public void updateCollection(Session session, Node parentNode, CollectionDescriptor collectionDescriptor, ManageableCollection collection)
    {
        try
        {

            ClassDescriptor elementClassDescriptor = mapper.getClassDescriptor(Class.forName(collectionDescriptor
                    .getElementClassName()));
            
            if (collection == null)
            {
                this.deleteCollectionItems(session, parentNode, elementClassDescriptor.getJcrNodeType());
                return;
            }

            if (!elementClassDescriptor.hasIdField())
            {
               this.deleteCollectionItems(session, parentNode, elementClassDescriptor.getJcrNodeType());
            }
            
            Iterator collectionIterator = collection.getIterator();
            int elementCollectionCount = 0;

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
                    if (parentNode.hasNode(elementJcrName))
                    {
                        objectConverter.update(session, parentNode, elementJcrName, item);
                    }
                    else
                    {
                        // Add new collection elements
                        objectConverter.insert(session, parentNode, elementJcrName, item);
                    }
                    
                    updatedItems.put(elementJcrName, item);

                }
                else
                {
                    
                    elementJcrName = COLLECTION_ELEMENT_NAME + elementCollectionCount;
                    objectConverter.insert(session, parentNode, elementJcrName, item);
                }

            }

            // Delete JCR nodes that are not present in the collection
            if (elementClassDescriptor.hasIdField())
            {
                Iterator nodeIterator = this.getCollectionNodes(session, parentNode, elementClassDescriptor.getJcrNodeType()).iterator();
                while (nodeIterator.hasNext())
                {
                    Node child = (Node) nodeIterator.next();
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
            ClassDescriptor elementClassDescriptor = mapper.getClassDescriptor(Class.forName(collectionDescriptor
                    .getElementClassName()));                        
            Iterator children = this.getCollectionNodes(session, parentNode, elementClassDescriptor.getJcrNodeType()).iterator();

            ManageableCollection collection = ManageableCollectionUtil.getManageableCollection(collectionFieldClass);

            while (children.hasNext())
            {
                Node itemNode = (Node) children.next();
                log.debug("Collection node found : " + itemNode.getPath());
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

    private Collection getCollectionNodes (Session session, Node parentNode, String itemNodeType) throws Exception
    {
    	  
           ArrayList collectionNodes = new ArrayList();

           // TODO : review this workaround used to support version nodes  
           // Searching on the version storage has some bugs => loop on all child noded and check the property jcr:frozenPrimaryType
           // I have to investigate in more detail what's happen exactly  
    	   if (! parentNode.getPath().startsWith("/jcr:system/jcr:versionStorage"))
    	   {               
               NodeIterator nodeIterator = parentNode.getNodes();
               while (nodeIterator.hasNext())
               {        	   
            	   Node child = nodeIterator.nextNode();

            	   if (child.isNodeType(itemNodeType))
            	   {
                       collectionNodes.add(child);
            	   }
               }    		   
    	   }
    	   else
    	   {
    		                  
               NodeIterator nodeIterator = parentNode.getNodes();
               while (nodeIterator.hasNext())
               {        	   
            	   Node child = nodeIterator.nextNode();

            	   if (child.getProperty("jcr:frozenPrimaryType").getString().equals(itemNodeType))
            	   {
                       collectionNodes.add(child);
            	   }
               } 
    		   
    	   }
    	   
           return collectionNodes;
           
    }
    
    private void deleteCollectionItems(Session session, Node parentNode, String itemNodeType) throws Exception
    {
           Iterator nodeIterator = this.getCollectionNodes(session, parentNode, itemNodeType).iterator(); 
           while (nodeIterator.hasNext())
           {
               Node node = (Node) nodeIterator.next();
               node.remove();
           }
    }
    
    
}
