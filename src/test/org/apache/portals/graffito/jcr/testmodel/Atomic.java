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

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

/**
 *
 * Simple object used to test atomic type
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Lombart Christophe </a>
 * @version $Id: Exp $
 */
public class Atomic
{
	private String path;
    private String string;
    private Boolean booleanObject;
    private boolean booleanPrimitive;
    private Integer integerObject;
    private int intPrimitive;
    private byte[] byteArray;
    private Calendar calendar;
    private Date date;
    private Double doubleObject;
    private double doublePrimitive;
    private InputStream inputStream;
    private Timestamp timestamp;
    private Collection multiValue;
    
    
    
    public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Boolean getBooleanObject()
    {
        return booleanObject;
    }
    public void setBooleanObject(Boolean booleanObject)
    {
        this.booleanObject = booleanObject;
    }
    public boolean isBooleanPrimitive()
    {
        return booleanPrimitive;
    }
    public void setBooleanPrimitive(boolean booleanPrimitive)
    {
        this.booleanPrimitive = booleanPrimitive;
    }
    public Integer getIntegerObject()
    {
        return integerObject;
    }
    public void setIntegerObject(Integer integerObject)
    {
        this.integerObject = integerObject;
    }
    public int getIntPrimitive()
    {
        return intPrimitive;
    }
    public void setIntPrimitive(int intPrimitive)
    {
        this.intPrimitive = intPrimitive;
    }
    public String getString()
    {
        return string;
    }
    public void setString(String string)
    {
        this.string = string;
    }
    public byte[] getByteArray()
    {
        return byteArray;
    }
    public void setByteArray(byte[] byteArray)
    {
        this.byteArray = byteArray;
    }
    public Calendar getCalendar()
    {
        return calendar;
    }
    public void setCalendar(Calendar calandar)
    {
        this.calendar = calandar;
    }
    public Date getDate()
    {
        return date;
    }
    public void setDate(Date date)
    {
        this.date = date;
    }
    public Double getDoubleObject()
    {
        return doubleObject;
    }
    public void setDoubleObject(Double doubleObject)
    {
        this.doubleObject = doubleObject;
    }
    public double getDoublePrimitive()
    {
        return doublePrimitive;
    }
    public void setDoublePrimitive(double doublePrimitive)
    {
        this.doublePrimitive = doublePrimitive;
    }
    public InputStream getInputStream()
    {
        return inputStream;
    }
    public void setInputStream(InputStream inputStream)
    {
        this.inputStream = inputStream;
    }
    public Timestamp getTimestamp()
    {
        return timestamp;
    }
    public void setTimestamp(Timestamp timestamp)
    {
        this.timestamp = timestamp;
    }
    
	public Collection getMultiValue()
	{
		return multiValue;
	}
	public void setMultiValue(Collection multiValue)
	{
		this.multiValue = multiValue;
	}
    
    
}
