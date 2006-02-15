/*
 * Copyright 2000-20045 The Apache Software Foundation.
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
package org.apache.portals.graffito.jcr.mapper.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.digester.Digester;
import org.apache.portals.graffito.jcr.exception.InitMapperException;
import org.apache.portals.graffito.jcr.mapper.model.BeanDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.ClassDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.CollectionDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.FieldDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.MappingDescriptor;

/**
 * Helper class that reads the xml mapping file and load all class descriptors into memory (object graph)
 * 
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Lombart Christophe </a>
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class DigesterDescriptorReader
{
    private boolean validating = true;
    
    /**
     * Set if the mapping should be validated.
     * @param flag <tt>true</tt> if the mapping should be validated
     */
    public void setValidating(boolean flag) {
        this.validating= flag;
    }

	/**
	 * Load all class descriptors found in the xml mapping file.
	 * 
	 * @param stream the xml mapping file reference
	 * @return a {@link MappingDescriptor}
	 * 
	 */
	public MappingDescriptor loadClassDescriptors(InputStream stream)
	{
		try
		{
			Digester digester = new Digester();
			digester.setValidating(this.validating);

			digester.addObjectCreate("graffito-jcr", MappingDescriptor.class);
            digester.addSetProperties("graffito-jcr", "package", "package");

			// --------------------------------------------------------------------------------
			// Rules used for the class-descriptor element
			// --------------------------------------------------------------------------------

			digester.addObjectCreate("graffito-jcr/class-descriptor", ClassDescriptor.class);
			digester.addSetProperties("graffito-jcr/class-descriptor", "className", "className");
			digester.addSetProperties("graffito-jcr/class-descriptor", "jcrNodeType", "jcrNodeType");
            digester.addSetProperties("graffito-jcr/class-descriptor", "jcrSuperTypes", "jcrSuperTypes");
            digester.addSetProperties("graffito-jcr/class-descriptor", "jcrMixinTypes", "jcrMixinTypes");
            digester.addSetProperties("graffito-jcr/class-descriptor", "extends", "superClass");
            digester.addSetProperties("graffito-jcr/class-descriptor", "abstract", "abstract");
            digester.addSetProperties("graffito-jcr/class-descriptor", "discriminatorValue", "discriminatorValue");

			digester.addSetNext("graffito-jcr/class-descriptor", "addClassDescriptor");

			// --------------------------------------------------------------------------------
			// Rules used for the field-descriptor element
			// --------------------------------------------------------------------------------

			digester.addObjectCreate("graffito-jcr/class-descriptor/field-descriptor", FieldDescriptor.class);
			digester.addSetProperties("graffito-jcr/class-descriptor/field-descriptor", "fieldName", "fieldName");
            digester.addSetProperties("graffito-jcr/class-descriptor/field-descriptor", "fieldType", "fieldType");
			digester.addSetProperties("graffito-jcr/class-descriptor/field-descriptor", "jcrName", "jcrName");
			digester.addSetProperties("graffito-jcr/class-descriptor/field-descriptor", "id", "id");
			digester.addSetProperties("graffito-jcr/class-descriptor/field-descriptor", "path", "path");
            digester.addSetProperties("graffito-jcr/class-descriptor/field-descriptor", "descriminator", "descriminator");
            digester.addSetProperties("graffito-jcr/class-descriptor/field-descriptor", "jcrType", "jcrType");
            digester.addSetProperties("graffito-jcr/class-descriptor/field-descriptor", "jcrAutoCreated", "jcrAutoCreated");
            digester.addSetProperties("graffito-jcr/class-descriptor/field-descriptor", "jcrMandatory", "jcrMandatory");
            digester.addSetProperties("graffito-jcr/class-descriptor/field-descriptor", "jcrOnParentVersion", "jcrOnParentVersion");
            digester.addSetProperties("graffito-jcr/class-descriptor/field-descriptor", "jcrProtected", "jcrProtected");
            digester.addSetProperties("graffito-jcr/class-descriptor/field-descriptor", "jcrMultiple", "jcrMultiple");

            digester.addSetNext("graffito-jcr/class-descriptor/field-descriptor", "addFieldDescriptor");

			// --------------------------------------------------------------------------------
			// Rules used for the bean-descriptor element
			// --------------------------------------------------------------------------------

			digester.addObjectCreate("graffito-jcr/class-descriptor/bean-descriptor", BeanDescriptor.class);
			digester.addSetProperties("graffito-jcr/class-descriptor/bean-descriptor", "fieldName", "fieldName");
			digester.addSetProperties("graffito-jcr/class-descriptor/bean-descriptor", "jcrName", "jcrName");
			digester.addSetProperties("graffito-jcr/class-descriptor/bean-descriptor", "proxy", "proxy");
            digester.addSetProperties("graffito-jcr/class-descriptor/bean-descriptor", "inline", "inline");
            digester.addSetProperties("graffito-jcr/class-descriptor/bean-descriptor", "converter", "converter");
            digester.addSetProperties("graffito-jcr/class-descriptor/bean-descriptor", "jcrNodeType", "jcrNodeType");
            digester.addSetProperties("graffito-jcr/class-descriptor/bean-descriptor", "jcrAutoCreated", "jcrAutoCreated");
            digester.addSetProperties("graffito-jcr/class-descriptor/bean-descriptor", "jcrMandatory", "jcrMandatory");
            digester.addSetProperties("graffito-jcr/class-descriptor/bean-descriptor", "jcrOnParentVersion", "jcrOnParentVersion");
            digester.addSetProperties("graffito-jcr/class-descriptor/bean-descriptor", "jcrProtected", "jcrProtected");
            digester.addSetProperties("graffito-jcr/class-descriptor/bean-descriptor", "jcrSameNameSiblings", "jcrSameNameSiblings");
			
            digester.addSetNext("graffito-jcr/class-descriptor/bean-descriptor", "addBeanDescriptor");

			// --------------------------------------------------------------------------------
			// Rules used for the collection-descriptor element
			// --------------------------------------------------------------------------------

			digester.addObjectCreate("graffito-jcr/class-descriptor/collection-descriptor", CollectionDescriptor.class);
			digester.addSetProperties("graffito-jcr/class-descriptor/collection-descriptor", "fieldName", "fieldName");
			digester.addSetProperties("graffito-jcr/class-descriptor/collection-descriptor", "jcrName", "jcrName");
			digester.addSetProperties("graffito-jcr/class-descriptor/collection-descriptor", "proxy", "proxy");
			digester.addSetProperties("graffito-jcr/class-descriptor/collection-descriptor", "elementClassName", "elementClassName");
			digester.addSetProperties("graffito-jcr/class-descriptor/collection-descriptor", "collectionConverter", "collectionConverterClassName");
			digester.addSetProperties("graffito-jcr/class-descriptor/collection-descriptor", "collectionClassName", "collectionClassName");
            digester.addSetProperties("graffito-jcr/class-descriptor/collection-descriptor", "jcrNodeType", "jcrNodeType");
            digester.addSetProperties("graffito-jcr/class-descriptor/collection-descriptor", "jcrAutoCreated", "jcrAutoCreated");
            digester.addSetProperties("graffito-jcr/class-descriptor/collection-descriptor", "jcrMandatory", "jcrMandatory");
            digester.addSetProperties("graffito-jcr/class-descriptor/collection-descriptor", "jcrOnParentVersion", "jcrOnParentVersion");
            digester.addSetProperties("graffito-jcr/class-descriptor/collection-descriptor", "jcrProtected", "jcrProtected");
            digester.addSetProperties("graffito-jcr/class-descriptor/collection-descriptor", "jcrSameNameSiblings", "jcrSameNameSiblings");            
			digester.addSetNext("graffito-jcr/class-descriptor/collection-descriptor", "addCollectionDescriptor");

            return (MappingDescriptor) digester.parse(stream);
		}
		catch (Exception e)
		{
			throw new InitMapperException("Impossible to read the xml mapping file", e);
		}
	}

	/**
	 * Load all class descriptors found in the xml mapping file.
	 * 
	 * @param xmlFile the xml mapping file reference
	 * @return a {@link MappingDescriptor}
	 * 
	 */	
	public MappingDescriptor loadClassDescriptors(String xmlFile)
	{
		try
		{
			return loadClassDescriptors(new FileInputStream(xmlFile));
		}
		catch (FileNotFoundException e)
		{
			throw new InitMapperException(e);
		}
	}
}
