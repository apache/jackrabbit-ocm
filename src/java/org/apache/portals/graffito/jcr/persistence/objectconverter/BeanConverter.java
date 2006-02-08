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
package org.apache.portals.graffito.jcr.persistence.objectconverter;


import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.portals.graffito.jcr.exception.PersistenceException;
import org.apache.portals.graffito.jcr.mapper.Mapper;

/**
 * Convert any kind of beans into JCR nodes & properties
 *
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public interface BeanConverter {
    /**
     * Insert the object
     *
     * @param session the JCR session
     * @param parentNode The parent node used to store the new JCR element (object)
     * @param nodeName The node name used to store the object
     * @param object the object to insert
     * @throws PersistenceException when it is not possible to insert the object
     */
    public void insert(Session session, 
                       Node parentNode,
                       Mapper mapper,
                       String beanName,
                       Object object)
    throws PersistenceException;

    /**
     * Update the object
     *
     * @param session the JCR session
     * @param parentNode The parent node used to store the new JCR element (object)
     * @param nodeName The node name used to store the object
     * @param object the object to update
     * @throws PersistenceException when it is not possible to update the object
     */
    public void update(Session session, 
                       Node parentNode, 
                       Mapper mapper, 
                       String beanName, 
                       Object object)
    throws PersistenceException;
    
    /**
     * ???
     */
    public Object getObject(Session session,
                            Node parentNode,
                            Mapper mapper,
                            String beanName,
                            Class beanClass) 
    throws PersistenceException;


    /**
     * ???
     */
    void remove(Session session, 
                Node parentNode, 
                Mapper mapper, 
                String beanName)
    throws PersistenceException;
}
