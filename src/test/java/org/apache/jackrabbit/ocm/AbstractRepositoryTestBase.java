/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.ocm;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeTypeManager;

import org.apache.jackrabbit.core.nodetype.InvalidNodeTypeDefException;
import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
import org.apache.jackrabbit.core.nodetype.NodeTypeRegistry;
import org.apache.jackrabbit.core.nodetype.xml.NodeTypeReader;
import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.apache.jackrabbit.ocm.reflection.ReflectionUtils;
import org.apache.jackrabbit.spi.QNodeTypeDefinition;
import org.apache.jackrabbit.test.AbstractJCRTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRepositoryTestBase extends AbstractJCRTest {


    private final static Logger log = LoggerFactory.getLogger(AbstractRepositoryTestBase.class);

    /** namespace prefix constant */
    public static final String OCM_NAMESPACE_PREFIX = "ocm";

    public static Repository repository;

    /** namespace constant */
    public static final String OCM_NAMESPACE = "http://jackrabbit.apache.org/ocm";

    private ObjectContentManager ocm;

    private Session session;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Session session = createSession();

        registerNamespace(session);
        registerNodeTypes(session);
        session.save();
        session.logout();
        ocm = createObjectContentManager(getSession());
    }

    protected abstract ObjectContentManager createObjectContentManager(Session session) throws javax.jcr.RepositoryException;

    public ObjectContentManager getObjectContentManager()
    {
        return ocm;
    }

    @Override
    public void tearDown() throws Exception {
        if (session != null) {
            session.logout();
        }
        cleanUpRepisotory();
        super.tearDown();
    }

    /**
     *
     * @return a readWrite session and creates a new one if this method is called for the first time for this AbstractRepositoryTestBase
     */
    protected Session getSession() {
        if (session != null) {
            return session;
        }
        session = createSession();
        return session;
    }

    private Session createSession() {
        try {
            return getHelper().getReadWriteSession();
        } catch (RepositoryException e) {
            throw new IllegalStateException("Could not get a jcr session", e);
        }
    }

    protected  void cleanUpRepisotory()
    {
        try
        {
            Session session = createSession();
            NodeIterator nodeIterator = session.getRootNode().getNodes();

            while (nodeIterator.hasNext())
            {
                Node node = nodeIterator.nextNode();
                if (! node.getName().startsWith("jcr:"))
                {
                    log.debug("tearDown - remove : " + node.getPath());
                    node.remove();
                }
            }
            session.save();
            session.logout();
        }
        catch(Exception e)
        {
            log.error("cleanUpRepository failed", e);
        }
    }

    public void exportDocument(String filePath, String nodePath, boolean skipBinary, boolean noRecurse)
    {
        try
        {
            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(filePath));
            Session session = getSession();
            session.exportDocumentView(nodePath, os, skipBinary, noRecurse);
            os.flush();
            os.close();
            session.logout();
        }
        catch (Exception e)
        {
            log.error("Impossible to export the content from : " + nodePath, e);
        }
    }

    protected void registerNamespace(final Session session) throws javax.jcr.RepositoryException {
        log.info("Register namespace");
        String[] jcrNamespaces = session.getWorkspace().getNamespaceRegistry().getPrefixes();
        boolean createNamespace = true;
        for (int i = 0; i < jcrNamespaces.length; i++) {
            if (jcrNamespaces[i].equals(OCM_NAMESPACE_PREFIX)) {
                createNamespace = false;
                log.debug("Jackrabbit OCM namespace exists.");
            }
        }
        if (createNamespace) {
            session.getWorkspace().getNamespaceRegistry().registerNamespace(OCM_NAMESPACE_PREFIX, OCM_NAMESPACE);
            log.info("Successfully created Jackrabbit OCM namespace.");
        }

        if (session.getRootNode() != null) {
            log.info("Jcr session setup successfull.");
        }
    }

    protected void registerNodeTypes(Session session)
            throws InvalidNodeTypeDefException, javax.jcr.RepositoryException, IOException {
        InputStream xml = new FileInputStream(
                "./src/test/test-config/nodetypes/custom_nodetypes.xml");

        // HINT: throws InvalidNodeTypeDefException, IOException
        QNodeTypeDefinition[] types = NodeTypeReader.read(xml);

        Workspace workspace = session.getWorkspace();
        NodeTypeManager ntMgr = workspace.getNodeTypeManager();
        NodeTypeRegistry ntReg = ((NodeTypeManagerImpl) ntMgr).getNodeTypeRegistry();

        for (int j = 0; j < types.length; j++) {
            QNodeTypeDefinition def = types[j];

            try {
                ntReg.getNodeTypeDef(def.getName());
            } catch (NoSuchNodeTypeException nsne) {
                // HINT: if not already registered than register custom node type
                ntReg.registerNodeType(def);
            }

        }
    }

    protected boolean contains(Collection result, String path, Class objectClass)
    {
        Iterator iterator = result.iterator();
        while (iterator.hasNext())
        {
            Object  object = iterator.next();
            String itemPath = (String)  ReflectionUtils.getNestedProperty(object, "path");
            if (itemPath.equals(path))
            {
                if (object.getClass() == objectClass)
                {
                    return true;
                }
                else
                {
                    return false;
                }

            }
        }
        return false;
    }
}