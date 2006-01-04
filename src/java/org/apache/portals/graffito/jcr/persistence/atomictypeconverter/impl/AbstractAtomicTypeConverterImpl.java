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
package org.apache.portals.graffito.jcr.persistence.atomictypeconverter.impl;

import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.AtomicTypeConverter;

/**
 * Abstract implementation for {@link org.apache.portals.graffito.jcr.persistence.atomictypeconverter.AtomicTypeConverter}
 *  
 * @author <a href="mailto:christophe.lombart@gmail.com">Christophe Lombart</a>
 *
 */
public abstract class AbstractAtomicTypeConverterImpl implements AtomicTypeConverter
{

	private ValueFactory valueFactory;

	 /**
	  * No-arg constructor.
	  * When using it you should provide later the <code>javax.jcr.ValueFactory</code>.
	  * 
	  * @see #setValueFactory(ValueFactory)
	  */
	  public AbstractAtomicTypeConverterImpl()
	 {
	 }	
	  
	/**
	 * Constructor
	 * @param factory The JCR Value factory to used
	 */
	public AbstractAtomicTypeConverterImpl(ValueFactory factory)
	{
		super();
		valueFactory = factory;
	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.atomictypeconverter.AtomicTypeConverter#getValue(java.lang.Object)
	 */
	public abstract Value getValue(Object propValue);


    /**
     * 
     * @see org.apache.portals.graffito.jcr.persistence.atomictypeconverter.AtomicTypeConverter#getObject(javax.jcr.Value)
     */
	public abstract Object getObject(Value value);

	/**
	 * Set the JCR value factory
	 * 
	 * @param valueFactory The value factory to set
	 */
	public void setValueFactory(ValueFactory valueFactory)
	{
		this.valueFactory = valueFactory;
	}

	/**	
	 * @return the JCR value factory 
	 */
	public ValueFactory getValueFactory()
	{
		return this.valueFactory;
	}
}
