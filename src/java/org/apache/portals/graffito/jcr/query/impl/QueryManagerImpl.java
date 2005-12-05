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

import java.util.Map;

import org.apache.portals.graffito.jcr.mapper.Mapper;
import org.apache.portals.graffito.jcr.mapper.model.ClassDescriptor;
import org.apache.portals.graffito.jcr.query.Filter;
import org.apache.portals.graffito.jcr.query.Query;
import org.apache.portals.graffito.jcr.query.QueryManager;

public class QueryManagerImpl implements QueryManager
{

	private Mapper mapper;
	private Map atomicTypeConverters;
	public QueryManagerImpl(Mapper mapper,  Map atomicTypeConverters)
	{
		this.mapper = mapper;
		this.atomicTypeConverters = atomicTypeConverters;
	}
	
	public Filter createFilter(Class classQuery) 
	{

		return new FilterImpl(mapper.getClassDescriptor(classQuery), atomicTypeConverters, classQuery);
	}

	public Query createQuery(Filter filter)
	{

		return new QueryImpl(filter, mapper);
	}

	public String buildJCRExpression(Query query)
	{

		Filter filter = query.getFilter();
					
		String jcrExp = "";
		
		// Add scope
		if ((filter.getScope() != null && ( ! filter.getScope().equals(""))))
		{
			jcrExp +=  "/jcr:root" + filter.getScope() + "element(*, ";
		}
		else
		{
			jcrExp +=  "//element(*, ";
		}
		
		// Add node type
		jcrExp +=  this.getNodeType(filter) + ") ";

        // Add filter criteria
		String filterExp = ((FilterImpl)filter).getJcrExpression();
		if ((filterExp != null) && ( ! filterExp.equals("")))
		{
		    jcrExp += "[" + filterExp + "]";
		}
		
		// Add order by
		jcrExp += ((QueryImpl)query).getOrderByExpression();
		
		return jcrExp;
		 
	}
	
	private String getNodeType(Filter filter)
	{
		ClassDescriptor classDescriptor = mapper.getClassDescriptor(filter.getFilterClass());
		return classDescriptor.getJcrNodeType();
		
	}

}
