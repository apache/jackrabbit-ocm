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
package org.apache.jackrabbit.ocm.nodemanagement.impl.jackrabbit;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.OnParentVersionAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.core.nodetype.InvalidNodeTypeDefException;
import org.apache.jackrabbit.core.nodetype.NodeTypeRegistry;
import org.apache.jackrabbit.core.nodetype.xml.NodeTypeReader;
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
import org.apache.jackrabbit.ocm.nodemanagement.exception.NodeTypeRemovalException;
import org.apache.jackrabbit.ocm.nodemanagement.exception.OperationNotSupportedException;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.QNodeDefinition;
import org.apache.jackrabbit.spi.QNodeTypeDefinition;
import org.apache.jackrabbit.spi.QPropertyDefinition;
import org.apache.jackrabbit.spi.QValue;
import org.apache.jackrabbit.spi.QValueConstraint;
import org.apache.jackrabbit.spi.commons.nodetype.QNodeDefinitionBuilder;
import org.apache.jackrabbit.spi.commons.nodetype.QNodeTypeDefinitionBuilder;
import org.apache.jackrabbit.spi.commons.nodetype.QPropertyDefinitionBuilder;

/** This is the NodeTypeManager implementation for Apache Jackrabbit.
 *
 * @author <a href="mailto:okiessler@apache.org">Oliver Kiessler</a>
 */
public class NodeTypeManagerImpl implements NodeTypeManager
{
    /**
     * Logging.
     */
    private static Log log = LogFactory.getLog(NodeTypeManagerImpl.class);
    private static final boolean debug = false;

    /** Namespace helper class for Jackrabbit.
     */
    private NamespaceHelper namespaceHelper = new NamespaceHelper();

    /** Creates a new instance of NodeTypeManagerImpl. */
    public NodeTypeManagerImpl()
    {
    }

