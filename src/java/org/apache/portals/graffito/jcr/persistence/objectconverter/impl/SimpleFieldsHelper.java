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
package org.apache.portals.graffito.jcr.persistence.objectconverter.impl;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.portals.graffito.jcr.exception.JcrMappingException;
import org.apache.portals.graffito.jcr.exception.PersistenceException;
import org.apache.portals.graffito.jcr.mapper.model.ClassDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.FieldDescriptor;
import org.apache.portals.graffito.jcr.persistence.PersistenceConstant;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.AtomicTypeConverter;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.AtomicTypeConverterProvider;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.impl.NullTypeConverterImpl;
import org.apache.portals.graffito.jcr.reflection.ReflectionUtils;

/**
 * Helper class used to map simple fields.
 * 
 * @author <a href="mailto:christophe.lombart@gmail.com">Lombart Christophe </a>
 * 
 */
public class SimpleFieldsHelper
{

	private final static Log log = LogFactory.getLog(SimpleFieldsHelper.class);

	private static final AtomicTypeConverter NULL_CONVERTER = new NullTypeConverterImpl();

	private AtomicTypeConverterProvider atomicTypeConverterProvider;

	/**
	 * Constructor
	 * 
	 * @param converterProvider The atomic type converter provider
	 * 
	 */
	public SimpleFieldsHelper(AtomicTypeConverterProvider converterProvider) 
	{
		this.atomicTypeConverterProvider = converterProvider;
	}

	
	/**
	 * Retrieve simple fields (atomic fields)
	 * 
	 * @throws JcrMappingException
	 * @throws org.apache.portals.graffito.jcr.exception.RepositoryException
	 */
	public Object retrieveSimpleFields(Session session, ClassDescriptor classDescriptor, Node node, Object object) 
	{
		Object initializedBean = object;
		try 
		{
			Iterator fieldDescriptorIterator = classDescriptor.getFieldDescriptors().iterator();

			while (fieldDescriptorIterator.hasNext()) 
			{
				FieldDescriptor fieldDescriptor = (FieldDescriptor) fieldDescriptorIterator.next();

				String fieldName = fieldDescriptor.getFieldName();
				String propertyName = fieldDescriptor.getJcrName();

				if (fieldDescriptor.isPath()) 
				{
					// HINT: lazy initialize target bean - The bean can be null when it is inline
					if (null == initializedBean) 
					{
						initializedBean = ReflectionUtils.newInstance(classDescriptor.getClassName());
					}

					ReflectionUtils.setNestedProperty(initializedBean, fieldName, node.getPath());
					
				} 
				else
				{
					if (classDescriptor.usesNodeTypePerHierarchyStrategy() && classDescriptor.hasDiscriminator()) 
					{
						if ( ! node.hasProperty(PersistenceConstant.DISCRIMINATOR_PROPERTY_NAME)) 
						{
							throw new PersistenceException("Class '" + classDescriptor.getClassName() + "' has not a discriminator property.");
						}					
					}
					if (node.hasProperty(propertyName)) 
					{
						Value propValue = node.getProperty(propertyName).getValue();
						// HINT: lazy initialize target bean - The bean can be null when it is inline
						if (null != propValue && null == initializedBean) {
							initializedBean = ReflectionUtils.newInstance(classDescriptor.getClassName());
						}

						AtomicTypeConverter converter = getAtomicTypeConverter(fieldDescriptor, initializedBean, fieldName);
						Object fieldValue = converter.getObject(propValue);
						ReflectionUtils.setNestedProperty(initializedBean, fieldName, fieldValue);
					} 
					else 
					{
						log.warn("Class '" + classDescriptor.getClassName() + "' has an unmapped property : " + propertyName);
					}
				}
				
			}
		} catch (ValueFormatException vfe) {
			throw new PersistenceException("Cannot retrieve properties of object " + object + " from node " + node, vfe);
		} catch (RepositoryException re) {
			throw new org.apache.portals.graffito.jcr.exception.RepositoryException("Cannot retrieve properties of object "
					+ object + " from node " + node, re);
		}

		return initializedBean;
	}
	
