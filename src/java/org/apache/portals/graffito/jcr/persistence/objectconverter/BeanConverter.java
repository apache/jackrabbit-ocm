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
package org.apache.portals.graffito.jcr.persistence.objectconverter;


import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.portals.graffito.jcr.exception.JcrMappingException;
import org.apache.portals.graffito.jcr.exception.PersistenceException;
import org.apache.portals.graffito.jcr.exception.RepositoryException;
import org.apache.portals.graffito.jcr.mapper.model.BeanDescriptor;

/**
 * Interface describing a custom bean converter. 
 *
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public interface BeanConverter {
    /**
     * Insert the object.
     *
     * @param session the JCR session
     * @param parentNode The parent node
     * @param mapper available mappings
     * @param beanName bean name to be inserter
     * @param object bean
     * 
     * @throws PersistenceException thrown in case the insert fails; marks a failure due to logic of
     *  the insert (parent node cannot be accessed, the insert fails, etc.)
     * @throws RepositoryException thrown in case the underlying repository has thrown a
     *  <code>javax.jcr.RepositoryException</code> that is not possible to be handled or
     *  wrapped in PersistenceException; marks a repository failure
     * @throws JcrMappingException throws in case the mapping of the bean is not correct
     */
    void insert(Session session, Node parentNode, BeanDescriptor descriptor, Object object)
    throws PersistenceException, RepositoryException, JcrMappingException;

    /**
     * Update repository from bean values.
     *
     * @param session the JCR session
     * @param parentNode The parent node
     * @param mapper available mappings
     * @param beanName bean name to be updated
     * @param object bean
     * 
     * @throws PersistenceException thrown in case the update fails; marks a failure due to logic
     *  of update (parent node cannot be accessed, the update fails, etc.)
     * @throws RepositoryException thrown in case the underlying repository has thrown a
     *  <code>javax.jcr.RepositoryException</code> that is not possible to be handled or
     *  wrapped in PersistenceException; marks a repository failure
     * @throws JcrMappingException throws in case the mapping of the bean is not correct
     */
    void update(Session session, Node parentNode, BeanDescriptor descriptor, Object object)
    throws PersistenceException, RepositoryException, JcrMappingException;
    
    /**
     * Retrieve a bean from the repository.
     * 
     * @param session the JCR session
     * @param parentNode The parent node
     * @param mapper available mappings
     * @param beanName bean name to be retrieved
     * @param beanClass class of the bean to be retrieved
     * 
     * @throws PersistenceException thrown in case the bean cannot be retrieved or initialized; 
     *  marks a failure due to logic of retrieval
     * @throws RepositoryException thrown in case the underlying repository has thrown a
     *  <code>javax.jcr.RepositoryException</code> that is not possible to be handled or
     *  wrapped in PersistenceException; marks a repository failure
     * @throws JcrMappingException throws in case the mapping of the bean is not correct
     */
    Object getObject(Session session, Node parentNode, BeanDescriptor descriptor, Class beanClass) 
    throws PersistenceException, RepositoryException, JcrMappingException;


    /**
     * Remove the bean from the repository.
     * 
     * @param session the JCR session
     * @param parentNode The parent node
     * @param mapper available mappings
     * @param beanName bean name to be retrieved
     * 
     * @throws PersistenceException thrown in case the bean cannot be removed; 
     *  marks a failure due to logic of removal
     * @throws RepositoryException thrown in case the underlying repository has thrown a
     *  <code>javax.jcr.RepositoryException</code> that is not possible to be handled or
     *  wrapped in PersistenceException; marks a repository failure
     * @throws JcrMappingException throws in case the mapping of the bean is not correct
     */
    void remove(Session session, Node parentNode, BeanDescriptor descriptor)
    throws PersistenceException, RepositoryException, JcrMappingException;
}
