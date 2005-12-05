/*
 * Copyright 2004-2005 The Apache Software Foundation or its licensors,
 *                     as applicable.
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
package org.apache.portals.graffito.jcr.query;

import org.apache.portals.graffito.jcr.exception.IncorrectAtomicTypeException;


/**
 * 
 * Graffito JCR Filter interface.
 * 
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Christophe Lombart</a>
 *
 */
public interface Filter
{

	
    /**
     * Set the filter scope. The scope is an Node path specifying where to search in the content tree.
     * For example, 
     * /mynode/mysecondnode/', the search engine will search on child objects in the /mynode/mysecondnode
     * /mynode/mysecondnode//', the search engine will search on desncendant objects in the /mynode/mysecondnode (the complete subnode tree)
     * 
     * @param scope The filter scope
     *  
     */
    public void setScope(String scope);
        
    
    /**
     * Get the filter scope.
     * 
     * @return The filter scope
     */
    public String getScope();
    
    
    /**
     * Search content based on a fullTextSearch. 
     * Depending on the full text search engine, you can also filter on properties.
     * 
     * @param scope either a a jcr node or propserty. If a node is used, all properties of this node are searche (following the internal index
     * @param fullTextSearch The full text search string  
     */
    public void addContains(String scope, String fullTextSearch) throws IncorrectAtomicTypeException;   
        
	public void addBetween(String arg0, Object arg1, Object arg2) throws IncorrectAtomicTypeException;

	public void addEqualTo(String arg0, Object arg1) throws IncorrectAtomicTypeException;

	public void addGreaterOrEqualThan(String arg0, Object arg1) throws IncorrectAtomicTypeException;

	public void addGreaterThan(String arg0, Object arg1) throws IncorrectAtomicTypeException;

	public void addLessOrEqualThan(String arg0, Object arg1) throws IncorrectAtomicTypeException;
	
	public void addLessThan(String arg0, Object arg1) throws IncorrectAtomicTypeException;

	public void addLike(String arg0, Object arg1) throws IncorrectAtomicTypeException;

	public void addNotEqualTo(String arg0, Object arg1) throws IncorrectAtomicTypeException;

	public void addNotNull(String arg0) throws IncorrectAtomicTypeException;

	public void addIsNull(String arg0) throws IncorrectAtomicTypeException;
	
	public void addOrFilter(Filter arg0) throws IncorrectAtomicTypeException;
	
	public void addJCRExpression(String jcrExpression) throws IncorrectAtomicTypeException;
	
    public Class getFilterClass();	
	

}
