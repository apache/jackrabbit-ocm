package org.apache.portals.graffito.jcr.reflection;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.portals.graffito.jcr.exception.JcrMappingException;
import org.apache.portals.graffito.jcr.persistence.collectionconverter.CollectionConverter;


/**
 * This class/interface 
 */
abstract public class ReflectionUtils {
    public static Object getNestedProperty(Object object, String fieldName) {
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
}
