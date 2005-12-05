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
package org.apache.portals.graffito.jcr.testmodel;

import java.util.List;




/**
 * CMS Folder Test
 *
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Christophe Lombart</a>
 * @version $Id: Folder.java,v 1.1 2004/12/22 20:36:59 christophe Exp $
 */
public class Folder extends CmsObject 
{

    protected List folders;
    protected List documents;
    
    /**
     * @see org.apache.portals.graffito.model.Folder#getDocuments()
     */
    public List getDocuments()
    {
        return documents;
    }

    /**
     * @see org.apache.portals.graffito.model.Folder#getFolders()
     */
    public List getFolders()
    {
        return folders;
    }
    
}

