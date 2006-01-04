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
package org.apache.portals.graffito.jcr.persistence.objectconverter.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.portals.graffito.jcr.exception.JcrMappingException;
import org.apache.portals.graffito.jcr.exception.PersistenceException;
import org.apache.portals.graffito.jcr.mapper.Mapper;
import org.apache.portals.graffito.jcr.mapper.model.BeanDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.ClassDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.CollectionDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.FieldDescriptor;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.AtomicTypeConverter;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.CollectionConverter;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.ManageableCollection;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.ManageableCollectionUtil;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.impl.DefaultCollectionConverterImpl;
import org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter;
import org.apache.portals.graffito.jcr.repository.RepositoryUtil;

/**
 * Default implementation for {@link ObjectConverterImpl}
 * 
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Lombart Christophe </a>
 * 
 */
public class ObjectConverterImpl implements ObjectConverter
{
	private Mapper mapper;

	private Map atomicTypeConverters;

	/**
	 * Constructor
	 * 
	 * @param mapper The mapper to used
	 * @param atomicTypeConverters The atomic type converters to used
	 * 
	 */
	public ObjectConverterImpl(Mapper mapper, Map atomicTypeConverters)
	{
		this.mapper = mapper;
		this.atomicTypeConverters = atomicTypeConverters;
	}

