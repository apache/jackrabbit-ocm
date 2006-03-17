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

import java.util.Date;


/**
 * CmsObject test
 * 
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Christophe Lombart</a>
 * 
 * 
 */
public class CmsObject
{
    public static final long serialVersionUID = 1;
    
    protected Long objectId;
        
    protected Long parentId;  //parent folder id
    protected Folder parentFolder;
    
    protected String name;
    
    protected String description;
    protected String title;
    protected Date creationDate;
    protected Date lastModified;
    
    /** 
     * Special attribute telling OJB the object's concrete type.
     *  
     */    
    protected String ojbConcreteClass; 
       
    /**
     * Constructor
     */
    public CmsObject()
    {
        ojbConcreteClass = this.getClass().getName();       
    }
    
    
        
    /**
     * @return Returns the parentFolder.
     */
    public Folder getParentFolder()
    {
        return parentFolder;
    }

    /**
     * @param parentFolder The parentFolder to set.
     */
    public void setParentFolder(Folder parentFolder)
    {
        this.parentFolder = parentFolder;
    }

    /**
     * @param parentId The parentId to set.
     */
    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    /**
     * @return Returns the parentId.
     */
    public Long getParentId()
    {
        return parentId;
    }
       
    
    /**
     * @return Returns the objectId.
     */
    public Long getObjectId()
    {
        return objectId;
    }

    /**
     * @param objectId The objectId to set.
     */
    public void setObjectId(Long objectId)
    {
        this.objectId = objectId;
    }
    

    /* 
     * @see org.apache.portals.graffito.model.CmsObject#getName()
     */
    public String getName()
    {
        return this.name;
    }

    /* 
     * @see org.apache.portals.graffito.model.CmsObject#setName(java.lang.String)
     */
    public void setName(String name)
    {
        this.name = name;
    }

    
    /* 
     * @see org.apache.portals.graffito.model.CmsObject#getCreationDate()
     */
    public Date getCreationDate()
    {
        return this.creationDate;
    }

    /* 
     * @see org.apache.portals.graffito.model.CmsObject#setCreationDate(java.util.Date)
     */
    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }

    /* 
     * @see org.apache.portals.graffito.model.CmsObject#getLastModified()
     */
    public Date getLastModified()
    {
        return this.lastModified;
    }

    /* 
     * @see org.apache.portals.graffito.model.CmsObject#setLastModified(java.util.Date)
     */
    public void setLastModified(Date lastModified)
    {
        this.lastModified = lastModified;
    }

    /* 
     * @see org.apache.portals.graffito.model.CmsObject#getDescription()
     */
    public String getDescription()
    {
        return this.description;
    }

    /* 
     * @see org.apache.portals.graffito.model.CmsObject#setDescription(java.lang.String)
     */
    public void setDescription(String v)
    {
        this.description = v;
    }

    /** 
     * @see org.apache.portals.graffito.model.CmsObject#getTitle()
     */
    public String getTitle()
    {
        return this.title;
    }

    /** 
     * @see org.apache.portals.graffito.model.CmsObject#setTitle(java.lang.String)
     */    
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    
 
    
    
}
