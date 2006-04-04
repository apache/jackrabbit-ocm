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

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter;

public class ProxyManager {

	 private final static Log log = LogFactory.getLog(ProxyManager.class);
	
	 private ObjectConverter objectConverter;
	 
	 public ProxyManager(ObjectConverter objectConverter)
	 {
		 	this.objectConverter = objectConverter;
	 }
	 
	public  Object createBeanProxy(Session session, Class beanClass, String path) 
	{
		
       try {
			if (!session.itemExists(path)) {
				return null;
			}
		} catch (RepositoryException e) {
			throw new org.apache.portals.graffito.jcr.exception.RepositoryException(
					"Impossible to check,if the object exits on " + path, e);
		}
		
		log.debug("Create proxy for " + path);
		LazyLoader loader = new BeanLazyLoader(this.objectConverter, session, beanClass, path) ;		
		return  Enhancer.create(beanClass, loader);
	}

}
