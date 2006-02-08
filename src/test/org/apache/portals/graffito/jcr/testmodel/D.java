package org.apache.portals.graffito.jcr.testmodel;


/**
 * This class/interface
 */
public class D {
    private String path;
    private String d1;
    private B b1;

    /**
     * @return Returns the dB.
     */
    public B getB1() {
        return this.b1;
    }

    /**
     * @param db The dB to set.
     */
    public void setB1(B db) {
        this.b1 = db;
    }

    /**
     * @return Returns the dString.
     */
    public String getD1() {
        return this.d1;
    }

    /**
     * @param string The dString to set.
     */
    public void setD1(String string) {
        this.d1 = string;
    }

    /**
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path The path to set.
     */
    public void setPath(String path) {
        this.path= path;
    }
}
