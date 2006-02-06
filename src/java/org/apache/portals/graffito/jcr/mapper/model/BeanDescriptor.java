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
package org.apache.portals.graffito.jcr.mapper.model;


import org.apache.portals.graffito.jcr.persistence.objectconverter.impl.ObjectConverterImpl;

/**
 * BeanDescriptor is used by the mapper to read general information on a bean field
 *
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Lombart Christophe </a>
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
 */
public class BeanDescriptor {
    private static final String DEFAULT_CONVERTER = ObjectConverterImpl.class.getName();

    private String fieldName;
    private String jcrName;
    private boolean proxy;
    private boolean inline;
    private String converter = DEFAULT_CONVERTER;
    private String jcrNodeType;
    private boolean jcrAutoCreated;
    private boolean jcrMandatory;
    private String jcrOnParentVersion;
    private boolean jcrProtected;
    private boolean jcrSameNameSiblings;

    /**
     * @return Returns the fieldName.
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @param fieldName The fieldName to set.
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * @return Returns the jcrName.
     */
    public String getJcrName() {
        return jcrName;
    }

    /**
     * @param jcrName The jcrName to set.
     */
    public void setJcrName(String jcrName) {
        this.jcrName = jcrName;
    }

    /**
     * @return Returns the proxy.
     */
    public boolean isProxy() {
        return proxy;
    }

    /**
     * @param proxy The proxy to set.
     */
    public void setProxy(boolean proxy) {
        this.proxy = proxy;
    }

    public boolean isInline() {
        return this.inline;
    }

    public void setInline(boolean flag) {
        this.inline = flag;
    }

    public String getConverter() {
        return this.converter;
    }

    public void setConverter(String converterClass) {
        this.converter = converterClass;
    }

    /** Getter for property jcrNodeType.
     *
     * @return jcrNodeType
     */
    public String getJcrNodeType() {
        return jcrNodeType;
    }

    /** Setter for property jcrNodeType.
     *
     * @param value jcrNodeType
     */
    public void setJcrNodeType(String value) {
        this.jcrNodeType = value;
    }

    /** Getter for property jcrAutoCreated.
     *
     * @return jcrAutoCreated
     */
    public boolean isJcrAutoCreated() {
        return jcrAutoCreated;
    }

    /** Setter for property jcrAutoCreated.
     *
     * @param value jcrAutoCreated
     */
    public void setJcrAutoCreated(boolean value) {
        this.jcrAutoCreated = value;
    }

    /** Getter for property jcrMandatory.
     *
     * @return jcrMandatory
     */
    public boolean isJcrMandatory() {
        return jcrMandatory;
    }

    /** Setter for property jcrMandatory.
     *
     * @param value jcrMandatory
     */
    public void setJcrMandatory(boolean value) {
        this.jcrMandatory = value;
    }

    /** Getter for property jcrOnParentVersion.
     *
     * @return jcrOnParentVersion
     */
    public String getJcrOnParentVersion() {
        return jcrOnParentVersion;
    }

    /** Setter for property jcrOnParentVersion.
     *
     * @param value jcrOnParentVersion
     */
    public void setJcrOnParentVersion(String value) {
        this.jcrOnParentVersion = value;
    }

    /** Getter for property jcrProtected.
     *
     * @return jcrProtected
     */
    public boolean isJcrProtected() {
        return jcrProtected;
    }

    /** Setter for property jcrProtected.
     *
     * @param value jcrProtected
     */
    public void setJcrProtected(boolean value) {
        this.jcrProtected = value;
    }

    /** Getter for property jcrSameNameSiblings.
     *
     * @return jcrSameNameSiblings
     */
    public boolean isJcrSameNameSiblings() {
        return jcrSameNameSiblings;
    }

    /** Setter for property jcrSameNameSiblings.
     *
     * @param value jcrSameNameSiblings
     */
    public void setJcrSameNameSiblings(boolean value) {
        this.jcrSameNameSiblings = value;
    }
}
