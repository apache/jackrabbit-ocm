package org.apache.portals.graffito.jcr.testmodel;

import java.io.InputStream;
import java.util.Calendar;

public class File
{

    private String mimeType;
    private String encoding;
    private InputStream data;
    private Calendar lastModified;
    
    public InputStream getData()
    {
        return data;
    }
    public void setData(InputStream data)
    {
        this.data = data;
    }
    public String getEncoding()
    {
        return encoding;
    }
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }
    public Calendar getLastModified()
    {
        return lastModified;
    }
    public void setLastModified(Calendar lastModified)
    {
        this.lastModified = lastModified;
    }
    public String getMimeType()
    {
        return mimeType;
    }
    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }
    
    
}
