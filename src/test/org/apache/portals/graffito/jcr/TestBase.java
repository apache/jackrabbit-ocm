/* ========================================================================
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================================
 */
package org.apache.portals.graffito.jcr;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;

import junit.framework.TestCase;

import org.apache.portals.graffito.jcr.exception.RepositoryException;
import org.apache.portals.graffito.jcr.mapper.impl.DigesterMapperImpl;
import org.apache.portals.graffito.jcr.persistence.PersistenceManager;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.impl.BinaryTypeConverterImpl;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.impl.BooleanTypeConverterImpl;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.impl.ByteArrayTypeConverterImpl;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.impl.CalendarTypeConverterImpl;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.impl.DoubleTypeConverterImpl;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.impl.IntTypeConverterImpl;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.impl.LongTypeConverterImpl;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.impl.StringTypeConverterImpl;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.impl.TimestampTypeConverterImpl;
import org.apache.portals.graffito.jcr.persistence.atomictypeconverter.impl.UtilDateTypeConverterImpl;
import org.apache.portals.graffito.jcr.persistence.impl.PersistenceManagerImpl;
import org.apache.portals.graffito.jcr.query.QueryManager;
import org.apache.portals.graffito.jcr.query.impl.QueryManagerImpl;
import org.apache.portals.graffito.jcr.repository.RepositoryUtil;
import org.xml.sax.ContentHandler;

/**
 * Base class for testcases. Provides priviledged access to the jcr test
 * repository.
 * 
 * @author <a href="mailto:okiessler@apache.org">Oliver Kiessler</a>
 * @version $Id: Exp $
 */
public abstract class TestBase extends TestCase
{

	protected Session session;

	private PersistenceManager persistenceManager;

	private QueryManager queryManager;

	DigesterMapperImpl mapper;

	private static boolean isInit = false;

	/**
	 * <p>
	 * Defines the test case name for junit.
	 * </p>
	 * 
	 * @param testName
	 *            The test case name.
	 */
	public TestBase(String testName)
	{
		super(testName);

		try
		{
			if (!isInit)
			{
				RepositoryUtil.registerRepository("repositoryTest", "./src/test-config/repository.xml", "./target/repository");
				isInit = true;
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Impossible to init the repository");
		}
	}

	/**
	 * Setting up the testcase.
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	public void tearDown() throws Exception
	{
		super.tearDown();
	}

	/**
	 * Getter for property persistenceManager.
	 * 
	 * @return jcrSession
	 */
	public PersistenceManager getPersistenceManager()
	{
		try
		{
			if (persistenceManager == null)
			{
				initPersistenceManager();
			}
			return persistenceManager;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	protected void initPersistenceManager() throws UnsupportedRepositoryOperationException, javax.jcr.RepositoryException
	{
		Repository repository = RepositoryUtil.getRepository("repositoryTest");
		String[] files = { "./src/test-config/jcrmapping.xml", "./src/test-config/jcrmapping-atomic.xml" };
		session = RepositoryUtil.login(repository, "superuser", "superuser");		
		HashMap atomicTypeConverters = new HashMap();
		atomicTypeConverters.put(String.class, new StringTypeConverterImpl(session.getValueFactory()));
		atomicTypeConverters.put(InputStream.class, new BinaryTypeConverterImpl(session.getValueFactory()));
		atomicTypeConverters.put(long.class, new LongTypeConverterImpl(session.getValueFactory()));
		atomicTypeConverters.put(Long.class, new LongTypeConverterImpl(session.getValueFactory()));
		atomicTypeConverters.put(int.class, new IntTypeConverterImpl(session.getValueFactory()));
		atomicTypeConverters.put(Integer.class, new IntTypeConverterImpl(session.getValueFactory()));
		atomicTypeConverters.put(double.class, new DoubleTypeConverterImpl(session.getValueFactory()));
		atomicTypeConverters.put(Double.class, new DoubleTypeConverterImpl(session.getValueFactory()));
		atomicTypeConverters.put(boolean.class, new BooleanTypeConverterImpl(session.getValueFactory()));
		atomicTypeConverters.put(Boolean.class, new BooleanTypeConverterImpl(session.getValueFactory()));
		atomicTypeConverters.put(Calendar.class, new CalendarTypeConverterImpl(session.getValueFactory()));
		atomicTypeConverters.put(GregorianCalendar.class, new CalendarTypeConverterImpl(session.getValueFactory()));
		atomicTypeConverters.put(Date.class, new UtilDateTypeConverterImpl(session.getValueFactory()));
		atomicTypeConverters.put(byte[].class, new ByteArrayTypeConverterImpl(session.getValueFactory()));
		atomicTypeConverters.put(Timestamp.class, new TimestampTypeConverterImpl(session.getValueFactory()));
		
		mapper = new DigesterMapperImpl(files);						
		queryManager = new QueryManagerImpl(mapper, atomicTypeConverters);
		persistenceManager = new PersistenceManagerImpl(mapper, atomicTypeConverters, queryManager, session);
		
	}

	/**
	 * Setter for property jcrSession.
	 * 
	 * @param persistenceManager
	 *            The persistence manager
	 */
	public void setPersistenceManager(PersistenceManager persistenceManager)
	{
		this.persistenceManager = persistenceManager;
	}

	public void exportDocument(String filePath, String nodePath, boolean skipBinary, boolean noRecurse)
	{
		try
		{
			BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(filePath));
			ContentHandler handler = new org.apache.xml.serialize.XMLSerializer(os, null).asContentHandler();
			session.exportDocumentView(nodePath, handler, skipBinary, noRecurse);
			os.flush();
			os.close();
		}
		catch (Exception e)
		{
			System.out.println("Impossible to export the content from : " + nodePath);
			e.printStackTrace();
		}
	}

	public void importDocument(String filePath, String nodePath)
	{
		try
		{
			BufferedInputStream is = new BufferedInputStream(new FileInputStream(filePath));
			session.importXML(nodePath, is, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
			session.save();
			is.close();
		}
		catch (Exception e)
		{
			System.out.println("Impossible to import the content from : " + nodePath);
			e.printStackTrace();
		}

	}

	protected Session getSession()
	{
		return this.session;
	}

	public QueryManager getQueryManager()
	{
		return this.queryManager;
	}

}