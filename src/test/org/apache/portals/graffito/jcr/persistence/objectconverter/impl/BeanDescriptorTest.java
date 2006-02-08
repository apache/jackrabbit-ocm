package org.apache.portals.graffito.jcr.persistence.objectconverter.impl;


import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.portals.graffito.jcr.RepositoryLifecycleTestSetup;
import org.apache.portals.graffito.jcr.TestBase;
import org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter;
import org.apache.portals.graffito.jcr.testmodel.B;
import org.apache.portals.graffito.jcr.testmodel.D;
import org.apache.portals.graffito.jcr.testmodel.DFull;

/**
 * This class/interface
 */
public class BeanDescriptorTest extends TestBase {
    private ObjectConverter objectConverter;
    
    public BeanDescriptorTest(String testname) {
        super(testname);
    }

    public static Test suite() {

        // All methods starting with "test" will be executed in the test suite.
        return new RepositoryLifecycleTestSetup(new TestSuite(BeanDescriptorTest.class));
    }
    
    
    /**
     * @see org.apache.portals.graffito.jcr.TestBase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.objectConverter = new ObjectConverterImpl(this.mapper, this.converterProvider);

        clean();
    }

    
    /**
     * @see org.apache.portals.graffito.jcr.TestBase#tearDown()
     */
    public void tearDown() throws Exception {
        clean();
        super.tearDown();
    }

    private void clean() throws Exception {
        if(getSession().itemExists("/someD")) {
            getSession().getItem("/someD").remove();
            getSession().save();
        }
    }
    
    public void testInlined() throws Exception {
        System.out.println(".inlined");
        
        B expB = new B();
        expB.setB1("b1value");
        expB.setB2("b2value");
        D expD = new D();
        expD.setPath("/someD");
        expD.setD1("d1value");
        expD.setB1(expB);
        
        this.objectConverter.insert(getSession(), expD);
        getSession().save();
        
        D actD = (D) this.objectConverter.getObject(getSession(), D.class, "/someD");
        
        assertEquals(expD.getD1(), actD.getD1());
        assertEquals(expB.getB1(), actD.getB1().getB1());
        assertNull("B.b2 is protected", actD.getB1().getB2());
        
        DFull actDFull = (DFull) this.objectConverter.getObject(getSession(), DFull.class, "/someD");
        
        assertEquals(expD.getD1(), actDFull.getD1());
        assertEquals(expB.getB1(), actDFull.getB1());
        assertNull("B.b2 is protected", actDFull.getB2());
        
        expB.setB1("updatedvalue1");
        
        this.objectConverter.update(getSession(), expD);
        getSession().save();
        
        actD = (D) this.objectConverter.getObject(getSession(), D.class, "/someD");
        
        assertEquals(expD.getD1(), actD.getD1());
        assertEquals(expB.getB1(), actD.getB1().getB1());
        assertNull("B.b2 is protected", actD.getB1().getB2());
        
        actDFull = (DFull) this.objectConverter.getObject(getSession(), DFull.class, "/someD");
        
        assertEquals(expD.getD1(), actDFull.getD1());
        assertEquals(expB.getB1(), actDFull.getB1());
        assertNull("B.b2 is protected", actDFull.getB2());
        
        expD.setB1(null);
        this.objectConverter.update(getSession(), expD);
        getSession().save();
        
        actD = (D) this.objectConverter.getObject(getSession(), D.class, "/someD");
        
        assertEquals(expD.getD1(), actD.getD1());
        assertNull("b1 was removed", actD.getB1());
        
        actDFull = (DFull) this.objectConverter.getObject(getSession(), DFull.class, "/someD");
        
        assertEquals(expD.getD1(), actDFull.getD1());
        assertNull("b1 was removed", actDFull.getB1());
        assertNull("B.b2 is protected", actDFull.getB2());

        getSession().getItem("/someD").remove();
        getSession().save();
    }
}
