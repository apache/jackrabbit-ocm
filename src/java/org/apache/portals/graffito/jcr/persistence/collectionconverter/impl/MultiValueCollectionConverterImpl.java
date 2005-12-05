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

import java.util.Iterator;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.portals.graffito.jcr.exception.PersistenceException;
import org.apache.portals.graffito.jcr.mapper.Mapper;
import org.apache.portals.graffito.jcr.mapper.model.CollectionDescriptor;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.AtomicTypeConverter;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.ManageableCollection;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.ManageableCollectionUtil;
import org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter;

/** 
 * Collection Mapping/convertion implementation used for multi values properties
 * 
 * This collection mapping strategy maps a collection into a JCR multi value property
 *
 * @author <a href="mailto:christophe.lombart@gmail.com">Christophe Lombart</a>
 * 
 */
public class MultiValueCollectionConverterImpl extends AbstractCollectionConverterImpl
{
    /**
     * Constructor 
     * 
     * @param atomicTypeConverters
     * @param objectConverter
     * @param mapper
     */
    public MultiValueCollectionConverterImpl(Map atomicTypeConverters, ObjectConverter objectConverter, Mapper mapper)
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

            String jcrName = collectionDescriptor.getJcrName();

            if (jcrName == null)
            {
                throw new PersistenceException("The JcrName attribute is not defined for the CollectionDescriptor : "
                        + collectionDescriptor.getCollectionConverterClassName());
            }
    
            Value[] values = new Value[collection.getSize()];
            Iterator collectionIterator = collection.getIterator();    
            for (int i=0; i<collection.getSize();i++)
            {
                Object fieldValue  = collectionIterator.next();
                AtomicTypeConverter atomicTypeConverter = (AtomicTypeConverter) atomicTypeConverters.get(fieldValue.getClass());
                values[i] = atomicTypeConverter.getValue(fieldValue);     
            }
            
            parentNode.setProperty(jcrName, values);
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
        	String jcrName = collectionDescriptor.getJcrName();

            if (collection == null)
            {
                if (parentNode.hasProperty(jcrName))
                {
                	parentNode.setProperty(jcrName, (Value[]) null);
                }
                return;
            }

            if (jcrName == null)
            {
                throw new PersistenceException("The JcrName attribute is not defined for the CollectionDescriptor : "
                        + collectionDescriptor.getCollectionConverterClassName());
            }
    
            // Delete existing values
            parentNode.setProperty(jcrName, (Value[]) null);
            
            // Add all collection element into an Value array
            Value[] values = new Value[collection.getSize()];
            Iterator collectionIterator = collection.getIterator();    
            for (int i=0; i<collection.getSize();i++)
            {
                Object fieldValue  = collectionIterator.next();
                AtomicTypeConverter atomicTypeConverter = (AtomicTypeConverter) atomicTypeConverters.get(fieldValue.getClass());
                values[i] = atomicTypeConverter.getValue(fieldValue);     
            }
            
            parentNode.setProperty(jcrName, values);
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
            if( ! parentNode.hasProperty(jcrName))
            {
            	return null;
            }
            Property property = parentNode.getProperty(jcrName);
            Value[] values = property.getValues();
            
            ManageableCollection collection = ManageableCollectionUtil.getManageableCollection(collectionFieldClass);
            for (int i=0;i<values.length;i++)
            {
            	String elementClassName = collectionDescriptor.getElementClassName();
            	AtomicTypeConverter atomicTypeConverter = (AtomicTypeConverter) atomicTypeConverters.get(Class.forName(elementClassName));
                collection.addObject(atomicTypeConverter.getObject(values[i]));	
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
