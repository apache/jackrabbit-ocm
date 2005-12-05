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
package org.apache.portals.graffito.jcr.mapper.impl;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.portals.graffito.jcr.mapper.Mapper;
import org.apache.portals.graffito.jcr.mapper.model.ClassDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.MappingDescriptor;

/**
 *
 * Digester implementation for {@link org.apache.portals.graffito.jcr.mapper.Mapper}
 * 
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Lombart Christophe </a>
 */
public class DigesterMapperImpl implements Mapper
{    
    private MappingDescriptor mappingDescriptor;
    private Log log = LogFactory.getLog(DigesterMapperImpl.class); 
    
    /**
     * Constructor
     *  
     * @param xmlFile The xml mapping file to read
     *
     */
    public DigesterMapperImpl(String xmlFile)
    {
        log.info("Read the xml mapping file : " +  xmlFile);
        mappingDescriptor = DigesterDescriptorReader.loadClassDescriptors(xmlFile);
    }
    
    /**
     * Constructor 
     * 
     * @param files a set of xml mapping files to read
     *
     */
    public DigesterMapperImpl(String[] files)
    {
    	log.info("Read the xml mapping file : " +  files[0]);
        mappingDescriptor = DigesterDescriptorReader.loadClassDescriptors(files[0]);
        for (int i=1; i<files.length;i++)
        {
        	log.info("Read the xml mapping file : " +  files[i]);
            MappingDescriptor anotherMappingDescriptor = DigesterDescriptorReader.loadClassDescriptors(files[i]);
            mappingDescriptor.getClassDescriptors().putAll(anotherMappingDescriptor.getClassDescriptors());
        }
    }
    
    /**
     * Constructor
     *  
     * @param stream The xml mapping file to read
     */
    public DigesterMapperImpl(InputStream stream)
    {
        log.info("Read the input stream : " +  stream.toString());
        mappingDescriptor = DigesterDescriptorReader.loadClassDescriptors(stream);
    }

    /**
     * Constructor 
     * 
     * @param streams a set of mapping files to read
     *
     */
    public DigesterMapperImpl(InputStream[] streams)
    {
    	log.info("Read the input stream : " +  streams[0].toString());
        mappingDescriptor = DigesterDescriptorReader.loadClassDescriptors(streams[0]);
        for (int i=1; i<streams.length;i++)
        {
        	log.info("Read the input stream : " +  streams[i].toString());
            MappingDescriptor anotherMappingDescriptor = DigesterDescriptorReader.loadClassDescriptors(streams[i]);
            mappingDescriptor.getClassDescriptors().putAll(anotherMappingDescriptor.getClassDescriptors());
        }
    }

    /**
     * 
     * @see org.apache.portals.graffito.jcr.mapper.Mapper#getClassDescriptor(java.lang.Class)
     */
    public ClassDescriptor getClassDescriptor(Class clazz) 
    {        
        return mappingDescriptor.getClassDescriptor(clazz.getName());
    }
}
