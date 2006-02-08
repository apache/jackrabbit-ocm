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
import org.apache.portals.graffito.jcr.exception.InitMapperException;
import org.apache.portals.graffito.jcr.mapper.Mapper;
import org.apache.portals.graffito.jcr.mapper.model.ClassDescriptor;
import org.apache.portals.graffito.jcr.mapper.model.MappingDescriptor;

/**
 *
 * Digester implementation for {@link org.apache.portals.graffito.jcr.mapper.Mapper}
 *
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Lombart Christophe </a>
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class DigesterMapperImpl implements Mapper {
    private static final Log log = LogFactory.getLog(DigesterMapperImpl.class);

    private MappingDescriptor mappingDescriptor;

    private String[] mappingFiles;
    private InputStream[] mappingStreams;
    private DigesterDescriptorReader descriptorReader;

    /**
     * No-arg constructor.
     */
    public DigesterMapperImpl() {
    }

    /**
     * Constructor
     *
     * @param xmlFile The xml mapping file to read
     *
     */
    public DigesterMapperImpl(String xmlFile) {
        this.mappingFiles = new String[] { xmlFile };
    }

    /**
     * Constructor
     *
     * @param files a set of xml mapping files to read
     *
     */
    public DigesterMapperImpl(String[] files) {
        this.mappingFiles = files;
    }

    /**
     * Constructor
     *
     * @param stream The xml mapping file to read
     */
    public DigesterMapperImpl(InputStream stream) {
        this.mappingStreams = new InputStream[] { stream };
    }

    /**
     * Constructor
     *
     * @param streams a set of mapping files to read
     *
     */
    public DigesterMapperImpl(InputStream[] streams) {
        this.mappingStreams = streams;
    }

    /**
     * Set a mapping file.
     * 
     * @param file path to mapping file
     */
    public void setMappingFile(String file) {
        setMappingFiles(new String[] { file });
    }

    /**
     * 
     * @param files
     */
    public void setMappingFiles(String[] files) {
        this.mappingFiles = files;
    }

    public void setMappingStream(InputStream stream) {
        setMappingStreams(new InputStream[] { stream });
    }

    public void setMappingStreams(InputStream[] streams) {
        this.mappingStreams = streams;
    }

    public void setDescriptorReader(DigesterDescriptorReader reader) {
        this.descriptorReader = reader;
    }

    public Mapper buildMapper() {
        if (this.descriptorReader == null) {
            this.descriptorReader = new DigesterDescriptorReader();
        }
        if (this.mappingFiles != null && this.mappingFiles.length > 0) {
            log.info("Read the xml mapping file : " +  this.mappingFiles[0]);
            this.mappingDescriptor = this.descriptorReader.loadClassDescriptors(this.mappingFiles[0]);
            this.mappingDescriptor.setMapper(this);

            for (int i = 1; i < this.mappingFiles.length; i++) {
                log.info("Read the xml mapping file : " +  this.mappingFiles[i]);
                MappingDescriptor anotherMappingDescriptor = this.descriptorReader.loadClassDescriptors(this.mappingFiles[i]);
                this.mappingDescriptor.getClassDescriptors().putAll(anotherMappingDescriptor.getClassDescriptors());
            }
        }
        else if (this.mappingStreams != null && this.mappingStreams.length > 0) {
            log.info("Read the stream mapping file : " +  this.mappingStreams[0].toString());
            this.mappingDescriptor = this.descriptorReader.loadClassDescriptors(this.mappingStreams[0]);
            this.mappingDescriptor.setMapper(this);

            for (int i = 1; i < this.mappingStreams.length; i++) {
                log.info("Read the stream mapping file : " +  this.mappingStreams[i].toString());
                MappingDescriptor anotherMappingDescriptor = this.descriptorReader.loadClassDescriptors(this.mappingStreams[i]);
                this.mappingDescriptor.getClassDescriptors().putAll(anotherMappingDescriptor.getClassDescriptors());
            }
        }
        else {
            throw new InitMapperException("No mappings were provided");
        }
        
        return this;
    }

    /**
     *
     * @see org.apache.portals.graffito.jcr.mapper.Mapper#getClassDescriptor(java.lang.Class)
     */
    public ClassDescriptor getClassDescriptor(Class clazz) {
        return mappingDescriptor.getClassDescriptor(clazz.getName());
    }
}