    /**
     * @see org.apache.jackrabbit.ocm.nodemanagement.NodeTypeManager#createNamespace
     */
    public void createNamespace(Session session, String namespace, String namespaceUri)
    throws NamespaceCreationException
    {
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

    /**
     * @see org.apache.jackrabbit.ocm.nodemanagement.NodeTypeManager#createNodeTypes
     */
    public void createNodeTypes(Session session, MappingDescriptor mappingDescriptor)
    throws NodeTypeCreationException
    {
    	if (mappingDescriptor != null && mappingDescriptor.getClassDescriptorsByClassName().size() > 0)
        {

        }
        else
        {
            throw new NodeTypeCreationException("The MappingDescriptor can't be null or empty.");
        }
    }

    /**
     * @see org.apache.jackrabbit.ocm.nodemanagement.NodeTypeManager#createNodeTypes
     */
    public void createNodeTypes(Session session, ClassDescriptor[] classDescriptors)
    throws NodeTypeCreationException
    {
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

    /**
     * @see org.apache.jackrabbit.ocm.nodemanagement.NodeTypeManager#createNodeTypesFromMappingFiles
     */
    public void createNodeTypesFromMappingFiles(Session session,
            InputStream[] mappingXmlFiles)
            throws NodeTypeCreationException
    {
    }

    /**
     * @see org.apache.jackrabbit.ocm.nodemanagement.NodeTypeManager#createSingleNodeType
     */
    public void createSingleNodeType(Session session, ClassDescriptor classDescriptor)
    throws NodeTypeCreationException
    {
        try
        {
            getNamespaceHelper().setRegistry(session.getWorkspace().getNamespaceRegistry());
            ArrayList list = new ArrayList();

            if (classDescriptor.getJcrType() != null &&
                    (classDescriptor.getJcrType().startsWith("nt:")
                    || classDescriptor.getJcrType().startsWith("mix:")))
            {
                throw new NodeTypeCreationException("Namespace nt and mix are reserved namespaces. Please specify your own.");
            }

            if (checkSuperTypes(session.getWorkspace().getNodeTypeManager(),
                    classDescriptor.getJcrSuperTypes()))
            {
                Name nodeTypeName = getNodeTypeName(classDescriptor.getJcrType(),
			classDescriptor.getClassName());

                List<QPropertyDefinition> propDefs = new ArrayList<QPropertyDefinition>();
                List<QNodeDefinition> nodeDefs = new ArrayList<QNodeDefinition>();
                if (classDescriptor.getFieldDescriptors() != null)
                {
                    Iterator fieldIterator = classDescriptor.getFieldDescriptors().iterator();
                    while (fieldIterator.hasNext())
                    {
                        FieldDescriptor field = (FieldDescriptor) fieldIterator.next();
                        if (!field.isPath()) {
                            propDefs.add(getPropertyDefinition(field.getFieldName(), field, nodeTypeName));
                        }
                    }
                }

                if (classDescriptor.getBeanDescriptors() != null) {
                    Iterator beanIterator = classDescriptor.getBeanDescriptors().iterator();
                    while (beanIterator.hasNext()) {
                        BeanDescriptor field = (BeanDescriptor) beanIterator.next();
                        if (this.isPropertyType(field.getJcrType())) {
                            propDefs.add(getPropertyDefinition(field.getFieldName(), field, nodeTypeName));
                        } else {
                            nodeDefs.add(getNodeDefinition(field.getFieldName(), field, nodeTypeName));
                        }
                    }
                }

                if (classDescriptor.getCollectionDescriptors() != null) {
                    Iterator collectionIterator = classDescriptor.getCollectionDescriptors().iterator();
                    while (collectionIterator.hasNext()) {
                        CollectionDescriptor field = (CollectionDescriptor) collectionIterator.next();
                        if (this.isPropertyType(field.getJcrType())) {
                            propDefs.add(getPropertyDefinition(field.getFieldName(), field, nodeTypeName));
                        } else {
                            nodeDefs.add(getNodeDefinition(field.getFieldName(), field, nodeTypeName));
                        }
                    }
                }

                 QNodeTypeDefinition nodeTypeDef = getNodeTypeDef(
                         classDescriptor.getJcrType(),
                         classDescriptor.getJcrSuperTypes(),
                         classDescriptor.getClassName(),
                         nodeTypeName,
                         propDefs,
                         nodeDefs,
                         classDescriptor.getJcrMixinTypes(),
                         classDescriptor.isAbstract(),
                         //TODO:is this correkt, how to decide whether mixin or not?
                         classDescriptor.isInterface());

                list.add(nodeTypeDef);
                createNodeTypesFromList(session, list);
                log.info("Registered JCR node type '" + nodeTypeDef.getName() +
                        "' for class '" + classDescriptor.getClassName() + "'");
            }
            else
            {
                throw new NodeTypeCreationException("JCR supertypes could not be resolved.");
            }
        }
        catch (Exception e)
        {
            log.error("Could not create node types from class descriptor.", e);
            throw new NodeTypeCreationException(e);
        }
    }

    /** Checks if all JCR super types for a given node type exist.
     *
     * @param ntMgr NodeTypeManager
     * @param superTypes Comma separated String with JCR node types
     * @return true/false
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

        private Name getNodeTypeName(String jcrNodeType,String className) {
                Name name = null;

                if (jcrNodeType != null && (!jcrNodeType.equals(""))) {
                        name = getNamespaceHelper().getName(jcrNodeType);

                } else {
                        name = getNamespaceHelper().getName(className);

                }
                return name;
        }

    /** Creates a NodeTypeDef object.
     *
     * @param jcrNodeType Name of JCR node type
     * @param jcrSuperTypes JCR node super types
     * @return type
     */
    public QNodeTypeDefinition getNodeTypeDef(String jcrNodeType, String jcrSuperTypes, String className,
            Name jcrNodeTypeName, List<QPropertyDefinition> propDefs, List<QNodeDefinition> nodeDefs,
            String[] jcrMixinTypes, boolean isAbstract, boolean isMixin)
    {
        QNodeTypeDefinitionBuilder ntdb = new QNodeTypeDefinitionBuilder();
        ntdb.setAbstract(isAbstract);
        ntdb.setChildNodeDefs(nodeDefs.toArray(QNodeDefinition.EMPTY_ARRAY));
        //ntdb.setMixin(classDescriptor.isAbstract());
        ntdb.setMixin(isMixin);
        ntdb.setName(jcrNodeTypeName);
        ntdb.setOrderableChildNodes(false);
        //ntdb.setPrimaryItemName(primaryItemName);
        ntdb.setPropertyDefs(propDefs.toArray(QPropertyDefinition.EMPTY_ARRAY));
        ntdb.setQueryable(true);
        ntdb.setSupertypes( getJcrSuperTypes(jcrSuperTypes) );
        ntdb.setSupportedMixinTypes( getJcrMixinTypes(jcrMixinTypes) );

        return ntdb.build();
    }

    /** Creates a PropDefImpl object.
     *
     * @param fieldName The name of the field
     * @param field property definition descriptor
     * @param declaringNodeType Node Type QName where the property belongs to
     * @return property
     */
    public QPropertyDefinition getPropertyDefinition(String fieldName,
            PropertyDefDescriptor field, Name declaringNodeType)
    {
            Name name = null;

        if (field.getJcrName() != null)
        {
            name = getNamespaceHelper().getName(field.getJcrName());
        }
        else
        {
            name = getNamespaceHelper().getName(fieldName);
        }

        int requiredType = PropertyType.UNDEFINED;

        if (field.getJcrType() != null)
        {
            requiredType = PropertyType.valueFromName(field.getJcrType());
        }
        else
        {
            log.info("No property type set for " + name.getLocalName() +
                    ". Setting 'String' type.");
            requiredType = PropertyType.STRING;
        }

        int onParentVersion = OnParentVersionAction.IGNORE;

        if (field.getJcrOnParentVersion() != null &&
                field.getJcrOnParentVersion().length() > 0)
        {
            onParentVersion = OnParentVersionAction.valueFromName(field.getJcrOnParentVersion());
        }

        QPropertyDefinitionBuilder pdb = new QPropertyDefinitionBuilder();
        pdb.setAutoCreated(field.isJcrAutoCreated());
        pdb.setAvailableQueryOperators(new String[0]);
        pdb.setDeclaringNodeType(declaringNodeType);
        pdb.setDefaultValues(QValue.EMPTY_ARRAY);
        pdb.setFullTextSearchable(false);
        pdb.setMandatory(field.isJcrMandatory());
        pdb.setMultiple(field.isJcrMultiple());
        pdb.setName(name);
        pdb.setOnParentVersion(onParentVersion);
        pdb.setProtected(field.isJcrProtected());
        pdb.setQueryOrderable(false);
        pdb.setRequiredType(requiredType);
        pdb.setValueConstraints(QValueConstraint.EMPTY_ARRAY);

        return pdb.build();
    }

    /** Creates a NodeDefImpl object.
     *
     * @param fieldName Name of the field
     * @param field child node definition descriptor
     * @param declaringNodeType Node Type QName where the chid node belongs to
     * @return child node definition
     */
    private QNodeDefinition getNodeDefinition(String fieldName,
        ChildNodeDefDescriptor field, Name declaringNodeType) {

            Name name = null;

        if (field.getJcrName() != null) {
            name = getNamespaceHelper().getName(field.getJcrName());
        } else {
            name = getNamespaceHelper().getName("*");
        }

        int onParentVersion = OnParentVersionAction.IGNORE;

        if (field.getJcrOnParentVersion() != null
            && field.getJcrOnParentVersion().length() > 0) {
            onParentVersion = OnParentVersionAction.valueFromName(field.getJcrOnParentVersion());
        }

        QNodeDefinitionBuilder ndb = new QNodeDefinitionBuilder();
        ndb.setAllowsSameNameSiblings(field.isJcrSameNameSiblings());
        ndb.setAutoCreated(field.isJcrAutoCreated());
        ndb.setDeclaringNodeType(declaringNodeType);
        ndb.setDefaultPrimaryType(getNamespaceHelper().getName(field.getDefaultPrimaryType()));
        ndb.setMandatory(field.isJcrMandatory());
        ndb.setName(name);
        ndb.setOnParentVersion(onParentVersion);
        ndb.setProtected(field.isJcrProtected());
        ndb.setRequiredPrimaryTypes(getJcrSuperTypes(field.getJcrType()));

        return ndb.build();
    }

    /**
     *
     * @param propDef
     * @return
     */
    protected String showPropertyDefinition(PropertyDefinition propDef)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("----");
        sb.append("\nName: " + propDef.getName());
        sb.append("\nAutocreated: " + propDef.isAutoCreated());
        sb.append("\nMandatory: " + propDef.isMandatory());
        sb.append("\n----");
        return sb.toString();
    }

    /** Creates a QName array from a comma separated list of JCR super types in
     * a given String.
     *
     * @param superTypes JCR super types
     * @return qNameSuperTypes
     */
    public Name[] getJcrSuperTypes(String superTypes)
    {
        return getNames(superTypes.split(","), "super type");
    }


    public Name[] getJcrMixinTypes(String[] jcrMixinTypes)
    {
    	return getNames(jcrMixinTypes, "mixin type");
    }


    private Name[] getNames(String[] jcrTypeNames, String logTypeKind)
    {
    	Name[] names = null;
        if (jcrTypeNames != null && jcrTypeNames.length > 0)
        {
            log.debug("JCR " + logTypeKind + "'s types found: " + jcrTypeNames.length);
            names = new Name[jcrTypeNames.length];
            for (int i = 0; i < jcrTypeNames.length; i++)
            {
                String superTypeName = jcrTypeNames[i].trim();
                names[i] = getNamespaceHelper().getName(superTypeName);
                log.debug("Setting JCR " + logTypeKind +  ": " + superTypeName);
            }
        }

        return names;
    }

    /**
     * @see org.apache.jackrabbit.ocm.nodemanagement.NodeTypeManager#createSingleNodeTypeFromMappingFile
     */
    public void createSingleNodeTypeFromMappingFile(Session session,
            InputStream mappingXmlFile, String jcrNodeType)
            throws NodeTypeCreationException
    {
    }

    /**
     * @see org.apache.jackrabbit.ocm.nodemanagement.NodeTypeManager#createNodeTypeFromClass
     */
    public void createNodeTypeFromClass(Session session, Class clazz,
            String jcrNodeType, boolean reflectSuperClasses)
            throws NodeTypeCreationException
    {
    }

    /**
     * @see org.apache.jackrabbit.ocm.nodemanagement.NodeTypeManager#createNodeTypesFromConfiguration
     */
    public void createNodeTypesFromConfiguration(Session session,
            InputStream jcrRepositoryConfigurationFile)
            throws OperationNotSupportedException, NodeTypeCreationException
    {
        try
        {
            QNodeTypeDefinition[] types = NodeTypeReader.read(jcrRepositoryConfigurationFile);

            ArrayList list = new ArrayList();
            for (int i = 0; i < types.length; i++)
            {
                list.add(types[i]);
            }

            createNodeTypesFromList(session, list);
            log.info("Registered " + list.size() + " nodetypes from xml configuration file.");
        }
        catch (Exception e)
        {
            log.error("Could not create node types from configuration file.", e);
            throw new NodeTypeCreationException(e);
        }
    }

    /**
     *
     * @param session
     * @param nodeTypes
     * @throws org.apache.jackrabbit.core.nodetype.InvalidNodeTypeDefException
     * @throws javax.jcr.RepositoryException
     */
    private void createNodeTypesFromList(Session session, List nodeTypes)
    throws InvalidNodeTypeDefException, RepositoryException
    {
        getNodeTypeRegistry(session).registerNodeTypes(nodeTypes);
    }

    /**
     * @see org.apache.jackrabbit.ocm.nodemanagement.NodeTypeManager#removeNodeTypes
     */
    public void removeNodeTypesFromConfiguration(Session session, InputStream jcrRepositoryConfigurationFile)
    throws NodeTypeRemovalException
    {
    	try
        {
            QNodeTypeDefinition[] types = NodeTypeReader.read(jcrRepositoryConfigurationFile);

            ArrayList list = new ArrayList();
            list.addAll(Arrays.asList(types));

            removeNodeTypesFromList(session, list);
            log.info("Registered " + list.size() + " nodetypes from xml configuration file.");
        }
        catch (Exception e)
        {
            log.error("Could not create node types from configuration file.", e);
            throw new NodeTypeRemovalException(e);
        }
    }

    private void removeNodeTypesFromList(Session session, List nodeTypes)
    throws NodeTypeRemovalException
    {
        for (Iterator nodeTypeIterator = nodeTypes.iterator(); nodeTypeIterator.hasNext();)
        {
			NodeTypeDefinition nodeTypeDef = (NodeTypeDefinition) nodeTypeIterator.next();
			this.removeSingleNodeType(session, nodeTypeDef.getName());

		}

    }

    /**
     * @see org.apache.jackrabbit.ocm.nodemanagement.NodeTypeManager#createSingleNodeTypeFromMappingFile
     */
    public void removeNodeTypesFromMappingFile(Session session, InputStream[] mappingXmlFile)
            throws NodeTypeRemovalException
    {
    }

    public void removeSingleNodeType(Session session, Name name)
    throws NodeTypeRemovalException
    {
        try
        {
            getNodeTypeRegistry(session).unregisterNodeType(name);
        }
        catch (Exception e)
        {
            throw new NodeTypeRemovalException(e);
        }
    }

    /**
     * @see org.apache.jackrabbit.ocm.nodemanagement.NodeTypeManager#removeSingleNodeType
     */
    public void removeSingleNodeType(Session session, String jcrNodeType)
    throws NodeTypeRemovalException
    {
        try
        {
            getNodeTypeRegistry(session).unregisterNodeType(getNamespaceHelper().getName(jcrNodeType));
        }
        catch (Exception e)
        {
            throw new NodeTypeRemovalException(e);
        }
    }

    /** Returns the jackrabbit NodeTypeRegistry from an open session.
     *
     * @param session Repository session
     * @return nodeTypeRegistry
     */
    private NodeTypeRegistry getNodeTypeRegistry(Session session)
    throws RepositoryException
    {
        Workspace wsp = session.getWorkspace();
        javax.jcr.nodetype.NodeTypeManager ntMgr = wsp.getNodeTypeManager();
        NodeTypeRegistry ntReg =
                ((org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl) ntMgr).getNodeTypeRegistry();
        return ntReg;
    }

    /**
     * @see org.apache.jackrabbit.ocm.nodemanagement.NodeTypeManager#getPrimaryNodeTypeNames
     */
    public List getPrimaryNodeTypeNames(Session session, String namespace)
    {
        return null;
    }

    /**
     * @see org.apache.jackrabbit.ocm.nodemanagement.NodeTypeManager#getAllPrimaryNodeTypeNames
     */
    public List getAllPrimaryNodeTypeNames(Session session)
    {
        return null;
    }

    /** Getter for property namespaceHelper.
     *
     * @return namespaceHelper
     */
    public NamespaceHelper getNamespaceHelper()
    {
        return namespaceHelper;
    }

    /** Setter for property namespaceHelper.
     *
     * @param object namespaceHelper
     */
    public void setNamespaceHelper(NamespaceHelper object)
    {
        this.namespaceHelper = object;
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