	/**
	 * @see org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter#insert(javax.jcr.Session, java.lang.Object)
	 */
	public void insert(Session session, Object object)
	{
		String path = this.getPath(session, object);
		try
		{
			String parentPath = RepositoryUtil.getParentPath(path);
			String nodeName = RepositoryUtil.getNodeName(path);
			Node parentNode = (Node) session.getItem(parentPath);
			this.insert(session, parentNode, nodeName, object);

		}
		catch (Exception e)
		{
			throw new PersistenceException("Impossible to insert the object at " + path, e);
		}

	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter#insert(javax.jcr.Session, javax.jcr.Node, java.lang.String, java.lang.Object)
	 */
	public void insert(Session session, Node parentNode, String nodeName, Object object)
	{
		try
		{
			ClassDescriptor classDescriptor = mapper.getClassDescriptor(object.getClass());
			if (classDescriptor == null)
			{
				throw new PersistenceException("Class of type: " + object.getClass().getName() + " is not JCR persistable. Maybe element 'class-descriptor' for this type in mapping file is missing");
			}

			String jcrNodeType = classDescriptor.getJcrNodeType();
			if (jcrNodeType == null || jcrNodeType.equals(""))
			{
				throw new PersistenceException("Undefined node type for  " + parentNode);
			}

			Node objectNode = null;
			objectNode = parentNode.addNode(nodeName, jcrNodeType);

			storeSimpleFields(object, classDescriptor, objectNode);
			insertBeanFields(session, object, classDescriptor, objectNode);
			insertCollectionFields(session, object, classDescriptor, objectNode);

		}
		catch (Exception e)
		{
			throw new PersistenceException("Impossible to insert the object at " + parentNode, e);
		}

	}

	/**
	 * @see org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter#update(javax.jcr.Session, java.lang.Object)
	 */
	public void update(Session session, Object object) 
	{
		String path = this.getPath(session, object);
		try
		{
			String parentPath = RepositoryUtil.getParentPath(path);
			String nodeName = RepositoryUtil.getNodeName(path);
			Node parentNode = (Node) session.getItem(parentPath);
			this.update(session, parentNode, nodeName, object);

		}
		catch (Exception e)
		{
			throw new PersistenceException("Impossible to update the object at " + path, e);
		}
	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter#update(javax.jcr.Session, javax.jcr.Node, java.lang.String, java.lang.Object)
	 */
	public void update(Session session, Node parentNode, String nodeName, Object object) 
	{
		try
		{
			ClassDescriptor classDescriptor = mapper.getClassDescriptor(object.getClass());
			if (classDescriptor == null)
			{
				throw new PersistenceException("Class of type: " + object.getClass().getName() + " is not JCR persistable. Maybe element 'class-descriptor' for this type in mapping file is missing");
			}

			String jcrNodeType = classDescriptor.getJcrNodeType();
			if (jcrNodeType == null || jcrNodeType.equals(""))
			{
				throw new PersistenceException("Undefined node type for  " + parentNode);
			}

			Node objectNode = null;
			objectNode = parentNode.getNode(nodeName);

			storeSimpleFields(object, classDescriptor, objectNode);
			updateBeanFields(session, object, classDescriptor, objectNode);
			updateCollectionFields(session, object, classDescriptor, objectNode);

		}
		catch (Exception e)
		{
			throw new PersistenceException("Impossible to update the node : " + parentNode, e);
		}

	}

	/**
	 * @see org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter#getObject(javax.jcr.Session, java.lang.Class, java.lang.String)
	 */
	public Object getObject(Session session, Class clazz, String path)
	{
		try
    	{

			if (!session.itemExists(path))
			{
				return null;
			}

			ClassDescriptor classDescriptor = mapper.getClassDescriptor(clazz);
			if (classDescriptor == null)
			{
				throw new PersistenceException("Class of type: " + clazz.getName() + " is not JCR persistable. Maybe element 'class-descriptor' for this type in mapping file is missing");
			}

			Node node = (Node) session.getItem(path);
			Object object = clazz.newInstance();

			retrieveSimpleFields(classDescriptor, node, object);
			retrieveBeanFields(session, path, classDescriptor, object);
			retrieveCollectionFields(session, classDescriptor, node, object);

			return object;

		}
		catch (Exception e)
		{
			throw new PersistenceException("Impossible to get the object at " + path, e);
		}
	}


	public String getPath(Session session, Object object)
	{
		try
		{
			ClassDescriptor classDescriptor = mapper.getClassDescriptor(object.getClass());
			if (classDescriptor == null)
			{
				throw new PersistenceException("Class of type: " + object.getClass().getName() + " is not JCR persistable. Maybe element 'class-descriptor' for this type in mapping file is missing");
			}
			
			final FieldDescriptor pathFieldDescriptor = classDescriptor.getPathFieldDescriptor();
            if (pathFieldDescriptor == null)
            {
                throw new PersistenceException("Class of type: " + object.getClass().getName() + " has no path mapping. Maybe attribute path=\"true\" for a field element of this class in jcrmapping.xml is missing?");
            }
            String pathField = pathFieldDescriptor.getFieldName();
			return (String) PropertyUtils.getNestedProperty(object, pathField);


		}
		catch (Exception e)
		{
			throw new PersistenceException("Impossible to get the path", e);
		}

	}	
	
	/**
	 * Retrieve simple fields (atomic fields)
	 */
	private void retrieveSimpleFields(ClassDescriptor classDescriptor, Node node, Object object) throws PathNotFoundException, RepositoryException, ValueFormatException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException
	{
		Iterator fieldDescriptorIterator = classDescriptor.getFieldDescriptors().iterator();

		while (fieldDescriptorIterator.hasNext())
		{
			FieldDescriptor fieldDescriptor = (FieldDescriptor) fieldDescriptorIterator.next();
			
			String fieldName = fieldDescriptor.getFieldName();
			String propertyName = fieldDescriptor.getJcrName();
			
			if (fieldDescriptor.isPath())
			{
				PropertyUtils.setNestedProperty(object, fieldName, node.getPath());
			} 
			else 
			{
                Class fieldTypeClass = fieldDescriptor.getFieldTypeClass() != null
                    ? fieldDescriptor.getFieldTypeClass() 
                    : PropertyUtils.getPropertyType(object, fieldName);
                    
				AtomicTypeConverter converter = (AtomicTypeConverter) atomicTypeConverters
						.get(fieldTypeClass);
				if (node.hasProperty(propertyName)) 
				{
					Object fieldValue = converter.getObject(node.getProperty(propertyName).getValue());
					PropertyUtils.setNestedProperty(object, fieldName, fieldValue);
				}
			}
		}
	}

	/**
	 * Retrieve bean fields
	 */
	private void retrieveBeanFields(Session session, String path, ClassDescriptor classDescriptor, Object object) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{
		Iterator beanDescriptorIterator = classDescriptor.getBeanDescriptors().iterator();
		while (beanDescriptorIterator.hasNext())
		{
			BeanDescriptor beanDescriptor = (BeanDescriptor) beanDescriptorIterator.next();
			String beanName = beanDescriptor.getFieldName();
			Class beanClass = PropertyUtils.getPropertyDescriptor(object, beanName).getPropertyType();
			Object bean = this.getObject(session, beanClass, path + "/" + beanDescriptor.getJcrName());
			PropertyUtils.setNestedProperty(object, beanName, bean);
		}
	}

	/**
	 * Retrieve Collection fields
	 */
	private void retrieveCollectionFields(Session session, ClassDescriptor classDescriptor, Node node, Object object) throws PathNotFoundException, RepositoryException, JcrMappingException,
			ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException
	{
		Iterator collectionDescriptorIterator = classDescriptor.getCollectionDescriptors().iterator();
		while (collectionDescriptorIterator.hasNext())
		{
			CollectionDescriptor collectionDescriptor = (CollectionDescriptor) collectionDescriptorIterator.next();
			CollectionConverter collectionConverter = this.getCollectionConverter(collectionDescriptor);
			Class collectionFieldClass = PropertyUtils.getPropertyDescriptor(object, collectionDescriptor.getFieldName()).getPropertyType();
			ManageableCollection collection = collectionConverter.getCollection(session, node, collectionDescriptor, collectionFieldClass);
			PropertyUtils.setNestedProperty(object, collectionDescriptor.getFieldName(), collection);
		}
	}

	/**
	 * Insert Bean fields
	 */
	private void insertBeanFields(Session session, Object object, ClassDescriptor classDescriptor, Node objectNode) 
	{
		try
		{
			Iterator beanDescriptorIterator = classDescriptor.getBeanDescriptors().iterator();
			while (beanDescriptorIterator.hasNext())
			{
				BeanDescriptor beanDescriptor = (BeanDescriptor) beanDescriptorIterator.next();
				String jcrName = beanDescriptor.getJcrName();
				Object bean = PropertyUtils.getNestedProperty(object, beanDescriptor.getFieldName());
				if (bean != null)
				{
					this.insert(session, objectNode, jcrName, bean);
				}

			}
		}
		catch (Exception e)
		{
			throw new PersistenceException("Impossible to insert the bean fields", e);
		}

	}

	/**
	 * Update Bean fields
	 */
	private void updateBeanFields(Session session, Object object, ClassDescriptor classDescriptor, Node objectNode) 
	{
		try
		{
			Iterator beanDescriptorIterator = classDescriptor.getBeanDescriptors().iterator();
			while (beanDescriptorIterator.hasNext())
			{
				BeanDescriptor beanDescriptor = (BeanDescriptor) beanDescriptorIterator.next();
				String jcrName = beanDescriptor.getJcrName();
				Object bean = PropertyUtils.getNestedProperty(object, beanDescriptor.getFieldName());

				// if the bean is null, remove existing node
				if ((bean == null))
				{
					if (objectNode.hasNode(jcrName))
					{
						objectNode.getNode(jcrName).remove();
					}
				}
				else
				{
					this.update(session, objectNode, jcrName, bean);
				}

			}
		}
		catch (Exception e)
		{
			throw new PersistenceException("Impossible to update the bean fields", e);
		}

	}

	private void insertCollectionFields(Session session, Object object, ClassDescriptor classDescriptor, Node objectNode)
	{
		try
		{
			Iterator collectionDescriptorIterator = classDescriptor.getCollectionDescriptors().iterator();
			while (collectionDescriptorIterator.hasNext())
			{
				CollectionDescriptor collectionDescriptor = (CollectionDescriptor) collectionDescriptorIterator.next();
				CollectionConverter collectionConverter = this.getCollectionConverter(collectionDescriptor);
				Object collection = PropertyUtils.getNestedProperty(object, collectionDescriptor.getFieldName());
				ManageableCollection manageableCollection = ManageableCollectionUtil.getManageableCollection(collection);
				collectionConverter.insertCollection(session, objectNode, collectionDescriptor, manageableCollection);
			}
		}
		catch (Exception e)
		{
			throw new PersistenceException("Impossible to insert the collection fields", e);
		}

	}

	private void updateCollectionFields(Session session, Object object, ClassDescriptor classDescriptor, Node objectNode)
	{
		try
		{
			Iterator collectionDescriptorIterator = classDescriptor.getCollectionDescriptors().iterator();
			while (collectionDescriptorIterator.hasNext())
			{
				CollectionDescriptor collectionDescriptor = (CollectionDescriptor) collectionDescriptorIterator.next();
				CollectionConverter collectionConverter = this.getCollectionConverter(collectionDescriptor);
				Object collection = PropertyUtils.getNestedProperty(object, collectionDescriptor.getFieldName());
				ManageableCollection manageableCollection = ManageableCollectionUtil.getManageableCollection(collection);
				collectionConverter.updateCollection(session, objectNode, collectionDescriptor, manageableCollection);
			}
		}
		catch (Exception e)
		{
			throw new PersistenceException("Impossible to store the bean fields", e);
		}

	}

	private void storeSimpleFields(Object object, ClassDescriptor classDescriptor, Node objectNode) 
	               throws PathNotFoundException, ValueFormatException, VersionException, LockException, ConstraintViolationException, 
	                      RepositoryException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{


			Iterator fieldDescriptorIterator = classDescriptor.getFieldDescriptors().iterator();
			while (fieldDescriptorIterator.hasNext())
			{				 
				
				FieldDescriptor fieldDescriptor = (FieldDescriptor) fieldDescriptorIterator.next();
				
				//Of course, Path field is not updated as property				
				if (fieldDescriptor.isPath())
				{
				   continue;	
				}
				
				String fieldName = fieldDescriptor.getFieldName();
				String jcrName = fieldDescriptor.getJcrName();

				// Check the node properties
				boolean autoCreated = false;

				if (objectNode.hasProperty(jcrName))
				{
					autoCreated = objectNode.getProperty(jcrName).getDefinition().isAutoCreated();
				}

				// All auto created JCR properties are ignored
				if (!autoCreated)
				{
					
					Object fieldValue = PropertyUtils.getNestedProperty(object, fieldName);
					Class fieldTypeClass = fieldDescriptor.getFieldTypeClass() != null
                        ? fieldDescriptor.getFieldTypeClass()
                        : PropertyUtils.getPropertyType(object, fieldName);
					AtomicTypeConverter converter = (AtomicTypeConverter) atomicTypeConverters.get(fieldTypeClass);
					Value value = converter.getValue(fieldValue);
					// Check if mandatory property are not null
					this.checkMandatoryProperty(objectNode, fieldDescriptor, value);

					objectNode.setProperty(jcrName, value);
				}

			}

	}

	private CollectionConverter getCollectionConverter(CollectionDescriptor collectionDescriptor) 
	                            throws ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException
	{

		String className = collectionDescriptor.getCollectionConverterClassName();
		if (className == null)
		{
			return new DefaultCollectionConverterImpl(this.atomicTypeConverters, this, this.mapper);
		}
		else
		{
			Class converterClass = Class.forName(className);
			Object[] param =
			{ this.atomicTypeConverters, this, this.mapper };
			return (CollectionConverter) ConstructorUtils.invokeConstructor(converterClass, param);
		}

	}

	private void checkMandatoryProperty(Node objectNode, FieldDescriptor fieldDescriptor, Value value) throws RepositoryException
	{
		PropertyDefinition[] propertyDefinitions = objectNode.getPrimaryNodeType().getDeclaredPropertyDefinitions();
		for (int i = 0; i < propertyDefinitions.length; i++)
		{
			PropertyDefinition definition = propertyDefinitions[i];
			if (definition.getName().equals(fieldDescriptor.getJcrName()) && definition.isMandatory() && definition.isAutoCreated() == false && value == null)
			{
				throw new PersistenceException("Class of type:" + fieldDescriptor.getClassDescriptor().getClassName() + " has property: " + fieldDescriptor.getFieldName()
						+ " declared as JCR property: " + fieldDescriptor.getJcrName() + " This property is mandatory but property in bean has value null");
			}
		}
	}
}
