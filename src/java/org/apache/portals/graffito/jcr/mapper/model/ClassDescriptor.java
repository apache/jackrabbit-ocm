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
package org.apache.portals.graffito.jcr.mapper.model;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.portals.graffito.jcr.exception.JcrMappingException;

/**
 *
 * ClassDescriptor is used by the mapper to read general information on a class
 *
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Lombart Christophe </a>
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class ClassDescriptor {
	
	private static final Log log = LogFactory.getLog(ClassDescriptor.class);
	
    private static final String NODETYPE_PER_HIERARCHY = "nodetypeperhierarchy";
    private static final String NODETYPE_PER_CONCRETECLASS = "nodetypeperconcreteclass";

    private MappingDescriptor mappingDescriptor;
    private ClassDescriptor superClassDescriptor;
    private Collection descendantClassDescriptors = new ArrayList();

    private String className;
    private String jcrNodeType;
    private String jcrSuperTypes;
    private String[] jcrMixinTypes = new String[0];
    private FieldDescriptor idFieldDescriptor;
    private FieldDescriptor pathFieldDescriptor;
    private FieldDescriptor discriminatorFieldDescriptor;

    private Map fieldDescriptors = new HashMap();    
    private Map beanDescriptors = new HashMap();        
    private Map collectionDescriptors = new HashMap();
        
    private Map fieldNames = new HashMap();

    private String superClassName;
    private String extendsStrategy;
    private boolean abstractClass = false;
    private boolean hasDescendant = false;
   
    
    public void setAbstract(boolean flag) {
        this.abstractClass = flag;
    }

    public boolean isAbstract() {
        return this.abstractClass;
    }

    public boolean usesNodeTypePerHierarchyStrategy() {
        return NODETYPE_PER_HIERARCHY.equals(this.extendsStrategy);
    }

    public boolean usesNodeTypePerConcreteClassStrategy() {
        return NODETYPE_PER_CONCRETECLASS.equals(this.extendsStrategy);
    }
    /**
     * @return Returns the className.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className The className to set.
     */
    public void setClassName(String className) {    	   
        this.className = className;
    }

    /**
     * @return Returns the jcrNodeType.
     */
    public String getJcrNodeType() {
        return jcrNodeType;
    }

    /**
     * @param jcrNodeType The jcrNodeType to set.
     */
    public void setJcrNodeType(String jcrNodeType) {
        this.jcrNodeType = jcrNodeType;
    }

    /**
     * Add a new FielDescriptor
     * @param fieldDescriptor the new field descriptor to add
     */
    public void addFieldDescriptor(FieldDescriptor fieldDescriptor) {
        fieldDescriptor.setClassDescriptor(this);
        if (fieldDescriptor.isId()) {
            this.idFieldDescriptor = fieldDescriptor;
        }
        if (fieldDescriptor.isPath()) {
            this.pathFieldDescriptor = fieldDescriptor;
        }
        if (fieldDescriptor.isDiscriminator()) {
            this.discriminatorFieldDescriptor = fieldDescriptor;
        }

        fieldDescriptors.put(fieldDescriptor.getFieldName(), fieldDescriptor);
        fieldNames.put(fieldDescriptor.getFieldName(), fieldDescriptor.getJcrName());
    }

    /**
     * Get the FieldDescriptor to used for a specific java bean attribute
     * @param fieldName The java bean attribute name
     *
     * @return the {@link FieldDescriptor} found or null
     */
    public FieldDescriptor getFieldDescriptor(String fieldName) {
        return (FieldDescriptor) this.fieldDescriptors.get(fieldName);
    }

    /**
     *
     * @return all {@link FieldDescriptor} defined in this ClassDescriptor
     */
    public Collection getFieldDescriptors() {
        return this.fieldDescriptors.values();
    }

    /**
     * Add a new BeanDescriptor
     * @param beanDescriptor the new bean descriptor to add
     */

    public void addBeanDescriptor(BeanDescriptor beanDescriptor) {
        beanDescriptor.setClassDescriptor(this);
        beanDescriptors.put(beanDescriptor.getFieldName(), beanDescriptor);
        fieldNames.put(beanDescriptor.getFieldName(), beanDescriptor.getJcrName());
    }

    /**
     * Get the BeanDescriptor to used for a specific java bean attribute
     * @param fieldName The java bean attribute name
     *
     * @return the {@link BeanDescriptor} found or null
     */
    public BeanDescriptor getBeanDescriptor(String fieldName) {
        return (BeanDescriptor) this.beanDescriptors.get(fieldName);
    }

    /**
     * @return all {@link BeanDescriptor} defined in this ClassDescriptor
     */
    public Collection getBeanDescriptors() {
        return this.beanDescriptors.values();
    }

    /**
     * Add a new CollectionDescriptor
     * @param collectionDescriptor the new collection descriptor to add
     */

    public void addCollectionDescriptor(CollectionDescriptor collectionDescriptor) {
        collectionDescriptor.setClassDescriptor(this);
        collectionDescriptors.put(collectionDescriptor.getFieldName(), collectionDescriptor);
        fieldNames.put(collectionDescriptor.getFieldName(), collectionDescriptor.getJcrName());
    }

    /**
     * Get the CollectionDescriptor to used for a specific java bean attribute
     * @param fieldName The java bean attribute name
     *
     * @return the {@link CollectionDescriptor} found or null
     */
    public CollectionDescriptor getCollectionDescriptor(String fieldName) {
        return (CollectionDescriptor) this.collectionDescriptors.get(fieldName);
    }

    /**
     * @return all {@link BeanDescriptor} defined in this ClassDescriptor
     */
    public Collection getCollectionDescriptors() {
        return this.collectionDescriptors.values();
    }

    /**
     * @return the fieldDescriptor ID
     */
    public FieldDescriptor getIdFieldDescriptor() {
        return idFieldDescriptor;
    }

    /**
     * @return the fieldDescriptor path
     */
    public FieldDescriptor getPathFieldDescriptor() {
        if (null != this.pathFieldDescriptor) {
            return this.pathFieldDescriptor;
        }

        if (null != this.superClassDescriptor) {
            return this.superClassDescriptor.getPathFieldDescriptor();
        }

        return null;
        
    
    }

    public FieldDescriptor getDiscriminatorFieldDescriptor() {       
        if (null != this.discriminatorFieldDescriptor) {
            return this.discriminatorFieldDescriptor;
        }

        if (null != this.superClassDescriptor) {
            return this.superClassDescriptor.getDiscriminatorFieldDescriptor();
        }

        return null;        
    }

    public boolean hasDiscriminatorField() {
        return this.getDiscriminatorFieldDescriptor() != null;
    }

    /**
     * Check if this class has an ID
     * @return true if the class has an ID
     */
    public boolean hasIdField() {
        return this.idFieldDescriptor != null;
    }

    /**
     * Get the JCR name used for one of the object attributes
     * @param fieldName the object attribute name (can be an atomic field, bean field or a collection field)
     * @return the JCR name found
     */
    public String getJcrName(String fieldName) {
        return (String) this.fieldNames.get(fieldName);
    }
    
    public Map getFieldNames()
    {
        return this.fieldNames;
    }

    /** Get the JCR node super types.
     *
     * @return jcrSuperTypes
     */
    public String getJcrSuperTypes() {
        return jcrSuperTypes;
    }

    /** Setter for JCR super types.
     *
     * @param superTypes Comma separated list of JCR node super types
     */
    public void setJcrSuperTypes(String superTypes) {
        this.jcrSuperTypes = superTypes;
    }

    /**
     * Retrieve the mixin types.
     *
     * @return array of mixin types
     */
    public String[] getJcrMixinTypes() {
        return this.jcrMixinTypes;
    }

    /**
     * Sets a comma separated list of mixin types.
     *
     * @param mixinTypes command separated list of mixins
     */
