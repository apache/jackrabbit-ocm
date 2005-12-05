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

package org.apache.portals.graffito.jcr.query.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.portals.graffito.jcr.mapper.model.ClassDescriptor;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.AtomicTypeConverter;
import org.apache.portals.graffito.jcr.query.Filter;

/**
 * {@link org.apache.portals.graffito.jcr.query.Filter}
 * 
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Christophe Lombart</a>
 *
 */
public class FilterImpl implements Filter
{

	private Class claszz;
	private String scope = "";
	private ArrayList jcrExpressions = new ArrayList();
	
	
	private ClassDescriptor classDescriptor;
	private Map atomicTypeConverters;
	
	private String orJcrExpression; 
		
	private final static Log log = LogFactory.getLog(FilterImpl.class);
	
	/**
	 * Construtor
	 * 
	 * @param classDescriptor
	 * @param atomicTypeConverters
	 * @param clazz
	 */
	public FilterImpl(ClassDescriptor classDescriptor, Map atomicTypeConverters, Class clazz) 
	{		
		this.claszz = clazz;
		this.atomicTypeConverters = atomicTypeConverters;
		this.classDescriptor = classDescriptor;
	}
	
	
	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.query.Filter#getFilterClass()
	 */
	public Class getFilterClass()
	{		
		return claszz;
	}



	/**
	 * @see org.apache.portals.graffito.jcr.query.Filter#setScope(java.lang.String)
	 */
	public void setScope(String scope)
	{
		this.scope = scope;
	}

	/**
	 * @see org.apache.portals.graffito.jcr.query.Filter#getScope()
	 */
	public String getScope()
	{

		return this.scope;
	}

	/**
	 * @see org.apache.portals.graffito.jcr.query.Filter#addContains(java.lang.String)
	 */
	public void addContains(String scope, String fullTextSearch) 
	{
		String jcrExpression = null;
		if (scope.equals("."))
		{
		     jcrExpression = "jcr:contains(., '" + fullTextSearch + "')";
		}
		else
		{
			jcrExpression = "jcr:contains(@" + this.getJcrFieldName(scope) + ", '" + fullTextSearch + "')";
		}
		
		jcrExpressions.add(jcrExpression);
	}

	/**
	 * @see org.apache.portals.graffito.jcr.query.Filter#addBetween(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	public void addBetween(String fieldAttributeName, Object value1, Object value2) 
	{
		String jcrExpression =  "( @" + this.getJcrFieldName(fieldAttributeName) + " >= " + this.getStringValue(value1) + 
		                        " and @" + this.getJcrFieldName(fieldAttributeName) + " <= " + this.getStringValue(value2) +  ")";
		jcrExpressions.add(jcrExpression) ;		

	}

	/**
	 * @see org.apache.portals.graffito.jcr.query.Filter#addEqualTo(java.lang.String, java.lang.Object)
	 */
	public void addEqualTo(String fieldAttributeName, Object value)
	{
		String jcrExpression =  "@" + this.getJcrFieldName(fieldAttributeName) + " = " + this.getStringValue(value);
		jcrExpressions.add(jcrExpression);		
	}

	/**
	 * @see org.apache.portals.graffito.jcr.query.Filter#addGreaterOrEqualThan(java.lang.String, java.lang.Object)
	 */
	public void addGreaterOrEqualThan(String fieldAttributeName, Object value) 
	{
	
		String jcrExpression =  "@" + this.getJcrFieldName(fieldAttributeName) + " >= " + this.getStringValue(value);
		jcrExpressions.add(jcrExpression);	
		
	}

	/**
	 * @see org.apache.portals.graffito.jcr.query.Filter#addGreaterThan(java.lang.String, java.lang.Object)
	 */
	public void addGreaterThan(String fieldAttributeName, Object value)
	{
		String jcrExpression =  "@" + this.getJcrFieldName(fieldAttributeName) + " > " + this.getStringValue(value);
		jcrExpressions.add(jcrExpression);	
		
	}


