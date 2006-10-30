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

public abstract class AbstractBeanConverterImpl implements BeanConverter {

	protected  ObjectConverter objectConverter;
	
	public AbstractBeanConverterImpl(ObjectConverter objectConverter)
	{
		this.objectConverter = objectConverter;
	}
	private final static Log log = LogFactory.getLog(AbstractBeanConverterImpl.class);
	
    public abstract void insert(Session session, Node parentNode, BeanDescriptor descriptor, Object object)
			throws PersistenceException, RepositoryException, 	JcrMappingException;

	public abstract  void update(Session session, Node parentNode, 	BeanDescriptor descriptor, Object object)
			throws PersistenceException, RepositoryException,	JcrMappingException;
	
	public abstract Object getObject(Session session, Node parentNode,BeanDescriptor descriptor, Class beanClass)
			throws PersistenceException, RepositoryException,JcrMappingException ;

	public abstract void remove(Session session, Node parentNode,	BeanDescriptor descriptor)
	          throws PersistenceException,	RepositoryException, JcrMappingException ;
}