	public void storeSimpleFields(Session session, Object object, ClassDescriptor classDescriptor, Node objectNode) {
		try {
			ValueFactory valueFactory = session.getValueFactory();

			Iterator fieldDescriptorIterator = classDescriptor.getFieldDescriptors().iterator();
			while (fieldDescriptorIterator.hasNext()) {
				FieldDescriptor fieldDescriptor = (FieldDescriptor) fieldDescriptorIterator.next();

				String fieldName = fieldDescriptor.getFieldName();
				String jcrName = fieldDescriptor.getJcrName();

				// Of course, Path field is not stored as property
				if (fieldDescriptor.isPath()) {
					continue;
				}

				boolean protectedProperty = fieldDescriptor.isJcrProtected();

				if (objectNode.hasProperty(jcrName)) {
					protectedProperty = objectNode.getProperty(jcrName).getDefinition().isProtected();
				}

				if (!protectedProperty) { // DO NOT TRY TO WRITE PROTECTED  PROPERTIES
					Object fieldValue = ReflectionUtils.getNestedProperty(object, fieldName);
					AtomicTypeConverter converter = getAtomicTypeConverter(fieldDescriptor, object, fieldName);
					Value value = converter.getValue(valueFactory, fieldValue);

					// Check if the node property is "autocreated"
					boolean autoCreated = fieldDescriptor.isJcrAutoCreated();

					if (objectNode.hasProperty(jcrName)) {
						autoCreated = objectNode.getProperty(jcrName).getDefinition().isAutoCreated();
					}

					if (!autoCreated) {
						// Check if mandatory property are not null
						checkMandatoryProperty(objectNode, fieldDescriptor, value);
					}

					objectNode.setProperty(jcrName, value);
				}
			}
		} catch (ValueFormatException vfe) {
			throw new PersistenceException("Cannot persist properties of object " + object + ". Value format exception.", vfe);
		} catch (VersionException ve) {
			throw new PersistenceException("Cannot persist properties of object " + object + ". Versioning exception.", ve);
		} catch (LockException le) {
			throw new PersistenceException("Cannot persist properties of object " + object + " on locked node.", le);
		} catch (ConstraintViolationException cve) {
			throw new PersistenceException("Cannot persist properties of object " + object + ". Constraint violation.", cve);
		} catch (RepositoryException re) {
			throw new org.apache.portals.graffito.jcr.exception.RepositoryException("Cannot persist properties of object "
					+ object, re);
		}
	}
	
	private void checkMandatoryProperty(Node objectNode, FieldDescriptor fieldDescriptor, Value value) throws RepositoryException {
		PropertyDefinition[] propertyDefinitions = objectNode.getPrimaryNodeType().getDeclaredPropertyDefinitions();
		for (int i = 0; i < propertyDefinitions.length; i++) {
			PropertyDefinition definition = propertyDefinitions[i];
			if (definition.getName().equals(fieldDescriptor.getJcrName()) && definition.isMandatory()
					&& (definition.isAutoCreated() == false) && (value == null)) {
				throw new PersistenceException("Class of type:" + fieldDescriptor.getClassDescriptor().getClassName()
						+ " has property: " + fieldDescriptor.getFieldName() + " declared as JCR property: "
						+ fieldDescriptor.getJcrName() + " This property is mandatory but property in bean has value null");
			}
		}
	}	
	private AtomicTypeConverter getAtomicTypeConverter(FieldDescriptor fd, Object object, String fieldName) {
		Class fieldTypeClass = null;
		// Check if an atomic converter is assigned to the field converter
		String atomicTypeConverterClass = fd.getConverter();
		if (null != atomicTypeConverterClass)
		{
			return (AtomicTypeConverter) ReflectionUtils.newInstance(atomicTypeConverterClass);
		}
		else
		{
			// Get the default atomic converter in function of the classname
			if (null != fd.getFieldTypeClass()) {
				fieldTypeClass = fd.getFieldTypeClass();
			} else if (null != object) {
				fieldTypeClass = ReflectionUtils.getPropertyType(object, fieldName);
			}

			if (null != fieldTypeClass) {
				return this.atomicTypeConverterProvider.getAtomicTypeConverter(fieldTypeClass);
			} else {
				return NULL_CONVERTER;
			}
			
		}
	}

}
