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

import java.util.Iterator;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.portals.graffito.jcr.exception.JcrMappingException;
import org.apache.portals.graffito.jcr.exception.PersistenceException;
import org.apache.portals.graffito.jcr.mapper.Mapper;
import org.apache.portals.graffito.jcr.mapper.model.BeanDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.ClassDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.CollectionDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.FieldDescriptor;
import org.apache.portals.graffito.jcr.persistence.PersistenceConstant;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.AtomicTypeConverter;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.AtomicTypeConverterProvider;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.impl.NullTypeConverterImpl;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.CollectionConverter;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.ManageableCollection;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.ManageableCollectionUtil;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.impl.DefaultCollectionConverterImpl;
import org.apache.portals.graffito.jcr.persistence.objectconverter.BeanConverter;
import org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter;
import org.apache.portals.graffito.jcr.reflection.ReflectionUtils;
import org.apache.portals.graffito.jcr.repository.RepositoryUtil;

/**
 * Default implementation for {@link ObjectConverterImpl}
 * 
 * @author <a href="mailto:christophe.lombart@gmail.com">Lombart  Christophe </a>
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class ObjectConverterImpl implements ObjectConverter {

	private final static Log log = LogFactory.getLog(ObjectConverterImpl.class);

	private static final AtomicTypeConverter NULL_CONVERTER = new NullTypeConverterImpl();

	private Mapper mapper;

	private AtomicTypeConverterProvider atomicTypeConverterProvider;

	private ProxyManager proxyManager;

	/**
	 * No-arg constructor.
	 */
	public ObjectConverterImpl() {
	}

	/**
	 * Constructor
	 * 
	 * @param mapper
	 *            The mapper to used
	 * @param converterProvider
	 *            The atomic type converter provider
	 * 
	 */
	public ObjectConverterImpl(Mapper mapper, AtomicTypeConverterProvider converterProvider) {
		this.mapper = mapper;
		this.atomicTypeConverterProvider = converterProvider;
		this.proxyManager = new ProxyManager();
	}

	/**
	 * Set the <code>Mapper</code> used to solve mappings.
	 * 
	 * @param mapper
	 *            a <code>Mapper</code>
	 */
	public void setMapper(Mapper mapper) {
		this.mapper = mapper;
	}

	/**
	 * Sets the converter provider.
	 * 
	 * @param converterProvider
	 *            an <code>AtomicTypeConverterProvider</code>
	 */
	public void setAtomicTypeConverterProvider(AtomicTypeConverterProvider converterProvider) {
		this.atomicTypeConverterProvider = converterProvider;
	}

	/**
	 * @see org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter#insert(javax.jcr.Session,
	 *      java.lang.Object)
	 */
	public void insert(Session session, Object object) {
		String path = this.getPath(session, object);
		try {
			String parentPath = RepositoryUtil.getParentPath(path);
			String nodeName = RepositoryUtil.getNodeName(path);
			Node parentNode = (Node) session.getItem(parentPath);
			this.insert(session, parentNode, nodeName, object);

		} catch (PathNotFoundException pnfe) {
			throw new PersistenceException("Impossible to insert the object at '" + path + "'", pnfe);
		} catch (RepositoryException re) {
			throw new org.apache.portals.graffito.jcr.exception.RepositoryException("Impossible to insert the object at '" + path
					+ "'", re);
		}
	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter#insert(javax.jcr.Session,
	 *      javax.jcr.Node, java.lang.String, java.lang.Object)
	 */
	public void insert(Session session, Node parentNode, String nodeName, Object object) {
		ClassDescriptor classDescriptor = mapper.getClassDescriptorByClass(object.getClass());

		String jcrNodeType = classDescriptor.getJcrNodeType();
		if ((jcrNodeType == null) || jcrNodeType.equals("")) {
			jcrNodeType = PersistenceConstant.NT_UNSTRUCTURED;
		}

		Node objectNode = null;
		try {
			objectNode = parentNode.addNode(nodeName, jcrNodeType);

		} catch (NoSuchNodeTypeException nsnte) {
			throw new JcrMappingException("Unknown node type " + jcrNodeType + " for mapped class " + object.getClass(), nsnte);
		} catch (RepositoryException re) {
			throw new PersistenceException("Cannot create new node of type " + jcrNodeType + " from mapped class "
					+ object.getClass(), re);
		}

		String[] mixinTypes = classDescriptor.getJcrMixinTypes();
		String mixinTypeName = null;
		try {

			// Add mixin types
			if (null != classDescriptor.getJcrMixinTypes()) {
				for (int i = 0; i < mixinTypes.length; i++) {
					mixinTypeName = mixinTypes[i].trim();
					objectNode.addMixin(mixinTypeName);
				}
			}

			// Add mixin types defined in the associated interfaces 
			if (!classDescriptor.hasDiscriminator() && classDescriptor.hasInterfaces()) {
				Iterator interfacesIterator = classDescriptor.getImplements().iterator();
				while (interfacesIterator.hasNext()) {
					String interfaceName = (String) interfacesIterator.next();
					ClassDescriptor interfaceDescriptor = mapper
							.getClassDescriptorByClass(ReflectionUtils.forName(interfaceName));
					objectNode.addMixin(interfaceDescriptor.getJcrNodeType().trim());
				}
			}

			// If required, add the discriminator node type 
			if (classDescriptor.hasDiscriminator()) {
				mixinTypeName = PersistenceConstant.DISCRIMINATOR_NODE_TYPE;
				objectNode.addMixin(mixinTypeName);
				objectNode.setProperty(PersistenceConstant.DISCRIMINATOR_PROPERTY_NAME, ReflectionUtils.getBeanClass(object)
						.getName());
			}
		} catch (NoSuchNodeTypeException nsnte) {
			throw new JcrMappingException("Unknown mixin type " + mixinTypeName + " for mapped class " + object.getClass(), nsnte);
		} catch (RepositoryException re) {
			throw new PersistenceException("Cannot create new node of type " + jcrNodeType + " from mapped class "
					+ object.getClass(), re);
		}

		storeSimpleFields(session, object, classDescriptor, objectNode);
		insertBeanFields(session, object, classDescriptor, objectNode);
		insertCollectionFields(session, object, classDescriptor, objectNode);
	}

	/**
	 * @see org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter#update(javax.jcr.Session,
	 *      java.lang.Object)
	 */
	public void update(Session session, Object object) {
		String path = this.getPath(session, object);
		try {
			String parentPath = RepositoryUtil.getParentPath(path);
			String nodeName = RepositoryUtil.getNodeName(path);
			Node parentNode = (Node) session.getItem(parentPath);
			this.update(session, parentNode, nodeName, object);
		} catch (PathNotFoundException pnfe) {
			throw new PersistenceException("Impossible to update the object at '" + path + "'", pnfe);
		} catch (RepositoryException re) {
			throw new org.apache.portals.graffito.jcr.exception.RepositoryException("Impossible to update the object at '" + path
					+ "'", re);
		}
	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter#update(javax.jcr.Session,
	 *      javax.jcr.Node, java.lang.String, java.lang.Object)
	 */
	public void update(Session session, Node parentNode, String nodeName, Object object) {
		try {
			ClassDescriptor classDescriptor = mapper.getClassDescriptorByClass(ReflectionUtils.getBeanClass(object));
			Node objectNode = parentNode.getNode(nodeName);

			checkNodeType(session, classDescriptor);

			checkCompatiblePrimaryNodeTypes(session, objectNode, classDescriptor, false);

			storeSimpleFields(session, object, classDescriptor, objectNode);
			updateBeanFields(session, object, classDescriptor, objectNode);
			updateCollectionFields(session, object, classDescriptor, objectNode);
		} catch (PathNotFoundException pnfe) {
			throw new PersistenceException("Impossible to update the object: " + nodeName + " at node : " + parentNode, pnfe);
		} catch (RepositoryException re) {
			throw new org.apache.portals.graffito.jcr.exception.RepositoryException("Impossible to update the object: "
					+ nodeName + " at node : " + parentNode, re);
		}
	}

	/**
	 * @see org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter#getObject(javax.jcr.Session,
	 *      java.lang.Class, java.lang.String)
	 */
	public Object getObject(Session session, String path) {
		try {
			if (!session.itemExists(path)) {
				return null;
			}

			ClassDescriptor classDescriptor = null;
			Node node = (Node) session.getItem(path);
			if (node.hasProperty(PersistenceConstant.DISCRIMINATOR_PROPERTY_NAME)) {
				String className = node.getProperty(PersistenceConstant.DISCRIMINATOR_PROPERTY_NAME).getValue().getString();
				classDescriptor = mapper.getClassDescriptorByClass(ReflectionUtils.forName(className));
			} else {
				String nodeType = node.getPrimaryNodeType().getName();
				if (nodeType.equals(PersistenceConstant.FROZEN_NODE_TYPE)) {
					nodeType = node.getProperty(PersistenceConstant.FROZEN_PRIMARY_TYPE_PROPERTY).getString();
				}
				classDescriptor = mapper.getClassDescriptorByNodeType(nodeType);
			}

			if (null == classDescriptor) {
				throw new JcrMappingException("Impossible to find the classdescriptor for " + path
						+ ". There is no discriminator and associated  JCR node type");
			}

			Object object = ReflectionUtils.newInstance(classDescriptor.getClassName());

			retrieveSimpleFields(session, classDescriptor, node, object);
			retrieveBeanFields(session, classDescriptor, node, path, object, false);
			retrieveCollectionFields(session, classDescriptor, node, object, false);

			return object;
//		} catch (ClassNotFoundException clnf) {
//			throw new PersistenceException("Impossible to instantiate the object at " + path, clnf);
		} catch (PathNotFoundException pnfe) {
			// HINT should never get here
			throw new PersistenceException("Impossible to get the object at " + path, pnfe);
		} catch (RepositoryException re) {
			throw new org.apache.portals.graffito.jcr.exception.RepositoryException("Impossible to get the object at " + path, re);
		}
	}

	/**
	 * @see org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter#getObject(javax.jcr.Session,
	 *      java.lang.Class, java.lang.String)
	 */
	public Object getObject(Session session, Class clazz, String path) {
		try {
			if (!session.itemExists(path)) {
				return null;
			}

			ClassDescriptor classDescriptor = getClassDescriptor(clazz);

			checkNodeType(session, classDescriptor);

			Node node = (Node) session.getItem(path);
			if (!classDescriptor.isInterface()) {
				checkCompatiblePrimaryNodeTypes(session, node, classDescriptor, true);
			}

			Object object = null;
			if (classDescriptor.usesNodeTypePerHierarchyStrategy()) {
				if (!node.hasProperty(PersistenceConstant.DISCRIMINATOR_PROPERTY_NAME)) {
					throw new PersistenceException("Cannot fetch object of type '" + clazz.getName()
							+ "' using NODETYPE_PER_HIERARCHY. Discriminator property is not present.");
				}

				String className = node.getProperty(PersistenceConstant.DISCRIMINATOR_PROPERTY_NAME).getValue().getString();
				classDescriptor = getClassDescriptor(ReflectionUtils.forName(className));
				object = ReflectionUtils.newInstance(className);
			} else {
				if (classDescriptor.usesNodeTypePerConcreteClassStrategy()) {
					String nodeType = node.getPrimaryNodeType().getName();
					if (!nodeType.equals(classDescriptor.getJcrNodeType())) {
						classDescriptor = classDescriptor.getDescendantClassDescriptor(nodeType);
					}
				}
				object = ReflectionUtils.newInstance(classDescriptor.getClassName());

			}

			retrieveSimpleFields(session, classDescriptor, node, object);
			retrieveBeanFields(session, classDescriptor, node, path, object, false);
			retrieveCollectionFields(session, classDescriptor, node, object, false);

			return object;
//		} catch (ClassNotFoundException clnf) {
//			throw new PersistenceException("Impossible to instantiate the object at " + path, clnf);
		} catch (PathNotFoundException pnfe) {
			// HINT should never get here
			throw new PersistenceException("Impossible to get the object at " + path, pnfe);
		} catch (RepositoryException re) {
			throw new org.apache.portals.graffito.jcr.exception.RepositoryException("Impossible to get the object at " + path, re);
		}
	}

	public void retrieveAllMappedAttributes(Session session, Object object) {
		String path = null;
		try {
			ClassDescriptor classDescriptor = getClassDescriptor(object.getClass());
			String pathFieldName = classDescriptor.getPathFieldDescriptor().getFieldName();
			path = (String) ReflectionUtils.getNestedProperty(object, pathFieldName);
			Node node = (Node) session.getItem(path);
			retrieveBeanFields(session, classDescriptor, node, path, object, true);
			retrieveCollectionFields(session, classDescriptor, node, object, true);
			
		} catch (PathNotFoundException pnfe) {

			throw new PersistenceException("Impossible to get the object at " + path, pnfe);
		} catch (RepositoryException re) {
			throw new org.apache.portals.graffito.jcr.exception.RepositoryException("Impossible to get the object at " + path, re);
		}
	}

	public void retrieveMappedAttribute(Session session, Object object, String attributeName) {
		String path = null;
		ClassDescriptor classDescriptor = null;
		try {
			classDescriptor = getClassDescriptor(object.getClass());
			String pathFieldName = classDescriptor.getPathFieldDescriptor().getFieldName();
			path = (String) ReflectionUtils.getNestedProperty(object, pathFieldName);
			Node node = (Node) session.getItem(path);
			BeanDescriptor beanDescriptor = classDescriptor.getBeanDescriptor(attributeName);
			if (beanDescriptor != null)
			{
				this.retrieveBeanField(session, beanDescriptor, node, path, object, true);				
			}
			// Check if the attribute is a collection
			else
			{
				CollectionDescriptor collectionDescriptor = classDescriptor.getCollectionDescriptor(attributeName);
				if (collectionDescriptor != null)
				{
					this.retrieveCollectionField(session, collectionDescriptor, node, object, true);
				}
				else
				{
					throw new PersistenceException("Impossible to retrieve the mapped attribute. The attribute '" + 
							                                                         attributeName + "'  is not a bean or a collection for the class : " + classDescriptor.getClassName());
				}
			}
			
		} catch (PathNotFoundException pnfe) {

			throw new PersistenceException("Impossible to get the object at " + path, pnfe);
		} catch (RepositoryException re) {
			throw new org.apache.portals.graffito.jcr.exception.RepositoryException("Impossible to get the object at " + path, re);
		}
	}

	/**
	 * Validates the node type used by the class descriptor.
	 * 
	 * @param session
	 *            the current session
	 * @param classDescriptor
	 *            descriptor
	 * @throws JcrMappingException
	 *             thrown if the node type is unknown
	 * @throws org.apache.portals.graffito.jcr.exception.RepositoryException
	 *             thrown if an error occured in the underlying repository
	 */
	private void checkNodeType(Session session, ClassDescriptor classDescriptor) {
		String jcrTypeName = null;
		try {

			//Don't check the primary node type for interfaces. They are only associated to mixin node type
			if (classDescriptor.isInterface()) {
				String[] mixinTypes = classDescriptor.getJcrMixinTypes();
				for (int i = 0; i < mixinTypes.length; i++) {
					jcrTypeName = mixinTypes[i];
					session.getWorkspace().getNodeTypeManager().getNodeType(jcrTypeName);
				}
			} else {
				jcrTypeName = classDescriptor.getJcrNodeType();
				if (jcrTypeName != null && !jcrTypeName.equals("")) {
					session.getWorkspace().getNodeTypeManager().getNodeType(jcrTypeName);
				}
			}
		} catch (NoSuchNodeTypeException nsnte) {
			throw new JcrMappingException("Mapping for class '" + classDescriptor.getClassName()
					+ "' use unknown primary or mixin node type '" + jcrTypeName + "'");
		} catch (RepositoryException re) {
			throw new org.apache.portals.graffito.jcr.exception.RepositoryException(re);
		}
	}

	/**
	 * Checks if the node type in the class descriptor is compatible with the
	 * specified node node type.
	 * 
	 * @param session
	 *            the current session
	 * @param node
	 *            node against whose node type the compatibility is checked
	 * @param classDescriptor
	 *            class descriptor
	 * @param checkVersionNode
	 *            <tt>true</tt> if the check should continue in case the
	 *            <tt>node</tt> is a version node, <tt>false</tt> if no
	 *            check against version node should be performed
	 * 
	 * @throws PersistenceException
	 *             thrown if node types are incompatible
	 * @throws org.apache.portals.graffito.jcr.exception.RepositoryException
	 *             thrown if an error occured in the underlying repository
	 */
	private void checkCompatiblePrimaryNodeTypes(Session session, Node node, ClassDescriptor classDescriptor,
			boolean checkVersionNode) {
		try {
			NodeType nodeType = node.getPrimaryNodeType();

			boolean compatible = checkCompatibleNodeTypes(nodeType, classDescriptor);

			if (!compatible && checkVersionNode && PersistenceConstant.FROZEN_NODE_TYPE.equals(nodeType.getName())) {
				NodeTypeManager ntMgr = session.getWorkspace().getNodeTypeManager();
				nodeType = ntMgr.getNodeType(node.getProperty(PersistenceConstant.FROZEN_PRIMARY_TYPE_PROPERTY).getString());

				compatible = checkCompatibleNodeTypes(nodeType, classDescriptor);
			}

			if (!compatible) {
				throw new PersistenceException("Cannot map object of type '" + classDescriptor.getClassName() + "'. Node type '"
						+ node.getPrimaryNodeType().getName() + "' does not match descriptor node type '"
						+ classDescriptor.getJcrNodeType() + "'");
			}
		} catch (RepositoryException re) {
			throw new org.apache.portals.graffito.jcr.exception.RepositoryException(re);
		}
	}

	/**
	 * Node types compatibility check.
	 * 
	 * @param nodeType
	 *            target node type
	 * @param descriptor
	 *            descriptor containing source node type
	 * @return <tt>true</tt> if nodes are considered compatible,
	 *         <tt>false</tt> otherwise
	 */
	private boolean checkCompatibleNodeTypes(NodeType nodeType, ClassDescriptor descriptor) {

		//return true if node type is not used
		if (descriptor.getJcrNodeType() == null || descriptor.getJcrNodeType().equals("")) {
			return true;
		}

		if (nodeType.getName().equals(descriptor.getJcrNodeType())) {
			return true;
		}

		NodeType[] superTypes = nodeType.getSupertypes();
		for (int i = 0; i < superTypes.length; i++) {
			if (superTypes[i].getName().equals(descriptor.getJcrNodeType())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @see org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter#getPath(javax.jcr.Session,
	 *      java.lang.Object)
	 * @throws JcrMappingException
	 */
	public String getPath(Session session, Object object) {
		ClassDescriptor classDescriptor = mapper.getClassDescriptorByClass(object.getClass());

		final FieldDescriptor pathFieldDescriptor = classDescriptor.getPathFieldDescriptor();
		if (pathFieldDescriptor == null) {
			throw new JcrMappingException(
					"Class of type: "
							+ object.getClass().getName()
							+ " has no path mapping. Maybe attribute path=\"true\" for a field element of this class in jcrmapping.xml is missing?");
		}
		String pathField = pathFieldDescriptor.getFieldName();

		return (String) ReflectionUtils.getNestedProperty(object, pathField);
	}

	/**
	 * Retrieve simple fields (atomic fields)
	 * 
	 * @throws JcrMappingException
	 * @throws org.apache.portals.graffito.jcr.exception.RepositoryException
	 */
	private Object retrieveSimpleFields(Session session, ClassDescriptor classDescriptor, Node node, Object object) 
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
				
//				} else if (classDescriptor.usesNodeTypePerHierarchyStrategy() && classDescriptor.hasDiscriminator()) {
//
//					if (node.hasProperty(PersistenceConstant.DISCRIMINATOR_PROPERTY_NAME)) {
//						if (null == initializedBean) {
//							initializedBean = ReflectionUtils.newInstance(classDescriptor.getClassName());
//						}
//						Value propValue = node.getProperty(propertyName).getValue();
//						AtomicTypeConverter converter = getAtomicTypeConverter(fieldDescriptor, initializedBean, fieldName);
//						Object fieldValue = converter.getObject(propValue);
//						ReflectionUtils.setNestedProperty(initializedBean, fieldName, fieldValue);
//						
//					} else {
//						throw new PersistenceException("Class '" + classDescriptor.getClassName()
//								+ "' have not a discriminator property.");
//					}
//				} else {
//					if (node.hasProperty(propertyName)) {
//						Value propValue = node.getProperty(propertyName).getValue();
//						// HINT: lazy initialize target bean - The bean can be
//						// null when it is inline
//						if (null != propValue && null == initializedBean) {
//							initializedBean = ReflectionUtils.newInstance(classDescriptor.getClassName());
//						}
//
//						AtomicTypeConverter converter = getAtomicTypeConverter(fieldDescriptor, initializedBean, fieldName);
//						Object fieldValue = converter.getObject(propValue);
//						ReflectionUtils.setNestedProperty(initializedBean, fieldName, fieldValue);
//					} else {
//						log.warn("Class '" + classDescriptor.getClassName() + "' has an unmapped property : " + propertyName);
//					}
//				}
			}
		} catch (ValueFormatException vfe) {
			throw new PersistenceException("Cannot retrieve properties of object " + object + " from node " + node, vfe);
		} catch (RepositoryException re) {
			throw new org.apache.portals.graffito.jcr.exception.RepositoryException("Cannot retrieve properties of object "
					+ object + " from node " + node, re);
		}

		return initializedBean;
	}

	/**
	 * Retrieve bean fields
	 */
	private void retrieveBeanFields(Session session, ClassDescriptor classDescriptor, Node node, String path, Object object,
			boolean forceToRetrieve) {
		Iterator beanDescriptorIterator = classDescriptor.getBeanDescriptors().iterator();
		while (beanDescriptorIterator.hasNext()) {
			BeanDescriptor beanDescriptor = (BeanDescriptor) beanDescriptorIterator.next();
			this.retrieveBeanField(session, beanDescriptor, node, path, object, forceToRetrieve);
		}
	}

	
	private void retrieveBeanField(Session session,BeanDescriptor beanDescriptor, Node node, String path, Object object, boolean forceToRetrieve )
	{
		if (!beanDescriptor.isAutoRetrieve() && !forceToRetrieve) {
			return;
		}

		String beanName = beanDescriptor.getFieldName();
		Class beanClass = ReflectionUtils.getPropertyType(object, beanName);
		Object bean = null;
		if (beanDescriptor.isProxy()) {
			bean = proxyManager.createBeanProxy(session, this, beanClass, path + "/" + beanDescriptor.getJcrName());

		} else {
			if (beanDescriptor.isInline()) {
				bean = this.retrieveSimpleFields(session, mapper.getClassDescriptorByClass(beanClass), node, bean);
			} else if (null != beanDescriptor.getConverter() && !"".equals(beanDescriptor.getConverter())) {
				String converterClassName = beanDescriptor.getConverter();
				Object[] param = {this};
				BeanConverter beanConverter = (BeanConverter) ReflectionUtils.invokeConstructor(converterClassName, param);
				bean = beanConverter.getObject(session, node, beanDescriptor, beanClass);
			} else {
				bean = this.getObject(session, path + "/" + beanDescriptor.getJcrName());
			}
		}
		ReflectionUtils.setNestedProperty(object, beanName, bean);		
	}
	
	
	
	/**
	 * Retrieve Collection fields
	 */
	private void retrieveCollectionFields(Session session, ClassDescriptor classDescriptor, Node parentNode, Object object,
			boolean forceToRetrieve) {
		Iterator collectionDescriptorIterator = classDescriptor.getCollectionDescriptors().iterator();
		while (collectionDescriptorIterator.hasNext()) {
			CollectionDescriptor collectionDescriptor = (CollectionDescriptor) collectionDescriptorIterator.next();
			this.retrieveCollectionField(session, collectionDescriptor, parentNode, object, forceToRetrieve);			
		}
	}

	private void retrieveCollectionField(Session session, CollectionDescriptor collectionDescriptor, Node parentNode, Object object, boolean forceToRetrieve)
	{
		if (!collectionDescriptor.isAutoRetrieve() && !forceToRetrieve) {
			return;
		}

		CollectionConverter collectionConverter = this.getCollectionConverter(session, collectionDescriptor);
		Class collectionFieldClass = ReflectionUtils.getPropertyType(object, collectionDescriptor.getFieldName());
		ManageableCollection collection = null;
		if (collectionDescriptor.isProxy()) {
			collection = (ManageableCollection) proxyManager.createCollectionProxy(session, collectionConverter, parentNode,
					collectionDescriptor, collectionFieldClass);

		} else {
			collection = collectionConverter.getCollection(session, parentNode, collectionDescriptor, collectionFieldClass);
		}

		ReflectionUtils.setNestedProperty(object, collectionDescriptor.getFieldName(), collection);		
	}
	
	/**
	 * Insert Bean fields
	 */
	private void insertBeanFields(Session session, Object object, ClassDescriptor classDescriptor, Node objectNode) {
		Iterator beanDescriptorIterator = classDescriptor.getBeanDescriptors().iterator();
		while (beanDescriptorIterator.hasNext()) {
			BeanDescriptor beanDescriptor = (BeanDescriptor) beanDescriptorIterator.next();

			if (!beanDescriptor.isAutoInsert()) {
				continue;
			}

			String jcrName = beanDescriptor.getJcrName();
			Object bean = ReflectionUtils.getNestedProperty(object, beanDescriptor.getFieldName());
			if (bean != null) {
				if (beanDescriptor.isInline()) {
					this.storeSimpleFields(session, bean, mapper.getClassDescriptorByClass(bean.getClass()), objectNode);
				} else if (null != beanDescriptor.getConverter() && !"".equals(beanDescriptor.getConverter())) {
					String converterClassName = beanDescriptor.getConverter();
					Object[] param = {this};
					BeanConverter beanConverter = (BeanConverter) ReflectionUtils.invokeConstructor(converterClassName, param);
					beanConverter.insert(session, objectNode, beanDescriptor, object);
				} else {
					this.insert(session, objectNode, jcrName, bean);
				}
			}
		}
	}

	/**
	 * Update Bean fields
	 */
	private void updateBeanFields(Session session, Object object, ClassDescriptor classDescriptor, Node objectNode) {
		String jcrName = null;
		try {
			Iterator beanDescriptorIterator = classDescriptor.getBeanDescriptors().iterator();
			while (beanDescriptorIterator.hasNext()) {
				BeanDescriptor beanDescriptor = (BeanDescriptor) beanDescriptorIterator.next();
				if (!beanDescriptor.isAutoUpdate()) {
					continue;
				}

				jcrName = beanDescriptor.getJcrName();
				Object bean = ReflectionUtils.getNestedProperty(object, beanDescriptor.getFieldName());

				// if the bean is null, remove existing node
				if ((bean == null)) {
					if (beanDescriptor.isInline()) {
						Class beanClass = ReflectionUtils.getPropertyType(object, beanDescriptor.getFieldName());
						this.storeSimpleFields(session, bean, mapper.getClassDescriptorByClass(beanClass), objectNode);
					} else if (null != beanDescriptor.getConverter() && !"".equals(beanDescriptor.getConverter())) {
						String converterClassName = beanDescriptor.getConverter();
						Object[] param = {this};
						BeanConverter beanConverter = (BeanConverter) ReflectionUtils
								.invokeConstructor(converterClassName, param);
						beanConverter.remove(session, objectNode, beanDescriptor);
					} else {
						if (objectNode.hasNode(jcrName)) {
							objectNode.getNode(jcrName).remove();
						}
					}
				} else {
					if (beanDescriptor.isInline()) {
						this.storeSimpleFields(session, bean, mapper.getClassDescriptorByClass(bean.getClass()), objectNode);
					} else if (null != beanDescriptor.getConverter() && !"".equals(beanDescriptor.getConverter())) {
						String converterClassName = beanDescriptor.getConverter();
						Object[] param = {this};
						BeanConverter beanConverter = (BeanConverter) ReflectionUtils
								.invokeConstructor(converterClassName, param);
						beanConverter.update(session, objectNode, beanDescriptor, bean);
					} else {
						this.update(session, objectNode, jcrName, bean);
					}
				}
			}
		} catch (VersionException ve) {
			throw new PersistenceException("Cannot remove bean at path " + jcrName, ve);
		} catch (LockException le) {
			throw new PersistenceException("Cannot remove bean at path " + jcrName + ". Item is locked.", le);
		} catch (ConstraintViolationException cve) {
			throw new PersistenceException("Cannot remove bean at path " + jcrName + ". Contraint violation.", cve);
		} catch (RepositoryException re) {
			throw new org.apache.portals.graffito.jcr.exception.RepositoryException("Cannot remove bean at path " + jcrName, re);
		}
	}

	private void insertCollectionFields(Session session, Object object, ClassDescriptor classDescriptor, Node objectNode) {
		Iterator collectionDescriptorIterator = classDescriptor.getCollectionDescriptors().iterator();

		while (collectionDescriptorIterator.hasNext()) {
			CollectionDescriptor collectionDescriptor = (CollectionDescriptor) collectionDescriptorIterator.next();

			if (!collectionDescriptor.isAutoInsert()) {
				continue;
			}

			CollectionConverter collectionConverter = this.getCollectionConverter(session, collectionDescriptor);
			Object collection = ReflectionUtils.getNestedProperty(object, collectionDescriptor.getFieldName());
			ManageableCollection manageableCollection = ManageableCollectionUtil.getManageableCollection(collection);

			collectionConverter.insertCollection(session, objectNode, collectionDescriptor, manageableCollection);
		}
	}

	private void updateCollectionFields(Session session, Object object, ClassDescriptor classDescriptor, Node objectNode) {
		Iterator collectionDescriptorIterator = classDescriptor.getCollectionDescriptors().iterator();

		while (collectionDescriptorIterator.hasNext()) {
			CollectionDescriptor collectionDescriptor = (CollectionDescriptor) collectionDescriptorIterator.next();
			if (!collectionDescriptor.isAutoUpdate()) {
				continue;
			}

			CollectionConverter collectionConverter = this.getCollectionConverter(session, collectionDescriptor);
			Object collection = ReflectionUtils.getNestedProperty(object, collectionDescriptor.getFieldName());
			ManageableCollection manageableCollection = ManageableCollectionUtil.getManageableCollection(collection);

			collectionConverter.updateCollection(session, objectNode, collectionDescriptor, manageableCollection);
		}
	}

	private void storeSimpleFields(Session session, Object object, ClassDescriptor classDescriptor, Node objectNode) {
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

	private CollectionConverter getCollectionConverter(Session session, CollectionDescriptor collectionDescriptor) {
		String className = collectionDescriptor.getCollectionConverter();
		Map atomicTypeConverters = this.atomicTypeConverterProvider.getAtomicTypeConverters();
		if (className == null) {
			return new DefaultCollectionConverterImpl(atomicTypeConverters, this, this.mapper);
		} else {
			return (CollectionConverter) ReflectionUtils.invokeConstructor(className, new Object[]{atomicTypeConverters, this,
					this.mapper});
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

	private ClassDescriptor getClassDescriptor(Class beanClass) {
		ClassDescriptor classDescriptor = mapper.getClassDescriptorByClass(beanClass);
		if (null == classDescriptor) {
			throw new JcrMappingException("Class of type: " + beanClass.getName()
					+ " is not JCR persistable. Maybe element 'class-descriptor' for this type in mapping file is missing");
		}

		return classDescriptor;
	}

}
