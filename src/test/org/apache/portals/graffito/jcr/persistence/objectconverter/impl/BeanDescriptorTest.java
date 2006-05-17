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
package org.apache.portals.graffito.jcr.persistence.objectconverter.impl;



import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.portals.graffito.jcr.RepositoryLifecycleTestSetup;
import org.apache.portals.graffito.jcr.TestBase;
import org.apache.portals.graffito.jcr.exception.PersistenceException;
import org.apache.portals.graffito.jcr.mapper.Mapper;
import org.apache.portals.graffito.jcr.mapper.model.BeanDescriptor;
import org.apache.portals.graffito.jcr.persistence.objectconverter.BeanConverter;
import org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter;
import org.apache.portals.graffito.jcr.testmodel.B;
import org.apache.portals.graffito.jcr.testmodel.D;
import org.apache.portals.graffito.jcr.testmodel.DFull;
import org.apache.portals.graffito.jcr.testmodel.E;

/**
 * ObjectConverter test for bean-descriptor with inner bean inlined and inner bean with
 * custom converter.
 * 
 * @author <a href='mailto:the_mindstorm[at]evolva[dot]ro'>Alexandru Popescu</a>
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
        System.out.println("inlined");
        
        B expB = new B();
        expB.setB1("b1value");
        expB.setB2("b2value");
        D expD = new D();
        expD.setPath("/someD");
        expD.setD1("d1value");
        expD.setB1(expB);
        
        this.objectConverter.insert(getSession(), expD);
        getSession().save();
        
        D actD = (D) this.objectConverter.getObject(getSession(), "/someD");
        
        assertEquals(expD.getD1(), actD.getD1());
        assertEquals(expB.getB1(), actD.getB1().getB1());
        assertEquals(expB.getB2(), actD.getB1().getB2());
        
        DFull actDFull = (DFull) this.objectConverter.getObject(getSession(), DFull.class,  "/someD");
        
        assertEquals(expD.getD1(), actDFull.getD1());
        assertEquals(expB.getB1(), actDFull.getB1());
        assertEquals(expB.getB2(), actDFull.getB2());
        
        expB.setB1("updatedvalue1");
        
        this.objectConverter.update(getSession(), expD);
        getSession().save();
        
        actD = (D) this.objectConverter.getObject(getSession(), "/someD");
        
        assertEquals(expD.getD1(), actD.getD1());
        assertEquals(expB.getB1(), actD.getB1().getB1());
        assertEquals(expB.getB2(), actD.getB1().getB2());
        
        actDFull = (DFull) this.objectConverter.getObject(getSession(), DFull.class,  "/someD");
        
        assertEquals(expD.getD1(), actDFull.getD1());
        assertEquals(expB.getB1(), actDFull.getB1());
        assertEquals(expB.getB2(), actDFull.getB2());
        
            
        expD.setB1(null);
        this.objectConverter.update(getSession(), expD);
        getSession().save();
        
        actD = (D) this.objectConverter.getObject(getSession(),  "/someD");
        
        assertEquals(expD.getD1(), actD.getD1());
        assertNull("b1 was not  removed", actD.getB1());
        
        actDFull = (DFull) this.objectConverter.getObject(getSession(), DFull.class,  "/someD");
        
        assertEquals(expD.getD1(), actDFull.getD1());
        assertNull("b1 was not  removed", actDFull.getB1());
        assertNull("b2 wan not remove", actDFull.getB2());

    }
    
    /*
    public void testBeanDescriptorConverter() throws Exception {
        BeanDescriptor beanDescriptor = this.mapper.getClassDescriptorByClass(E.class).getBeanDescriptor("b1");
        //FakeBeanConverter converter = new FakeBeanConverter(this.objectConverter);
        
        //assertNotNull("E.b1 should be using the FakeBeanConverter", converter);
        
        B expB = new B();
        expB.setB1("b1value");
        expB.setB2("b2value");
        E expE = new E();
        expE.setPath("/someD");
        expE.setD1("d1value");
        expE.setB1(expB);
        
        this.objectConverter.insert(getSession(), expE);
        getSession().save();
        
        // HINT: FakeBeanConverter should set expB
        converter.setB(expB);
        E actE = (E) this.objectConverter.getObject(getSession(), "/someD");
        
        
        assertEquals(expE.getD1(), actE.getD1());
        assertEquals(expB, actE.getB1());
        
        expE.setD1("updatedvalueD1");
        expB.setB1("updatedvalue1");
        
        this.objectConverter.update(getSession(), expE);
        getSession().save();
        
        converter.setB(expB);
        actE = (E) this.objectConverter.getObject(getSession(),  "/someD");
        
        assertEquals(expE.getD1(), actE.getD1());
        assertEquals(expB, actE.getB1());
                
        expE.setB1(null);
        this.objectConverter.update(getSession(), expE);
        getSession().save();
        
        converter.setB(null);
        actE = (E) this.objectConverter.getObject(getSession(),  "/someD");
        
        assertEquals(expE.getD1(), actE.getD1());
        assertNull(actE.getB1());
        
        // HINT: check messages
        List messages = converter.getLog();
        assertEquals(6, messages.size());
        assertEquals("insert at path /someD", messages.get(0));
        assertEquals("get from path /someD", messages.get(1));
        assertEquals("update at path /someD", messages.get(2));
        assertEquals("get from path /someD", messages.get(3));
        assertEquals("remove from path /someD", messages.get(4));
        assertEquals("get from path /someD", messages.get(5));
    }
    */
    public static class FakeBeanConverter extends AbstractBeanConverterImpl {
        private static B returnB;
        private List log;
        
        
        
        public FakeBeanConverter(ObjectConverter objectConverter) {
			super(objectConverter);
			  log = new ArrayList();
		}

		public List getLog() {
            return this.log;
        }
        
        public void setB(B b) {
            this.returnB = b;
        }
        
        /**
         * @see org.apache.portals.graffito.jcr.persistence.objectconverter.BeanConverter#insert(javax.jcr.Session, javax.jcr.Node, org.apache.portals.graffito.jcr.mapper.Mapper, java.lang.String, java.lang.Object)
         */
        public void insert(Session session, Node parentNode, BeanDescriptor descriptor, Object object) throws PersistenceException {
            try {
                log.add("insert at path " + parentNode.getPath());
            }
            catch(RepositoryException re) {
                throw new PersistenceException(re);
            }
        } 

        /**
         * @see org.apache.portals.graffito.jcr.persistence.objectconverter.BeanConverter#update(javax.jcr.Session, javax.jcr.Node, org.apache.portals.graffito.jcr.mapper.Mapper, java.lang.String, java.lang.Object)
         */
        public void update(Session session, Node parentNode, BeanDescriptor descriptor, Object object) throws PersistenceException {
            try {
                log.add("update at path " + parentNode.getPath());
            }
            catch(RepositoryException re) {
                throw new PersistenceException(re);
            }
        }

        /**
         * @see org.apache.portals.graffito.jcr.persistence.objectconverter.BeanConverter#getObject(javax.jcr.Session, javax.jcr.Node, org.apache.portals.graffito.jcr.mapper.Mapper, java.lang.String, java.lang.Class)
         */
        public Object getObject(Session session, Node parentNode, BeanDescriptor descriptor, Class beanClass) throws PersistenceException {
            try {
                log.add("get from path " + parentNode.getPath());
            }
            catch(RepositoryException re) {
                throw new PersistenceException(re);
            }
            return this.returnB;
        }

        /**
         * @see org.apache.portals.graffito.jcr.persistence.objectconverter.BeanConverter#remove(javax.jcr.Session, javax.jcr.Node, org.apache.portals.graffito.jcr.mapper.Mapper, java.lang.String)
         */
        public void remove(Session session, Node parentNode, BeanDescriptor descriptor) throws PersistenceException {
            try {
                log.add("remove from path " + parentNode.getPath());
            }
            catch(RepositoryException re) {
                throw new PersistenceException(re);
            }
        }
    }
}
