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
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;

import org.apache.portals.graffito.jcr.exception.JcrMappingException;
import org.apache.portals.graffito.jcr.exception.PersistenceException;
import org.apache.portals.graffito.jcr.mapper.Mapper;
import org.apache.portals.graffito.jcr.mapper.model.BeanDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.ClassDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.CollectionDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.FieldDescriptor;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.AtomicTypeConverter;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.AtomicTypeConverterProvider;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.impl.NullTypeConverterImpl;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.CollectionConverter;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.ManageableCollection;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.ManageableCollectionUtil;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.impl.DefaultCollectionConverterImpl;
import org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter;
import org.apache.portals.graffito.jcr.reflection.ReflectionUtils;
import org.apache.portals.graffito.jcr.repository.RepositoryUtil;

/**
 * Default implementation for {@link ObjectConverterImpl}
 *
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Lombart Christophe </a>
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class ObjectConverterImpl implements ObjectConverter {
    private static final AtomicTypeConverter NULL_CONVERTER = new NullTypeConverterImpl();

    private Mapper mapper;

    private AtomicTypeConverterProvider atomicTypeConverterProvider;

    /**
     * No-arg constructor.
     */
    public ObjectConverterImpl() {
    }

    /**
     * Constructor
     *
     * @param mapper The mapper to used
     * @param converterProvider The atomic type converter provider
     *
     */
    public ObjectConverterImpl(Mapper mapper, AtomicTypeConverterProvider converterProvider) {
        this.mapper = mapper;
        this.atomicTypeConverterProvider = converterProvider;
    }

    /**
     * Set the <code>Mapper</code> used to solve mappings.
     * @param mapper a <code>Mapper</code>
     */
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Sets the converter provider.
     *
     * @param converterProvider an <code>AtomicTypeConverterProvider</code>
     */
    public void setAtomicTypeConverterProvider(AtomicTypeConverterProvider converterProvider) {
        this.atomicTypeConverterProvider = converterProvider;
    }

    /**
     * @see org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter#insert(javax.jcr.Session, java.lang.Object)
     */
    public void insert(Session session, Object object) {
        String path = this.getPath(session, object);
        try {
            String parentPath = RepositoryUtil.getParentPath(path);
            String nodeName = RepositoryUtil.getNodeName(path);
            Node parentNode = (Node) session.getItem(parentPath);
            this.insert(session, parentNode, nodeName, object);

        } catch (PathNotFoundException pnfe) {
            throw new PersistenceException("Impossible to insert the object at '" + path + "'",
                                           pnfe);
        } catch (RepositoryException re) {
            throw new org.apache.portals.graffito.jcr.exception.RepositoryException(
                    "Impossible to insert the object at '" + path + "'",
                    re);
        }
    }

    /**
     *
     * @see org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter#insert(javax.jcr.Session, javax.jcr.Node, java.lang.String, java.lang.Object)
     */
    public void insert(Session session, Node parentNode, String nodeName, Object object) {
        ClassDescriptor classDescriptor = getClassDescriptor(object.getClass());
        
        String jcrNodeType = classDescriptor.getJcrNodeType();
        if ((jcrNodeType == null) || jcrNodeType.equals("")) {
            throw new JcrMappingException("Undefined node type for  " + parentNode);
        }

        Node objectNode = null;
        try {
            objectNode = parentNode.addNode(nodeName, jcrNodeType);
        } 
        catch (NoSuchNodeTypeException nsnte) {
            throw new JcrMappingException("Unknown node type " + jcrNodeType
                                          + " for mapped class " + object.getClass());
        } 
        catch (RepositoryException re) {
            throw new PersistenceException("Cannot create new node of type " + jcrNodeType
                                           + " from mapped class " + object.getClass());
        }

        if (null != classDescriptor.getJcrMixinTypes()) {
            String[] mixinTypes = classDescriptor.getJcrMixinTypes();
            for (int i = 0; i < mixinTypes.length; i++) {
                try {
                    objectNode.addMixin(mixinTypes[i].trim());
                } catch (NoSuchNodeTypeException nsnte) {
                    throw new JcrMappingException("Unknown mixin type " + mixinTypes[i].trim()
                                                  + " for mapped class " + object.getClass());
                } catch (RepositoryException re) {
                    throw new PersistenceException("Cannot create new node of type " + jcrNodeType
                                                   + " from mapped class " + object.getClass());
                }
            }
        }

        storeSimpleFields(session, object, classDescriptor, objectNode);
        insertBeanFields(session, object, classDescriptor, objectNode);
        insertCollectionFields(session, object, classDescriptor, objectNode);
    }

    /**
     * @see org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter#update(javax.jcr.Session, java.lang.Object)
     */
    public void update(Session session, Object object) {
        String path = this.getPath(session, object);
        try {
            String parentPath = RepositoryUtil.getParentPath(path);
            String nodeName = RepositoryUtil.getNodeName(path);
            Node parentNode = (Node) session.getItem(parentPath);
            this.update(session, parentNode, nodeName, object);
        }
        catch(PathNotFoundException pnfe) {
            throw new PersistenceException("Impossible to update the object at '"
                    + path + "'",
                    pnfe);
        }
        catch(RepositoryException re) {
            throw new org.apache.portals.graffito.jcr.exception.RepositoryException(
                    "Impossible to update the object at '" + path + "'",
                    re);
        }
    }

    /**
     *
     * @see org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter#update(javax.jcr.Session, javax.jcr.Node, java.lang.String, java.lang.Object)
     */
    public void update(Session session, Node parentNode, String nodeName, Object object) {
        try {
            ClassDescriptor classDescriptor = getClassDescriptor(object.getClass());
            Node objectNode = parentNode.getNode(nodeName);
            
            storeSimpleFields(session, object, classDescriptor, objectNode);
            updateBeanFields(session, object, classDescriptor, objectNode);
            updateCollectionFields(session, object, classDescriptor, objectNode);
        }
        catch(PathNotFoundException pnfe) {
            throw new PersistenceException("Impossible to update the object: "
                    + nodeName
                    + " at node : " + parentNode, pnfe);
        }
        catch(RepositoryException re) {
            throw new org.apache.portals.graffito.jcr.exception.RepositoryException(
                    "Impossible to update the object: " + nodeName
                    + " at node : " + parentNode, re);
        }
    }

    /**
     * @see org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter#getObject(javax.jcr.Session, java.lang.Class, java.lang.String)
     */
    public Object getObject(Session session, Class clazz, String path) {
        try {
            if (!session.itemExists(path)) {
                return null;
            }

            ClassDescriptor classDescriptor = getClassDescriptor(clazz);
            Node node = (Node) session.getItem(path);
            Object object = ReflectionUtils.newInstance(clazz);

            retrieveSimpleFields(session, classDescriptor, node, object);
            retrieveBeanFields(session, classDescriptor, node, path, object);
            retrieveCollectionFields(session, classDescriptor, node, object);

            return object;
        }         
        catch(PathNotFoundException pnfe) {
            // HINT should never get here
            throw new PersistenceException("Impossible to get the object at " + path, pnfe);
        }
        catch(RepositoryException re) {
            throw new org.apache.portals.graffito.jcr.exception.RepositoryException(
                    "Impossible to get the object at " + path, re);
        }
    }

    /**
     * @see org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter#getPath(javax.jcr.Session, java.lang.Object)
     * @throws JcrMappingException
     */
    public String getPath(Session session, Object object) {
        ClassDescriptor classDescriptor = getClassDescriptor(object.getClass());

        final FieldDescriptor pathFieldDescriptor = classDescriptor.getPathFieldDescriptor();
        if (pathFieldDescriptor == null) {
            throw new JcrMappingException("Class of type: " 
                    + object.getClass().getName()
                    + " has no path mapping. Maybe attribute path=\"true\" for a field element of this class in jcrmapping.xml is missing?"
            );
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
    private Object retrieveSimpleFields(Session session, 
                                        ClassDescriptor classDescriptor, 
                                        Node node, 
                                        Object object) {
        Object initializedBean = object;
        try {
            Iterator fieldDescriptorIterator = classDescriptor.getFieldDescriptors().iterator();
    
            while (fieldDescriptorIterator.hasNext()) {
                FieldDescriptor fieldDescriptor = (FieldDescriptor) fieldDescriptorIterator.next();
    
                String fieldName = fieldDescriptor.getFieldName();
                String propertyName = fieldDescriptor.getJcrName();
    
                if (fieldDescriptor.isPath()) {
                    if (null == initializedBean) { // HINT: lazy initialize target bean
                        initializedBean = ReflectionUtils.newInstance(classDescriptor.getClassName());
                    }
                    
                    ReflectionUtils.setNestedProperty(initializedBean, fieldName, node.getPath());
                } else {
                    if (node.hasProperty(propertyName)) {
                        Value propValue = node.getProperty(propertyName).getValue();
                        if (null != propValue && null == initializedBean) { // HINT: lazy initialize target bean
                            initializedBean = ReflectionUtils.newInstance(classDescriptor.getClassName());
                        }

                        AtomicTypeConverter converter= getAtomicTypeConverter(fieldDescriptor, 
                                                                              initializedBean, 
                                                                              fieldName);
                        
                        Object fieldValue = converter.getObject(propValue);
                        ReflectionUtils.setNestedProperty(initializedBean, fieldName, fieldValue);
                    }
                }
            }
        }
        catch(RepositoryException re) {
            throw new org.apache.portals.graffito.jcr.exception.RepositoryException(
                    "Cannot retrieve properties of object"
                    + object
                    + " from node "
                    + node,
                    re);
        }
        
        return initializedBean;
    }
    
    /**
     * Retrieve bean fields
     */
    private void retrieveBeanFields(Session session,
                                    ClassDescriptor classDescriptor,
                                    Node node,
                                    String path,
                                    Object object) {
        Iterator beanDescriptorIterator = classDescriptor.getBeanDescriptors().iterator();
        while (beanDescriptorIterator.hasNext()) {
            BeanDescriptor beanDescriptor = (BeanDescriptor) beanDescriptorIterator.next();
            String beanName = beanDescriptor.getFieldName();
            Class beanClass = ReflectionUtils.getPropertyType(object, beanName);
            Object bean = null;
            if (beanDescriptor.isInline()) {
                bean = this.retrieveSimpleFields(session, getClassDescriptor(beanClass), node, null);
            }
            else if (null != beanDescriptor.getBeanConverter()) {
                bean = beanDescriptor.getBeanConverter().getObject(session,
                        node,
                        this.mapper,
                        beanName,
                        beanClass);
            }
            else {
                bean = this.getObject(session,
                                      beanClass,
                                      path + "/" + beanDescriptor.getJcrName());
            }
            ReflectionUtils.setNestedProperty(object, beanName, bean);
        }
    }

    /**
     * Retrieve Collection fields
     */
    private void retrieveCollectionFields(Session session,
                                          ClassDescriptor classDescriptor,
                                          Node node,
                                          Object object)  {
        Iterator collectionDescriptorIterator = classDescriptor.getCollectionDescriptors()
                                                               .iterator();
        while (collectionDescriptorIterator.hasNext()) {
            CollectionDescriptor collectionDescriptor = (CollectionDescriptor)
                collectionDescriptorIterator.next();
            CollectionConverter collectionConverter = this.getCollectionConverter(session, collectionDescriptor);
            Class collectionFieldClass = ReflectionUtils.getPropertyType(object,
                    collectionDescriptor.getFieldName());
            ManageableCollection collection = collectionConverter.getCollection(session,
                                                                                node,
                                                                                collectionDescriptor,
                                                                                collectionFieldClass);
            ReflectionUtils.setNestedProperty(object,
                                              collectionDescriptor.getFieldName(),
                                              collection);
        }
    }

    /**
     * Insert Bean fields
     */
    private void insertBeanFields(Session session,
                                  Object object,
                                  ClassDescriptor classDescriptor,
                                  Node objectNode) {
        Iterator beanDescriptorIterator = classDescriptor.getBeanDescriptors().iterator();
        while (beanDescriptorIterator.hasNext()) {
            BeanDescriptor beanDescriptor = (BeanDescriptor) beanDescriptorIterator.next();
            String jcrName = beanDescriptor.getJcrName();
            Object bean = ReflectionUtils.getNestedProperty(object,
                                                            beanDescriptor.getFieldName());
            if (bean != null) {
                if (beanDescriptor.isInline()) {
                    this.storeSimpleFields(session, bean, getClassDescriptor(bean.getClass()), objectNode);
                }
                else if (null != beanDescriptor.getBeanConverter()) {
                    beanDescriptor.getBeanConverter().insert(session,
                            objectNode,
                            this.mapper,
                            jcrName,
                            object);
                }
                else {
                    this.insert(session, objectNode, jcrName, bean);
                }
            }
        }
    }

    /**
     * Update Bean fields
     */
    private void updateBeanFields(Session session,
                                  Object object,
                                  ClassDescriptor classDescriptor,
                                  Node objectNode) {
        String jcrName = null;
        try {
            Iterator beanDescriptorIterator = classDescriptor.getBeanDescriptors().iterator();
            while (beanDescriptorIterator.hasNext()) {
                BeanDescriptor beanDescriptor = (BeanDescriptor) beanDescriptorIterator.next();
                jcrName = beanDescriptor.getJcrName();
                Object bean = ReflectionUtils.getNestedProperty(object,
                                                                beanDescriptor.getFieldName());

                // if the bean is null, remove existing node
                if ((bean == null)) {
                    if (beanDescriptor.isInline()) {
                        Class beanClass = ReflectionUtils.getPropertyType(object, beanDescriptor.getFieldName());
                        this.storeSimpleFields(session, bean, getClassDescriptor(beanClass), objectNode);
                    }
                    else if (null != beanDescriptor.getBeanConverter()) {
                        beanDescriptor.getBeanConverter().remove(session, objectNode, this.mapper, jcrName);
                    }
                    else {
                        if (objectNode.hasNode(jcrName)) {
                            objectNode.getNode(jcrName).remove();
                        }
                    }
                } 
                else {
                    if (beanDescriptor.isInline()) {
                        this.storeSimpleFields(session, bean, getClassDescriptor(bean.getClass()), objectNode);
                    }
                    else if (null != beanDescriptor.getBeanConverter()) {
                        beanDescriptor.getBeanConverter().update(session,
                                objectNode,
                                this.mapper, 
                                jcrName,
                                bean);
                    }
                    else {
                        this.update(session, objectNode, jcrName, bean);
                    }
                }
            }
        } 
        catch(VersionException ve) {
            throw new PersistenceException("Cannot remove bean at path " + jcrName,
                    ve);
        }
        catch(LockException le) {
            throw new PersistenceException("Cannot remove bean at path " + jcrName + ". Item is locked.",
                    le);
        }
        catch(ConstraintViolationException cve) {
            throw new PersistenceException("Cannot remove bean at path " + jcrName + ". Contraint violation.",
                    cve);
        }
        catch(RepositoryException re) {
            throw new org.apache.portals.graffito.jcr.exception.RepositoryException(
                    "Cannot remove bean at path " + jcrName,
                    re);
        }
    }

    private void insertCollectionFields(Session session,
                                        Object object,
                                        ClassDescriptor classDescriptor,
                                        Node objectNode) {
        Iterator collectionDescriptorIterator = classDescriptor.getCollectionDescriptors()
                                                               .iterator();
        while (collectionDescriptorIterator.hasNext()) {
            CollectionDescriptor collectionDescriptor = (CollectionDescriptor)
                collectionDescriptorIterator.next();
            CollectionConverter collectionConverter = this.getCollectionConverter(session, collectionDescriptor);
            Object collection = ReflectionUtils.getNestedProperty(object, collectionDescriptor.getFieldName());
            ManageableCollection manageableCollection = ManageableCollectionUtil
                .getManageableCollection(collection);
            collectionConverter.insertCollection(session,
                                                 objectNode,
                                                 collectionDescriptor,
                                                 manageableCollection);
        }
    }

    private void updateCollectionFields(Session session,
                                        Object object,
                                        ClassDescriptor classDescriptor,
                                        Node objectNode) {
        Iterator collectionDescriptorIterator = classDescriptor.getCollectionDescriptors()
                                                               .iterator();
        while (collectionDescriptorIterator.hasNext()) {
            CollectionDescriptor collectionDescriptor = (CollectionDescriptor)
                collectionDescriptorIterator.next();
            CollectionConverter collectionConverter = this.getCollectionConverter(session, collectionDescriptor);
            Object collection = ReflectionUtils.getNestedProperty(object,
                    collectionDescriptor.getFieldName());
            ManageableCollection manageableCollection = ManageableCollectionUtil
                .getManageableCollection(collection);
            collectionConverter.updateCollection(session,
                                                 objectNode,
                                                 collectionDescriptor,
                                                 manageableCollection);
        }
    }

    private void storeSimpleFields(Session session,
                                   Object object,
                                   ClassDescriptor classDescriptor,
                                   Node objectNode) {
        try {
            ValueFactory valueFactory = session.getValueFactory();
    
            Iterator fieldDescriptorIterator = classDescriptor.getFieldDescriptors().iterator();
            while (fieldDescriptorIterator.hasNext()) {
                FieldDescriptor fieldDescriptor = (FieldDescriptor) fieldDescriptorIterator.next();
    
                //Of course, Path field is not updated as property
                if (fieldDescriptor.isPath()) {
                    continue;
                }
    
                String fieldName = fieldDescriptor.getFieldName();
                String jcrName = fieldDescriptor.getJcrName();
    
                boolean protectedProperty= fieldDescriptor.isJcrProtected();
                
                if(objectNode.hasProperty(jcrName)) {
                    protectedProperty= objectNode.getProperty(jcrName).getDefinition().isProtected();
                }
    
                if(!protectedProperty) { // DO NOT TRY TO WRITE PROTECTED PROPERTIES
                    Object fieldValue = ReflectionUtils.getNestedProperty(object, fieldName);
                    AtomicTypeConverter converter= getAtomicTypeConverter(fieldDescriptor, 
                                                                          object, 
                                                                          fieldName); 
                    Value value = converter.getValue(valueFactory, fieldValue);
    
                    // Check if the node property is "autocreated"
                    boolean autoCreated= fieldDescriptor.isJcrAutoCreated();
    
                    if(objectNode.hasProperty(jcrName)) {
                        autoCreated= objectNode.getProperty(jcrName).getDefinition().isAutoCreated();
                    }
                    
                    if(!autoCreated) {
                        // Check if mandatory property are not null
                        checkMandatoryProperty(objectNode, fieldDescriptor, value);
                    }
    
                    objectNode.setProperty(jcrName, value);
                }
            }
        }
        catch(RepositoryException re) {
            throw new org.apache.portals.graffito.jcr.exception.RepositoryException(
                    "Cannot persist properties of object" + object,
                    re);
        }
    }

    private CollectionConverter getCollectionConverter(Session session, CollectionDescriptor collectionDescriptor) {
        String className = collectionDescriptor.getCollectionConverterClassName();
        Map atomicTypeConverters= this.atomicTypeConverterProvider.getAtomicTypeConverters();
        if (className == null) {
            return new DefaultCollectionConverterImpl(atomicTypeConverters, this, this.mapper);
        } else {
            return (CollectionConverter) ReflectionUtils.invokeConstructor(className,
                    new Object[] {atomicTypeConverters, this, this.mapper});
        }

    }

    private void checkMandatoryProperty(Node objectNode,
                                        FieldDescriptor fieldDescriptor,
                                        Value value) throws RepositoryException {
        PropertyDefinition[] propertyDefinitions = objectNode.getPrimaryNodeType()
                                                             .getDeclaredPropertyDefinitions();
        for (int i = 0; i < propertyDefinitions.length; i++) {
            PropertyDefinition definition = propertyDefinitions[i];
            if (definition.getName().equals(fieldDescriptor.getJcrName())
                && definition.isMandatory() 
                && (definition.isAutoCreated() == false)
                && (value == null)) 
            {
                throw new PersistenceException("Class of type:"
                        + fieldDescriptor.getClassDescriptor().getClassName() 
                        + " has property: "
                        + fieldDescriptor.getFieldName()
                        + " declared as JCR property: "
                        + fieldDescriptor.getJcrName()
                        + " This property is mandatory but property in bean has value null");
            }
        }
    }
    
    private AtomicTypeConverter getAtomicTypeConverter(FieldDescriptor fd, 
                                                       Object object, 
                                                       String fieldName) {
        Class fieldTypeClass = null;
        if (null != fd.getFieldTypeClass()) {
            fieldTypeClass = fd.getFieldTypeClass();
        }
        else if (null != object) {
            fieldTypeClass = ReflectionUtils.getPropertyType(object, fieldName);
        }

        if (null != fieldTypeClass) {
            return this.atomicTypeConverterProvider.getAtomicTypeConverter(fieldTypeClass);
        }
        else {
            return NULL_CONVERTER;
        }
    }
    
    protected ClassDescriptor getClassDescriptor(Class beanClass) {
        ClassDescriptor classDescriptor = mapper.getClassDescriptor(beanClass);
        if(null == classDescriptor) {
            throw new JcrMappingException("Class of type: " + beanClass.getName()
                    + " is not JCR persistable. Maybe element 'class-descriptor' for this type in mapping file is missing");
        }
  
        return classDescriptor;
    }
}
