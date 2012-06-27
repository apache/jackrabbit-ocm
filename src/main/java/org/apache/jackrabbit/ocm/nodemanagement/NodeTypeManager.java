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
package org.apache.jackrabbit.ocm.nodemanagement;

import java.io.InputStream;
import java.util.List;

import javax.jcr.Session;

import org.apache.jackrabbit.ocm.mapper.model.ClassDescriptor;
import org.apache.jackrabbit.ocm.mapper.model.MappingDescriptor;
import org.apache.jackrabbit.ocm.nodemanagement.exception.NamespaceCreationException;
import org.apache.jackrabbit.ocm.nodemanagement.exception.NodeTypeCreationException;
import org.apache.jackrabbit.ocm.nodemanagement.exception.NodeTypeRemovalException;
import org.apache.jackrabbit.ocm.nodemanagement.exception.OperationNotSupportedException;


/** This interface defines the API for JCR Node Type Management implementations.
 * It does not contain any JCR vendor specific methods.
 *
 * Classes that implement this interface are used to create custom node types in
 * a JCR repository. Each JCR repository has its own way of doing this as it is
 * not defined by the JSR-170 spec. The default implementation of jcr-nodemanagement is Jackrabbit.
 *
 * In order to create JCR custom node types you need to provide an established
 * session to a JCR repository. The user that is logged into has to have the
 * necessary permissions to create nodes (user has to have "superuser" rights).
 *
 * The JCR Node Type Management tools are an extension to the jcr-mapping
 * tools. NodeTypeManager implementations depend on the jcr-mapping
 * xml file and the object model defined by jcr-mapping.
 *
 * @author <a href="mailto:okiessler@apache.org">Oliver Kiessler</a>
 */
public interface NodeTypeManager {

    /** Creates a new namespace in the repository.
     *
     * @param namespace Namespace
     * @param namespaceUri Full namespace URI
     */
    void createNamespace(Session session, String namespace, String namespaceUri)
    throws NamespaceCreationException;

    /** This method creates JCR node types based on the MappingDescriptor object
     * which is created by a jcr-mapping Mapper implementation. A
     * Mapper reads one to many jcr mapping XML File.
     *
     * @param session Repository session
     * @param mappingDescriptor Mapping descriptor object created by
     * jcr-mapping
     * @throws NodeTypeCreationException NodeTypeCreationException
     */
    void createNodeTypes(Session session, MappingDescriptor mappingDescriptor)
    throws NodeTypeCreationException;

    /** This method creates JCR node types based on ClassDescriptor objects
     * which are created by a  jcr-mapping Mapper implementation. A
     * Mapper reads one to many jcr mapping XML File.
     *
     * @param session Repository session
     * @param classDescriptors Array of ClassDescriptor objects created by
     * jcr-mapping
     * @throws NodeTypeCreationException NodeTypeCreationException
     */
    void createNodeTypes(Session session, ClassDescriptor[] classDescriptors)
    throws NodeTypeCreationException;

    /** This method creates a single JCR node type identified by its ClassDescriptor
     * read from the jcr mapping file.
     *
     * @param session Repository session
     * @param classDescriptor ClassDescriptor object created by jcr-mapping
     * @throws NodeTypeCreationException NodeTypeCreationException
     */
    void createSingleNodeType(Session session, ClassDescriptor classDescriptor)
    throws NodeTypeCreationException;


}
