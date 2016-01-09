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

package org.apache.jackrabbit.ocm.manager.collectionconverter.impl;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.ocm.exception.ObjectContentManagerException;
import org.apache.jackrabbit.ocm.manager.collectionconverter.ManageableCollection;
import org.apache.jackrabbit.ocm.manager.collectionconverter.ManageableMap;
import org.apache.jackrabbit.ocm.manager.collectionconverter.ManageableObjects;
import org.apache.jackrabbit.ocm.manager.objectconverter.ObjectConverter;
import org.apache.jackrabbit.ocm.mapper.Mapper;
import org.apache.jackrabbit.ocm.mapper.model.CollectionDescriptor;

/**
 * Collection Mapping/convertion implementation used for String collections
 *
 * This collection mapping strategy maps a collection into a JCR property
 *
 * @author <a href="mailto:christophe.lombart@gmail.com">Christophe Lombart</a>
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 * @author <a href='mailto:boni.g@bioimagene.com'>Boni Gopalan</a>
 */
public class StringCollectionConverterImpl extends AbstractCollectionConverterImpl {
	
    /**
     * Constructor
     *
     * @param atomicTypeConverters
     * @param objectConverter
     * @param mapper
     */
    public StringCollectionConverterImpl(Map atomicTypeConverters,
                                         ObjectConverter objectConverter,
                                         Mapper mapper) {
        super(atomicTypeConverters, objectConverter, mapper);
    }

    /**
     *
     * @see AbstractCollectionConverterImpl#doInsertCollection(Session, Node, CollectionDescriptor, ManageableCollection)
     * 
     * ManageableObjects actually contains a Map
     * 
     */
    protected void doInsertCollection(Session session,
                                      Node parentNode,
                                      CollectionDescriptor collectionDescriptor,
                                      ManageableObjects objects) throws RepositoryException {
        try {
            if (objects == null) {
                return;
            }
            
            if( !(objects.getObjects() instanceof Map)){
                throw new ObjectContentManagerException("Input to StringCollectionConverter not Map");
            }

            String jcrName = getCollectionJcrName(collectionDescriptor);
            
            Node collectionNode;
            if( !parentNode.hasNode(jcrName)){
	            if (!StringUtils.isBlank(collectionDescriptor.getJcrType())) {
	                collectionNode = parentNode.addNode(jcrName, collectionDescriptor.getJcrType());
	            } else {
	                collectionNode = parentNode.addNode(jcrName);
	            }
            } else{
            	collectionNode = parentNode.getNode(jcrName);
            }
            
            Map<String,Object> valueMap = (Map<String,Object>) objects.getObjects();
            
            for( String key : valueMap.keySet()){
            	Object value = valueMap.get(key);
            	
            	if( value instanceof Boolean){
            		Boolean booleanValue = (Boolean) value;
            		collectionNode.setProperty(key, booleanValue);
            	} else if( value instanceof Integer){
            		Integer integerValue = (Integer) value;
            		collectionNode.setProperty(key, integerValue);            		
            	} else if( value instanceof Long){
            		Long longValue = (Long) value;
            		collectionNode.setProperty(key, longValue);            		
            	} else if( value instanceof String){
            		String stringValue = (String) value;
            		collectionNode.setProperty(key, stringValue);            		            		
            	} else if( value instanceof Date){
            		Calendar dateValue = new GregorianCalendar( );
            		dateValue.setTime((Date) value);
            		collectionNode.setProperty(key, dateValue);            		            		
            	} else {
            		collectionNode.setProperty(key, value.toString());
            	}
            }            
        }
        catch(ValueFormatException vfe) {
            throw new ObjectContentManagerException("Cannot insert collection field : "
                    + collectionDescriptor.getFieldName()
                    + " of class "
                    + collectionDescriptor.getClassDescriptor().getClassName(), vfe);
        }
    }

    /**
     *
     * @see AbstractCollectionConverterImpl#doUpdateCollection(Session, Node, CollectionDescriptor, ManageableCollection)
     * 
     * TODO: Delete properties that do not exist.
     * 
     */
    protected void doUpdateCollection(Session session,
                                 Node parentNode,
                                 CollectionDescriptor collectionDescriptor,
                                 ManageableObjects objects) throws RepositoryException {
    	
    	
    	doInsertCollection( session, parentNode, collectionDescriptor, objects);
    	
    }

    /**
     * @see AbstractCollectionConverterImpl#doGetCollection(Session, Node, CollectionDescriptor, Class)
     */
    protected ManageableObjects doGetCollection(Session session,
                                                   Node parentNode,
                                                   CollectionDescriptor collectionDescriptor,
                                                   Class collectionFieldClass) throws RepositoryException {
        try {        
        	
        	String jcrName = getCollectionJcrName(collectionDescriptor);
        	
        	if( !parentNode.hasNode(jcrName)){
        		return null;
        	}
        	
        	Node collectionNode = parentNode.getNode(jcrName);
        	
            ManageableMap returnObjects = new ManageableMapImpl(new HashMap<String,Object>());
            PropertyIterator properties = collectionNode.getProperties();
            while( properties.hasNext()){
            	Property property = (Property) properties.next();
            	if( !property.getName().startsWith("jcr:")){

            		switch (property.getValue().getType()){
            		case PropertyType.BOOLEAN:
            			returnObjects.addObject(property.getName(),property.getBoolean());
            			break;
            		case PropertyType.DECIMAL:
            			returnObjects.addObject(property.getName(),property.getDecimal());
            			break;
            		case PropertyType.LONG:
            			returnObjects.addObject(property.getName(),property.getLong());
            			break;            			
            		case PropertyType.STRING:	
            			returnObjects.addObject(property.getName(),property.getString());
            			break;
            		case PropertyType.DATE:	
            			returnObjects.addObject(property.getName(),property.getDate().getTime());
            			break;
            		}
            	}
            }
                        
            return returnObjects;
        }
        catch(ValueFormatException vfe) {
          throw new ObjectContentManagerException("Cannot get the collection field : "
                  + collectionDescriptor.getFieldName()
                  + "for class " + collectionDescriptor.getClassDescriptor().getClassName(),
                  vfe);
        }
    }

    /**
     * @see AbstractCollectionConverterImpl#doIsNull(Session, Node, CollectionDescriptor, Class)
     */
    protected boolean doIsNull(Session session,
                                              Node parentNode,
                                              CollectionDescriptor collectionDescriptor,
                                              Class collectionFieldClass) throws RepositoryException {

        return parentNode.getProperties().getSize()>0;
    }
}