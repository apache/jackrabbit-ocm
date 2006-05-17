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

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.portals.graffito.jcr.exception.JcrMappingException;
import org.apache.portals.graffito.jcr.exception.PersistenceException;
import org.apache.portals.graffito.jcr.exception.RepositoryException;
import org.apache.portals.graffito.jcr.mapper.model.BeanDescriptor;
import org.apache.portals.graffito.jcr.persistence.objectconverter.BeanConverter;
import org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter;

public class ParentBeanConverterImpl extends AbstractBeanConverterImpl  implements BeanConverter {

	private final static Log log = LogFactory.getLog(ParentBeanConverterImpl.class);
	
	public ParentBeanConverterImpl(ObjectConverter objectConverter) 
	{
		super(objectConverter);	
	}

	public void insert(Session session, Node parentNode, BeanDescriptor descriptor, Object object)
			throws PersistenceException, RepositoryException, 	JcrMappingException {
	}

	public void update(Session session, Node parentNode, 	BeanDescriptor descriptor, Object object)
			throws PersistenceException, RepositoryException,	JcrMappingException {
	}

	public Object getObject(Session session, Node parentNode,BeanDescriptor descriptor, Class beanClass)
			throws PersistenceException, RepositoryException,JcrMappingException {
        try {
			log.debug("ParentBeanConverter  - path : " +parentNode.getPath());
			Node grandParentNode = parentNode.getParent();
			if (grandParentNode.getPath().equals("/"))
			{
				return null;
			}
			return objectConverter.getObject(session, grandParentNode.getPath());
			
		} catch (javax.jcr.RepositoryException e) {
			throw new RepositoryException(e);
		} 
		
	}

	public void remove(Session session, Node parentNode,	BeanDescriptor descriptor)
	          throws PersistenceException,	RepositoryException, JcrMappingException {

	}

}
