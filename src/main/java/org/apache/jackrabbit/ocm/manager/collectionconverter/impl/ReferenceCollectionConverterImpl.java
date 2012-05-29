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


import java.util.Iterator;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.jackrabbit.ocm.exception.ObjectContentManagerException;
import org.apache.jackrabbit.ocm.manager.collectionconverter.ManageableCollection;
import org.apache.jackrabbit.ocm.manager.collectionconverter.ManageableObjects;
import org.apache.jackrabbit.ocm.manager.collectionconverter.ManageableObjectsUtil;
import org.apache.jackrabbit.ocm.manager.objectconverter.ObjectConverter;
import org.apache.jackrabbit.ocm.mapper.Mapper;
import org.apache.jackrabbit.ocm.mapper.model.CollectionDescriptor;

/**
 *
 * This collection mapping strategy maps a collection of String into a JCR multi value property based on the reference type
 *
 * @author <a href="mailto:christophe.lombart@gmail.com">Christophe Lombart</a>
 */
public class ReferenceCollectionConverterImpl extends AbstractCollectionConverterImpl {

    /**
     * Constructor
     *
     * @param atomicTypeConverters
     * @param objectConverter
     * @param mapper
     */
    public ReferenceCollectionConverterImpl(Map atomicTypeConverters,
                                             ObjectConverter objectConverter,
                                             Mapper mapper) {
        super(atomicTypeConverters, objectConverter, mapper);
    }

    /**
     *
     * @see AbstractCollectionConverterImpl#doInsertCollection(Session, Node, CollectionDescriptor, ManageableCollection)
     */
    protected void doInsertCollection(Session session,
                                      Node parentNode,
                                      CollectionDescriptor collectionDescriptor,
                                      ManageableObjects objects) throws RepositoryException {
        addUuidProperties(session, parentNode, collectionDescriptor, objects);
    }

    /**
     *
     * @see AbstractCollectionConverterImpl#doUpdateCollection(Session, Node, CollectionDescriptor, ManageableCollection)
     */
    protected void doUpdateCollection(Session session,
                                 Node parentNode,
                                 CollectionDescriptor collectionDescriptor,
                                 ManageableObjects objects) throws RepositoryException {
        String jcrName = getCollectionJcrName(collectionDescriptor);

        // Delete existing values
        if (parentNode.hasProperty(jcrName)) {
            parentNode.setProperty(jcrName, (Value[]) null);
        }

        if (objects == null) {
            return;
        }


        addUuidProperties(session, parentNode, collectionDescriptor, objects);
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
            if (!parentNode.hasProperty(jcrName)) {
                return null;
            }
            Property property = parentNode.getProperty(jcrName);
            Value[] values = property.getValues();

            ManageableObjects objects = ManageableObjectsUtil.getManageableObjects(collectionFieldClass);
            for (int i = 0; i < values.length; i++) {

            	if (objects instanceof ManageableCollection)
                    ((ManageableCollection)objects).addObject(values[i].getString());
            	else
            		throw new ObjectContentManagerException(
            				"Unsupported data type in ReferenceCollectionConverter : " + objects.getClass().getName());
            }

            return objects;
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
        String jcrName = getCollectionJcrName(collectionDescriptor);

         if (!parentNode.hasProperty(jcrName)) {
            return true;
        }
        return false;
    }

	private void addUuidProperties(Session session, Node parentNode,
			             CollectionDescriptor collectionDescriptor, ManageableObjects objects)
	                     throws UnsupportedRepositoryOperationException, RepositoryException, VersionException, LockException, ConstraintViolationException {
		try {
            if (objects == null) {
                return;
            }

            String jcrName = getCollectionJcrName(collectionDescriptor);
            Value[] values = new Value[objects.getSize()];
            ValueFactory valueFactory = session.getValueFactory();
            Iterator collectionIterator = objects.getIterator();
            for (int i = 0; i < objects.getSize(); i++) {
                String uuid = (String) collectionIterator.next();
                values[i] = valueFactory.createValue(uuid, PropertyType.REFERENCE);

            }

            parentNode.setProperty(jcrName, values);
        }
        catch(ValueFormatException vfe) {
            throw new ObjectContentManagerException("Cannot insert collection field : "
                    + collectionDescriptor.getFieldName()
                    + " of class "
                    + collectionDescriptor.getClassDescriptor().getClassName(), vfe);
        }
	}

}