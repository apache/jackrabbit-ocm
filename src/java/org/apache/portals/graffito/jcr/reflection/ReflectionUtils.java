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
package org.apache.portals.graffito.jcr.reflection;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.portals.graffito.jcr.exception.JcrMappingException;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.CollectionConverter;


/**
 * Utility class for handling reflection using BeanUtils.
 * 
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
abstract public class ReflectionUtils {
    public static Object getNestedProperty(Object object, String fieldName) {
        if (null == object) {
            return null;
        }
        
        try {
            return PropertyUtils.getNestedProperty(object, fieldName);
        }
        catch(IllegalAccessException e) {
            throw new JcrMappingException("Cannot access property "
                    + fieldName,
                    e);
        }
        catch(InvocationTargetException e) {
            throw new JcrMappingException("Cannot access property "
                    + fieldName,
                    e);
        }
        catch(NoSuchMethodException e) {
            throw new JcrMappingException("Cannot access property "
                    + fieldName,
                    e);
        }
    }
    
    public static Class getPropertyType(Object object, String fieldName) {
        try {
            return PropertyUtils.getPropertyType(object, fieldName);
        }
        catch(Exception ex) {
            throw new JcrMappingException("Cannot access property "
                    + fieldName,
                    ex);
        }
    }

    public static Object newInstance(Class clazz) {
        try {
            return clazz.newInstance();
        }
        catch(Exception ex) {
            throw new JcrMappingException("Cannot create instance for class "
                    + clazz,
                    ex);
        }
    }
    
    /**
     * @param className
     * @param objects
     * @return
     */
    public static CollectionConverter invokeConstructor(String className, 
                                                        Object[] params) {
        try {
            Class converterClass= Class.forName(className);
    
            return (CollectionConverter) ConstructorUtils.invokeConstructor(converterClass, params);
        }
        catch(Exception ex) {
            throw new JcrMappingException("Cannot create instance for class "
                    + className,
                    ex);
        }
    }

    /**
     * @param object
     * @param fieldName
     * @param path
     */
    public static void setNestedProperty(Object object, String fieldName, Object value) {
        try {
            PropertyUtils.setNestedProperty(object, fieldName, value);
        }
        catch(Exception ex) {
            throw new JcrMappingException("Cannot set the field " + fieldName,
                    ex);
        }
    }

    /**
     * @param string
     * @return
     */
    public static Object newInstance(String clazz) {
        try {
            return Class.forName(clazz).newInstance();
        }
        catch(Exception ex) {
            throw new JcrMappingException("Cannot create instance for class "
                    + clazz,
                    ex);
        }
    }
}
