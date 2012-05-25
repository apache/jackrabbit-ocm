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
package org.apache.jackrabbit.ocm.mapper.model;


/**
 *
 * CollectionDescriptor is used by the mapper to read general information on a collection field
 *
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Lombart Christophe </a>
 *
 */
public class CollectionDescriptor implements ChildNodeDefDescriptor, PropertyDefDescriptor
{

     private String fieldName;
     private String jcrName;
     private String elementClassName;
     private String jcrElementName;
     private String collectionConverterClassName;
     private String collectionClassName;
     private boolean proxy;
     private boolean autoRetrieve = true;
     private boolean autoUpdate = true;
     private boolean autoInsert = true;
     private String jcrType;
     private boolean jcrAutoCreated;
     private boolean jcrMandatory;
     private String jcrOnParentVersion;
     private boolean jcrProtected;
     private boolean jcrSameNameSiblings;
     private boolean jcrMultiple;
     private String defaultPrimaryType;

     private ClassDescriptor classDescriptor;

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getJcrName()
    {
        return jcrName;
    }

    public void setJcrName(String jcrName)
    {
        this.jcrName = jcrName;
    }

    public String getElementClassName()
    {
        return elementClassName;
    }

    public void setElementClassName(String elementClassName)
    {
        this.elementClassName = elementClassName;
    }

    public String getJcrElementName()
    {
        return jcrElementName;
    }

    public void setJcrElementName(String jcrElementName)
    {
        this.jcrElementName = jcrElementName;
    }


    public boolean isProxy()
    {
        return proxy;
    }

    public void setProxy(boolean proxy)
    {
        this.proxy = proxy;
    }



    public boolean isAutoInsert() {
		return autoInsert;
	}
	public void setAutoInsert(boolean autoInsert) {
		this.autoInsert = autoInsert;
	}
	public boolean isAutoRetrieve() {
		return autoRetrieve;
	}
	public void setAutoRetrieve(boolean autoRetrieve) {
		this.autoRetrieve = autoRetrieve;
	}
	public boolean isAutoUpdate() {
		return autoUpdate;
	}
	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

    public String getCollectionConverter()
    {
        return collectionConverterClassName;
    }

    public void setCollectionConverter(String collectionConverterClassName)
    {
        this.collectionConverterClassName = collectionConverterClassName;
    }

    /**
     *
     * @return the collection class name (can be also a Map)
     */
    public String getCollectionClassName()
    {
        return collectionClassName;
    }

    /**
     * Set the collection class name.
     * This collection class has to implement {@link org.apache.jackrabbit.ocm.manager.collectionconverter.ManageableCollection}
     * @param collectionClassName The collection class name to set
     */
    public void setCollectionClassName(String collectionClassName)
    {
        this.collectionClassName = collectionClassName;
    }

    /**
     *
     * @return The associated class descriptor
     */
    public ClassDescriptor getClassDescriptor()
    {
        return classDescriptor;
    }

    /**
     * Set the associated class descriptor
     * @param classDescriptor the class descriptor to set
     */
    public void setClassDescriptor(ClassDescriptor classDescriptor)
    {
        this.classDescriptor = classDescriptor;
    }


    public String getJcrType() {
        return jcrType;
    }

    public void setJcrType(String value) {
        this.jcrType = value;
    }

    public boolean isJcrAutoCreated()
    {
        return jcrAutoCreated;
    }

    public void setJcrAutoCreated(boolean value)
    {
        this.jcrAutoCreated = value;
    }

    public boolean isJcrMandatory()
    {
        return jcrMandatory;
    }

    public void setJcrMandatory(boolean value)
    {
        this.jcrMandatory = value;
    }

    public String getJcrOnParentVersion()
    {
        return jcrOnParentVersion;
    }

    public void setJcrOnParentVersion(String value)
    {
        this.jcrOnParentVersion = value;
    }

    public boolean isJcrProtected()
    {
        return jcrProtected;
    }

    public void setJcrProtected(boolean value)
    {
        this.jcrProtected = value;
    }

    public boolean isJcrSameNameSiblings()
    {
        return jcrSameNameSiblings;
    }

    public void setJcrSameNameSiblings(boolean value)
    {
        this.jcrSameNameSiblings = value;
    }

    public boolean isJcrMultiple() {
        return jcrMultiple;
    }

    public void setJcrMultiple(boolean value) {
        this.jcrMultiple = value;
    }

	public String toString() {
		
		return "Collection Descriptor : " +  this.getFieldName();
	}

    public String getDefaultPrimaryType() {
        return defaultPrimaryType;
    }

    public void setDefaultPrimaryType(String defaultPrimaryType) {
        this.defaultPrimaryType = defaultPrimaryType;
    }
}
