/*
 * Copyright 2000-2004 The Apache Software Foundation.
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
package org.apache.portals.graffito.jcr.testmodel.inheritance;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CMS VersionnedDocument implementation.
 *
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Christophe Lombart</a>
 * 
 */
public class Document extends CmsObject
{
    protected final static Log log =  LogFactory.getLog(Document.class);
    
    protected long size;
    protected String contentType;   
        
    protected Content content;


    /** 
     * @see org.apache.portals.graffito.model.Document#getContentType()
     */
    public String getContentType()
    {
        return this.contentType;
    }

    /**
     * @see org.apache.portals.graffito.model.Document#setContentType(java.lang.String)
     */
    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }


    /**
     * 
     * @see org.apache.portals.graffito.model.Document#getSize()
     */
    public long getSize()
    {
        return size;
    }

    /**
     * 
     * @see org.apache.portals.graffito.model.Document#setSize(long)
     */
    public void setSize(long size)
    {
        this.size = size;
    }

    
    public Content getContent()
    {
        return content;
    }
    
    public void setContent(Content content)
    {
        this.content = content;
    }
}

