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

package org.apache.portals.graffito.jcr.exception;


/**
 * Occurs when the persistence manager try to manage an object which is not based on a persistent class.
 *
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Christophe Lombart</a>
 */
public class IncorrectPersistentClassException extends JcrMappingException {
   
    
    public IncorrectPersistentClassException(String message, Throwable nested) {
        super(message, nested);
    }

    public IncorrectPersistentClassException(String message) {
        super(message);
    }

    public IncorrectPersistentClassException(Throwable nested) {
        super(nested);
    }

}
