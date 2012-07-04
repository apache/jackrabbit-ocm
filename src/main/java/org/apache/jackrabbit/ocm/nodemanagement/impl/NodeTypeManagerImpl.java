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
package org.apache.jackrabbit.ocm.nodemanagement.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeDefinitionTemplate;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;
import javax.jcr.version.OnParentVersionAction;

import org.apache.jackrabbit.ocm.mapper.model.BeanDescriptor;
import org.apache.jackrabbit.ocm.mapper.model.ChildNodeDefDescriptor;
import org.apache.jackrabbit.ocm.mapper.model.ClassDescriptor;
import org.apache.jackrabbit.ocm.mapper.model.CollectionDescriptor;
import org.apache.jackrabbit.ocm.mapper.model.FieldDescriptor;
import org.apache.jackrabbit.ocm.mapper.model.MappingDescriptor;
import org.apache.jackrabbit.ocm.mapper.model.PropertyDefDescriptor;
import org.apache.jackrabbit.ocm.nodemanagement.NodeTypeManager;
import org.apache.jackrabbit.ocm.nodemanagement.exception.NamespaceCreationException;
import org.apache.jackrabbit.ocm.nodemanagement.exception.NodeTypeCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeTypeManagerImpl implements NodeTypeManager {

    private static Logger log = LoggerFactory.getLogger(NodeTypeManagerImpl.class);
    @Override
    public void createNamespace(final Session session, final String namespace, final String namespaceUri) throws NamespaceCreationException {
        if (session != null)
        {
            try
            {
                session.getWorkspace().getNamespaceRegistry().registerNamespace(namespace, namespaceUri);
                log.info("Namespace created: " +
                        "{" + namespaceUri + "}" + namespace);
            }
            catch (Exception e)
            {
                throw new NamespaceCreationException(e);
            }
        }
    }

    @Override
    public void createNodeTypes(final Session session, final MappingDescriptor mappingDescriptor) throws NodeTypeCreationException {
        if (mappingDescriptor != null && mappingDescriptor.getClassDescriptorsByClassName().size() > 0)
        {
            final Collection classDescriptorObjects = mappingDescriptor.getClassDescriptorsByClassName().values();
            final ClassDescriptor[] classDescriptors = (ClassDescriptor[])classDescriptorObjects.toArray(new ClassDescriptor[classDescriptorObjects.size()]);
            createNodeTypes(session, classDescriptors);
        }
        else
        {
            throw new NodeTypeCreationException("The MappingDescriptor can't be null or empty.");
        }
    }

    @Override
    public void createNodeTypes(final Session session, final ClassDescriptor[] classDescriptors) throws NodeTypeCreationException {
        if (classDescriptors != null && classDescriptors.length > 0)
        {
            log.info("Trying to create " + classDescriptors.length +
                    " JCR node types.");
            for (int i = 0; i < classDescriptors.length; i++)
            {
                createSingleNodeType(session, classDescriptors[i]);
            }
        }
        else
        {
            throw new NodeTypeCreationException("The ClassDescriptor can't be null or empty.");
        }
    }

    @Override
    public void createSingleNodeType(final Session session, final ClassDescriptor classDescriptor) throws NodeTypeCreationException {
        try {
            if (classDescriptor.getJcrType() != null &&
                    (classDescriptor.getJcrType().startsWith("nt:")
                            || classDescriptor.getJcrType().startsWith("mix:"))) {
                throw new NodeTypeCreationException("Namespace nt and mix are reserved namespaces. Please specify your own.");
            }

            if (checkSuperTypes(session.getWorkspace().getNodeTypeManager(),
                    classDescriptor.getJcrSuperTypes())) {

                javax.jcr.nodetype.NodeTypeManager ntm = session.getWorkspace().getNodeTypeManager();
                final NodeTypeTemplate ntt = ntm.createNodeTypeTemplate();

                if (classDescriptor.getJcrType() == null) {
                    ntt.setName(classDescriptor.getClassName());
                } else {
                    ntt.setName(classDescriptor.getJcrType());
                }
                ntt.setAbstract(classDescriptor.isAbstract());
                if (classDescriptor.getJcrSuperTypes() != null && classDescriptor.getJcrSuperTypes().length() > 0) {
                    String[] superTypesArray = classDescriptor.getJcrSuperTypes().split(",");
                    //TODO  combine the mixins here as well as supertypes
                    // Add classDescriptor.getJcrMixinTypes() to superTypesArray
                    for (String s : classDescriptor.getJcrMixinTypes()) {
                        System.out.println(s);
                    }
                    ntt.setDeclaredSuperTypeNames(superTypesArray);
                }
                // should we also support mixins to be created?
                ntt.setMixin(false);
                ntt.setQueryable(true);
                ntt.setOrderableChildNodes(true);

                final List nodeDefinitionTemplates = ntt.getNodeDefinitionTemplates();
                final List propertyDefinitionTemplates = ntt.getPropertyDefinitionTemplates();

                if (classDescriptor.getFieldDescriptors() != null) {
                    Iterator fieldIterator = classDescriptor.getFieldDescriptors().iterator();
                    while (fieldIterator.hasNext()) {
                        FieldDescriptor field = (FieldDescriptor) fieldIterator.next();
                        if (!field.isPath()) {
                            final PropertyDefinitionTemplate pdt = getPropertyDefinition(ntm, session.getValueFactory(), field);
                            // add the just created pdt to the nodetypetemplate
                            propertyDefinitionTemplates.add(pdt);
                        }
                    }

                    if (classDescriptor.getBeanDescriptors() != null) {
                        Iterator beanIterator = classDescriptor.getBeanDescriptors().iterator();
                        while (beanIterator.hasNext()) {
                            BeanDescriptor field = (BeanDescriptor) beanIterator.next();
                            if (this.isPropertyType(field.getJcrType())) {
                                final PropertyDefinitionTemplate pdt = getPropertyDefinition(ntm, session.getValueFactory(), field);
                                // add the just created pdt to the nodetypetemplate
                                propertyDefinitionTemplates.add(pdt);
                            } else {
                                final NodeDefinitionTemplate ndt = getNodeDefinition(ntm, session.getValueFactory(), field);
                                // add the just created pdt to the nodetypetemplate
                                nodeDefinitionTemplates.add(ndt);
                            }
                        }
                    }

                    if (classDescriptor.getCollectionDescriptors() != null) {
                        Iterator collectionIterator = classDescriptor.getCollectionDescriptors().iterator();
                        while (collectionIterator.hasNext()) {
                            CollectionDescriptor field = (CollectionDescriptor) collectionIterator.next();
                            if (this.isPropertyType(field.getJcrType())) {
                                final PropertyDefinitionTemplate pdt = getPropertyDefinition(ntm, session.getValueFactory(), field);
                                // add the just created pdt to the nodetypetemplate
                                propertyDefinitionTemplates.add(pdt);
                            } else {
                                final NodeDefinitionTemplate ndt = getNodeDefinition(ntm, session.getValueFactory(), field);
                                // add the just created pdt to the nodetypetemplate
                                nodeDefinitionTemplates.add(ndt);
                            }
                        }
                    }
                    ntm.registerNodeType(ntt, false);
                    log.info("Registered JCR node type '" + ntt.getName() +
                            "' for class '" + classDescriptor.getClassName() + "'");
                }
            } else {
                throw new NodeTypeCreationException("JCR supertypes could not be resolved.");
            }
        } catch (Exception e) {
            log.error("Could not create node types from class descriptor.", e);
            throw new NodeTypeCreationException(e);
        }
    }

    private NodeDefinitionTemplate getNodeDefinition(final javax.jcr.nodetype.NodeTypeManager ntm,
                                                     final ValueFactory valueFactory,
                                                     final ChildNodeDefDescriptor field) throws RepositoryException {


        final NodeDefinitionTemplate ndt = ntm.createNodeDefinitionTemplate();
        if (field.getJcrName() != null) {
            ndt.setName(field.getJcrName());
        } else {
            ndt.setName("*");
        }

        int onParentVersion = OnParentVersionAction.IGNORE;

        if (field.getJcrOnParentVersion() != null
                && field.getJcrOnParentVersion().length() > 0) {
            onParentVersion = OnParentVersionAction.valueFromName(field.getJcrOnParentVersion());
        }
        ndt.setOnParentVersion(onParentVersion);
        ndt.setSameNameSiblings(field.isJcrSameNameSiblings());

        ndt.setAutoCreated(field.isJcrAutoCreated());
        ndt.setDefaultPrimaryTypeName(field.getDefaultPrimaryType());
        ndt.setMandatory(field.isJcrMandatory());
        ndt.setProtected(field.isJcrProtected());
        ndt.setRequiredPrimaryTypeNames(getJcrSuperTypes(field.getJcrType()));

        return ndt;
    }

    private PropertyDefinitionTemplate getPropertyDefinition(final javax.jcr.nodetype.NodeTypeManager ntm,
                                                             final ValueFactory valueFactory,
                                                             final PropertyDefDescriptor field) throws RepositoryException {
        final PropertyDefinitionTemplate pdt = ntm.createPropertyDefinitionTemplate();
        if (field.getJcrName() != null) {
            pdt.setName(field.getJcrName());
        } else {
            pdt.setName(field.getFieldName());
        }

        if (field.getJcrType() != null) {
            try {
                pdt.setRequiredType(PropertyType.valueFromName(field.getJcrType()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid property type '{}' for '{}'. Set default to String type", field.getJcrType(), field.getJcrName());
                pdt.setRequiredType(PropertyType.STRING);
            }
        } else {
            log.info("No property type set for {}. Setting 'String' type.", field.getJcrName());
            pdt.setRequiredType(PropertyType.STRING);

        }
        int onParentVersion = OnParentVersionAction.IGNORE;
        if (field.getJcrOnParentVersion() != null &&
                field.getJcrOnParentVersion().length() > 0) {
            onParentVersion = OnParentVersionAction.valueFromName(field.getJcrOnParentVersion());
        }
        pdt.setOnParentVersion(onParentVersion);

        pdt.setAutoCreated(field.isJcrAutoCreated());
        pdt.setAvailableQueryOperators(new String[0]);

        pdt.setFullTextSearchable(true);
        pdt.setMandatory(field.isJcrMandatory());
        pdt.setMultiple(field.isJcrMultiple());
        pdt.setOnParentVersion(onParentVersion);
        pdt.setProtected(field.isJcrProtected());
        pdt.setQueryOrderable(true);

        if (field instanceof  FieldDescriptor) {
            FieldDescriptor f = (FieldDescriptor) field;
            if (f.getJcrDefaultValue() != null) {
                if (pdt.getRequiredType() == PropertyType.STRING) {
                    Value[] vals = {valueFactory.createValue(f.getJcrDefaultValue())};
                    pdt.setDefaultValues(vals);
                } else {
                    log.warn("Can only set default value for String properties. Skip for field '{}'", field.getJcrName());
                }
            }
            pdt.setValueConstraints(f.getJcrValueConstraints());
        }

        return pdt;
    }

    /** Checks if all JCR super types for a given node type exist.
     *
     * @param ntMgr NodeTypeManager
     * @param superTypes Comma separated String with JCR node types
     * @return returns <code>false</code> if one of the supertypes does not exist, otherwise returns <code>true</code>
     */
    private boolean checkSuperTypes(javax.jcr.nodetype.NodeTypeManager ntMgr,
                                    String superTypes)
    {
        boolean exists = true;

        if (superTypes != null && superTypes.length() > 0)
        {
            String[] superTypesArray = superTypes.split(",");
            log.debug("JCR super types found: " + superTypesArray.length);
            for (int i = 0; i < superTypesArray.length; i++)
            {
                try
                {
                    ntMgr.getNodeType(superTypesArray[i]);
                }
                catch (Exception e)
                {
                    log.error("JCR super type '" + superTypesArray[i] + "' does not exist!");
                    exists = false;
                    break;
                }
            }
        }

        return exists;
    }

    public String[] getJcrSuperTypes(String superTypes)
    {
        return superTypes.split(",");
    }

    private boolean isPropertyType(String type)
    {
        return (type.equals(PropertyType.TYPENAME_BINARY) ||
                type.equals(PropertyType.TYPENAME_BOOLEAN) ||
                type.equals(PropertyType.TYPENAME_DATE) ||
                type.equals(PropertyType.TYPENAME_DOUBLE) ||
                type.equals(PropertyType.TYPENAME_LONG) ||
                type.equals(PropertyType.TYPENAME_NAME) ||
                type.equals(PropertyType.TYPENAME_PATH) ||
                type.equals(PropertyType.TYPENAME_REFERENCE) ||
                type.equals(PropertyType.TYPENAME_WEAKREFERENCE) ||
                type.equals(PropertyType.TYPENAME_DECIMAL) ||
                type.equals(PropertyType.TYPENAME_URI) ||
                type.equals(PropertyType.TYPENAME_STRING));
    }
}
