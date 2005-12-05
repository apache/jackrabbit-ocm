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

import java.util.Collection;
import java.util.HashMap;

/**
 * 
 * ClassDescriptor is used by the mapper to read general information on a class
 * 
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Lombart Christophe </a>
 * 
 */
public class ClassDescriptor
{

     private String className;
     private String jcrNodeType;
     private String jcrSuperTypes;
     private FieldDescriptor idFieldDescriptor;
     private FieldDescriptor pathFieldDescriptor;
     
     private HashMap fieldDescriptors = new HashMap();
     private HashMap beanDescriptors = new HashMap();
     private HashMap collectionDescriptors = new HashMap();
     private HashMap fieldNames = new HashMap();
    
    
    /**
     * @return Returns the className.
     */
    public String getClassName()
    {
        return className;
    }
    /**
     * @param className The className to set.
     */
    public void setClassName(String className)
    {
        this.className = className;
    }
    
    
    /**
     * @return Returns the jcrNodeType.
     */
    public String getJcrNodeType()
    {
        return jcrNodeType;
    }
    /**
     * @param jcrNodeType The jcrNodeType to set.
     */
    public void setJcrNodeType(String jcrNodeType)
    {
        this.jcrNodeType = jcrNodeType;
    }
    
    /**
     * Add a new FielDescriptor
     * @param fieldDescriptor the new field descriptor to add
     */
    public void addFieldDescriptor(FieldDescriptor fieldDescriptor )
    {
        fieldDescriptor.setClassDescriptor(this);
        if (fieldDescriptor.isId())
        {
           this.idFieldDescriptor = fieldDescriptor;
        }
        
        if (fieldDescriptor.isPath())
        {
        	this.pathFieldDescriptor = fieldDescriptor;
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
    public FieldDescriptor getFieldDescriptor(String fieldName)
    {
        return (FieldDescriptor) fieldDescriptors.get(fieldName);
    }
    
    /**
     * 
     * @return all {@link FieldDescriptor} defined in this ClassDescriptor
     */
    public Collection getFieldDescriptors()
    {
        return fieldDescriptors.values();   
    }
    
    /**
     * Add a new BeanDescriptor
     * @param beanDescriptor the new bean descriptor to add
     */
                  
    public void addBeanDescriptor(BeanDescriptor beanDescriptor )
    {
        beanDescriptors.put(beanDescriptor.getFieldName(), beanDescriptor);
        fieldNames.put(beanDescriptor.getFieldName(), beanDescriptor.getJcrName());
    }

    /**
     * Get the BeanDescriptor to used for a specific java bean attribute
     * @param fieldName The java bean attribute name
     * 
     * @return the {@link BeanDescriptor} found or null
     */
    public BeanDescriptor getBeanDescriptor(String fieldName)
    {
        return (BeanDescriptor) beanDescriptors.get(fieldName);
    }
    
    
    /**     
     * @return all {@link BeanDescriptor} defined in this ClassDescriptor
     */
    public Collection getBeanDescriptors()
    {
        return beanDescriptors.values();   
    }

    
    /**
     * Add a new CollectionDescriptor
     * @param collectionDescriptor the new collection descriptor to add
     */
                  
    public void addCollectionDescriptor(CollectionDescriptor collectionDescriptor )
    {
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
    public CollectionDescriptor getCollectionDescriptor(String fieldName)
    {
        return (CollectionDescriptor) collectionDescriptors.get(fieldName);
    }
        
    /**
     * @return all {@link BeanDescriptor} defined in this ClassDescriptor
     */
    public Collection getCollectionDescriptors()
    {
        return collectionDescriptors.values();   
    }
    
    /**
     * @return the fieldDescriptor ID  
     */
    public FieldDescriptor getIdFieldDescriptor()
    {
        return idFieldDescriptor;
    }
    
    /**
     * @return the fieldDescriptor path  
     */
    public FieldDescriptor getPathFieldDescriptor()
    {
        return pathFieldDescriptor;
    }
    
    /**
     * Check if this class has an ID 
     * @return true if the class has an ID
     */
    public boolean hasIdField()
    {
        return this.idFieldDescriptor != null;
    }
    
    /**
     * Get the JCR name used for one of the object attributes
     * @param fieldName the object attribute name (can be an atomic field, bean field or a collection field)
     * @return the JCR name found 
     */
    public String getJcrName(String fieldName)
    {
    	return (String) this.fieldNames.get(fieldName);
    }

    /** Get the JCR node super types.
     * 
     * @return jcrSuperTypes
     */
    public String getJcrSuperTypes()
    {
        return jcrSuperTypes;
    }

    /** Setter for JCR super types.
     * 
     * @param superTypes Comma separated list of JCR node super types
     */
    public void setJcrSuperTypes(String superTypes)
    {
        this.jcrSuperTypes = superTypes;
    }
}