	/**
	 * @see org.apache.portals.graffito.jcr.query.Filter#addLessOrEqualThan(java.lang.String, java.lang.Object)
	 */
	public void addLessOrEqualThan(String fieldAttributeName, Object value)
	{
		String jcrExpression =  "@" + this.getJcrFieldName(fieldAttributeName) + " <= " + this.getStringValue(value);
		jcrExpressions.add(jcrExpression);	
		
	}
	
	/**
	 * @see org.apache.portals.graffito.jcr.query.Filter#addLessOrEqualThan(java.lang.String, java.lang.Object)
	 */
	public void addLessThan(String fieldAttributeName, Object value)
	{
		String jcrExpression =  "@" + this.getJcrFieldName(fieldAttributeName) + " < " + this.getStringValue(value);
		jcrExpressions.add(jcrExpression);	
		
	}	

	/**
	 * @see org.apache.portals.graffito.jcr.query.Filter#addLike(java.lang.Object, java.lang.Object)
	 */
	public void addLike(String fieldAttributeName, Object value)
	{
		String jcrExpression = "jcr:like(" + "@" + this.getJcrFieldName(fieldAttributeName) + ", '" + value + "')";
		jcrExpressions.add(jcrExpression);
	}


	/**
	 * @see org.apache.portals.graffito.jcr.query.Filter#addNotEqualTo(java.lang.String, java.lang.Object)
	 */
	public void addNotEqualTo(String fieldAttributeName, Object value) 
	{
		String jcrExpression =  "@" + this.getJcrFieldName(fieldAttributeName) + " != " + this.getStringValue(value);
		jcrExpressions.add(jcrExpression);			
	}


	/**
	 * @see org.apache.portals.graffito.jcr.query.Filter#addNotNull(java.lang.String)
	 */
	public void addNotNull(String fieldAttributeName) 
	{
		String jcrExpression =  "@" + this.getJcrFieldName(fieldAttributeName);
		jcrExpressions.add(jcrExpression);			
		
	}

	/**
	 * @see org.apache.portals.graffito.jcr.query.Filter#addIsNull(java.lang.String)
	 */
	public void addIsNull(String fieldAttributeName)
	{
		String jcrExpression =  "not(@" + this.getJcrFieldName(fieldAttributeName)+")";
		jcrExpressions.add(jcrExpression);			
		
	}

	/**
	 * @see org.apache.portals.graffito.jcr.query.Filter#addOrFilter(org.apache.portals.graffito.jcr.query.Filter)
	 */
	public void addOrFilter(Filter filter)
	{
		
		orJcrExpression  = ((FilterImpl)filter).getJcrExpression();
	
	}

	public void addJCRExpression(String jcrExpression)
	{
		jcrExpressions.add(jcrExpression);
	}
	
	
	private String getJcrFieldName(String fieldAttribute) 
	{
		String jcrFieldName =  classDescriptor.getJcrName(fieldAttribute);
		if (jcrFieldName == null)
		{
			log.error("Impossible to find the jcrFieldName for the attribute :" + fieldAttribute);
		}
		return jcrFieldName;
	    	
	}
	
	private String getStringValue (Object value) 
	{
		AtomicTypeConverter atomicTypeConverter = (AtomicTypeConverter) atomicTypeConverters.get(value.getClass());
		return atomicTypeConverter.getStringValue(value);
	}
	
	public String  getJcrExpression() 
	{
		if (orJcrExpression == null || orJcrExpression.equals(""))
		{
			return buildJcrExpression();	
		}
		else
		{
			return "(" + buildJcrExpression() + ") or (" +  this.orJcrExpression + ")";
		}
		
	}
	
	private String buildJcrExpression() 
	{
		   int count = 1;
		   String jcrExp = "";
		   
		    
		   Iterator criteriaIterator =  jcrExpressions.iterator();
		   while (criteriaIterator.hasNext())
		   {
			   if (count > 1)
			   {
				   jcrExp += " and ";
			   }
			   jcrExp += (String) criteriaIterator.next();
			   count++;
			   
		   }
		    
		   
		   return jcrExp;
		
	}
	
	

}