//    public void setJcrMixinTypesList(String[] mixinTypes) {
//        if (null != mixinTypes) {
//            setJcrMixinTypes(mixinTypes[0].split(","));
//        }
//    }

    public void setJcrMixinTypes(String[] mixinTypes) {
        if (null != mixinTypes && mixinTypes.length == 1) {
            jcrMixinTypes = mixinTypes[0].split(" *, *");
        }
    }

    /**
     * @return Returns the mappingDescriptor.
     */
    public MappingDescriptor getMappingDescriptor() {
        return mappingDescriptor;
    }

    /**
     * @param mappingDescriptor The mappingDescriptor to set.
     */
    public void setMappingDescriptor(MappingDescriptor mappingDescriptor) {
        this.mappingDescriptor = mappingDescriptor;
    }

    /**
     * Revisit information in this descriptor and fills in more.
     */
    public void afterPropertiesSet() {
        lookupSuperDescriptor();
        lookupInheritanceSettings();
        validateInheritanceSettings();
    }

    private void validateInheritanceSettings() {
        if (NODETYPE_PER_CONCRETECLASS.equals(this.extendsStrategy)) {
            this.discriminatorFieldDescriptor = getDiscriminatorFieldDescriptor();

            if (null != this.discriminatorFieldDescriptor) {
                throw new JcrMappingException("");
            }
        }
        else if (NODETYPE_PER_HIERARCHY.equals(this.extendsStrategy)) {
            this.discriminatorFieldDescriptor = getDiscriminatorFieldDescriptor();

            if (null == this.discriminatorFieldDescriptor) {
                throw new JcrMappingException("");
            }
        }
    }



    private void lookupInheritanceSettings() {
    	     if ((null != this.superClassDescriptor) || (this.hasDescendants() ))
    	     {
    	    	       if (this.hasDiscriminatorField())
    	    	       {
    	    	    	        this.extendsStrategy = NODETYPE_PER_HIERARCHY;
    	    	       }
    	    	       else
    	    	       {
    	    	    	       this.extendsStrategy = NODETYPE_PER_CONCRETECLASS;
    	    	       }
    	     }
    }

    private void lookupSuperDescriptor() {
        if (null != this.superClassDescriptor) {
            this.fieldDescriptors = merge(this.fieldDescriptors, this.superClassDescriptor.getFieldDescriptors());
            this.beanDescriptors = merge(this.beanDescriptors, this.superClassDescriptor.getBeanDescriptors());
            this.collectionDescriptors = merge(this.collectionDescriptors, this.superClassDescriptor.getCollectionDescriptors());
            this.fieldNames.putAll(this.superClassDescriptor.getFieldNames());
            
            
        }
    }

    /**
     * @return return the super class name if defined in mapping, or
     * <tt>null</tt> if not set
     */
    public String getSuperClass() {
        return this.superClassName;
    }

    /**
     * @param className
     */
    public void setSuperClass(String className) {
        this.superClassName = className;
    }

    /**
     * @return Returns the superClassDescriptor.
     */
    public ClassDescriptor getSuperClassDescriptor() {
        return superClassDescriptor;
    }
    
    public Collection getDescendantClassDescriptors()
    {
    	     return this.descendantClassDescriptors;
    }
    
    public void addDescendantClassDescriptor(ClassDescriptor classDescriptor)
    {
    	     this.descendantClassDescriptors.add(classDescriptor);
    	     this.hasDescendant = true;
    }
    
    public boolean hasDescendants()
    {
    	    return this.hasDescendant;
    }

    /**
     * @param superClassDescriptor The superClassDescriptor to set.
     */
    public void setSuperClassDescriptor(ClassDescriptor superClassDescriptor) {
        this.superClassDescriptor= superClassDescriptor;
        superClassDescriptor.addDescendantClassDescriptor(this);
        
    }

    private Map merge(Map existing, Collection superSource) {
        if (null == superSource) {
            return existing;
        }

        Map merged = new HashMap(existing);
        for(Iterator it = superSource.iterator(); it.hasNext();) {
            FieldDescriptor fd = (FieldDescriptor) it.next();
            if (!merged.containsKey(fd.getFieldName())) {
                merged.put(fd.getFieldName(), fd);
            }
            else
            {
            	    log.warn("Field name conflict in " + this.className + " - field : " +fd.getFieldName() + " -  this  field name is also defined  in the ancestor class : " + this.getSuperClass());
            }
        }

        return merged;
    }

	public String toString() {
		
		return "Class Descriptor : " +  this.getClassName();
	}
    
    
}