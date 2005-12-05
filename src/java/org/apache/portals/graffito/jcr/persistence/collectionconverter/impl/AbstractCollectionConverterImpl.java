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

package org.apache.portals.graffito.jcr.persistence.collectionconverter.impl;

import java.util.Map;

import org.apache.portals.graffito.jcr.mapper.Mapper;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.CollectionConverter;
import org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter;

/** 
 * Abstract class used for all CollectionConverter
 * 
 * @author <a href="mailto:christophe.lombart@gmail.com">Christophe Lombart</a>
 * 
 */
public abstract class AbstractCollectionConverterImpl implements CollectionConverter
{
    protected Map atomicTypeConverters;
	protected ObjectConverter objectConverter;    
    protected Mapper mapper;
    
    /**
     * Constructor
     * 
     * @param atomicTypeConverters The atomic type converter to used
     * @param objectConverter The object converter to used
     * @param mapper The mapper to used
     */
    public AbstractCollectionConverterImpl(Map atomicTypeConverters, ObjectConverter objectConverter, Mapper mapper)
    {
    	this.atomicTypeConverters = atomicTypeConverters;
        this.objectConverter = objectConverter;
        this.mapper = mapper;
    }


}
