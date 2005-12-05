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
package org.apache.portals.graffito.jcr.persistence.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.query.QueryResult;
import javax.jcr.version.VersionHistory;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.portals.graffito.jcr.exception.CannotUnlockException;
import org.apache.portals.graffito.jcr.exception.JcrMappingException;
import org.apache.portals.graffito.jcr.exception.LockedException;
import org.apache.portals.graffito.jcr.exception.PersistenceException;
import org.apache.portals.graffito.jcr.exception.VersionException;
import org.apache.portals.graffito.jcr.mapper.Mapper;
import org.apache.portals.graffito.jcr.mapper.model.ClassDescriptor;
import org.apache.portals.graffito.jcr.persistence.PersistenceManager;
import org.apache.portals.graffito.jcr.persistence.objectconverter.ObjectConverter;
import org.apache.portals.graffito.jcr.persistence.objectconverter.impl.ObjectConverterImpl;
import org.apache.portals.graffito.jcr.query.Query;
import org.apache.portals.graffito.jcr.query.QueryManager;
import org.apache.portals.graffito.jcr.version.Version;
import org.apache.portals.graffito.jcr.version.VersionIterator;

/** 
 * 
 * Default implementation for {@link org.apache.portals.graffito.jcr.persistence.PersistenceManager}
 * 
 * @author Sandro Boehme
 * @author <a href="mailto:christophe.lombart@sword-technologies.com">Lombart Christophe</a>
 * @author Martin Koci
 * 
 */
public class PersistenceManagerImpl implements PersistenceManager
{
	/** 
	 * Logger.
	 */
	private final static Log log = LogFactory
			.getLog(PersistenceManagerImpl.class);

	/** 
	 * JCR session.
	 */
	protected Session session;

	protected Mapper mapper;

	/**
	 * The Graffito query manager
	 */
	protected QueryManager queryManager;

	/**
	 * Object Converter
	 */
	protected ObjectConverter objectConverter;

	/**
	 * Contructor 
	 * 
	 * @param mapper the Mapper component
	 * @param atomicTypeConverters Atomic type converters to used
	 * @param queryManager the query manager to used
	 * @param session The JCR session 
	 * 
	 */
	public PersistenceManagerImpl(Mapper mapper, Map atomicTypeConverters,
			QueryManager queryManager, Session session)
	{
		this.mapper = mapper;
		this.session = session;
		this.objectConverter = new ObjectConverterImpl(mapper,
				atomicTypeConverters);
		this.queryManager = queryManager;
	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#getObject(java.lang.Class, java.lang.String)
	 */
	public Object getObject(Class objectClass, String path)
	{
		try
		{
			if (!session.itemExists(path))
			{
				return null;
			}

		}
		catch (Exception e)
		{
			throw new PersistenceException("Impossible to get the object at "
					+ path, e);
		}

		return objectConverter.getObject(session, objectClass, path);

	}

	/**
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#getObject(java.lang.Class, java.lang.String, java.lang.String)
	 */
	public Object getObject(Class objectClass, String path, String versionName)
			throws PersistenceException
	{
		String pathVersion = null;
		try
		{
			if (!session.itemExists(path))
			{
				return null;
			}

			Version version = this.getVersion(path, versionName);
			pathVersion = version.getPath() + "/jcr:frozenNode";

		}
		catch (Exception e)
		{
			throw new PersistenceException("Impossible to get the object at "
					+ path + " - version :" + versionName, e);
		}

		return objectConverter.getObject(session, objectClass, pathVersion);
	}

	/**
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#insert(java.lang.Object)
	 */
	public void insert(Object object)
	{
		String path = objectConverter.getPath(session, object);
		
		try
		{

			if (session.itemExists(path))
			{
				Item item = session.getItem(path);
				if (item.isNode())
				{
					if (!((Node) item).getDefinition().allowsSameNameSiblings())
					{
						throw new PersistenceException(
								"Path already exists and it is not supporting the same name sibling : "
										+ path);
					}
				}
				else
				{
					throw new PersistenceException(
							"Path already exists and it is a property : "
									+ path);
				}

			}
		}
		catch (RepositoryException e)
		{
			throw new PersistenceException(
					"Impossible to insert the object at " + path, e);
		}

		objectConverter.insert(session, object);

	}

	/**
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#update(java.lang.Object)
	 */
	public void update(Object object)
	{
		String path = objectConverter.getPath(session, object);
		try
		{
			if (!session.itemExists(path))
			{
				throw new PersistenceException("Path is not existing : " + path);
			}
			else
			{
				checkIfNodeLocked(path);
			}
		}
		catch (RepositoryException e)
		{
			throw new PersistenceException("Impossible to update", e);
		}

		objectConverter.update(session, object);
	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#remove(java.lang.String)
	 */
	public void remove(String path)
	{

		try
		{
			if (!session.itemExists(path))
			{
				throw new PersistenceException("Path is not existing : " + path);
			}
			else
			{
				checkIfNodeLocked(path);
			}
			
			Item item = session.getItem(path);
			item.remove();

		}
		catch (RepositoryException e)
		{
			throw new PersistenceException(
					"Impossible to remove the object at " + path);
		}
	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#remove(java.lang.Object)
	 */
	public void remove(Object object) throws PersistenceException
	{
		this.remove(objectConverter.getPath(session, object));
	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#remove(org.apache.portals.graffito.jcr.query.Query)
	 */
	public void remove(Query query)
	{
		try
		{

			String jcrExpression = this.queryManager.buildJCRExpression(query);
			log.debug("Remove Objects with expression : " + jcrExpression);

			javax.jcr.query.Query jcrQuery = session.getWorkspace().getQueryManager().createQuery(jcrExpression,javax.jcr.query.Query.XPATH);
			QueryResult queryResult = jcrQuery.execute();
			NodeIterator nodeIterator = queryResult.getNodes();
			ArrayList nodes = new ArrayList();

			while (nodeIterator.hasNext())
			{
				Node node = nodeIterator.nextNode();
				log.debug("Remove node : " + node.getPath());
				// it is not possible to remove nodes from an NodeIterator
				// So, we add the node found in a collection to remove them after
				nodes.add(node);
			}

			// Remove all collection nodes
			for (int i = 0; i < nodes.size(); i++)
			{				
				Node node = (Node) nodes.get(i);
				checkIfNodeLocked(node.getPath());
				node.remove();
			}

		}
		catch (RepositoryException e)
		{
			throw new PersistenceException("Impossible to get the object collection", e);
		}

	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#objectExists(java.lang.String)
	 */
	public boolean objectExists(String path)
	{
		try
		{
			//TODO : Check also if it is an object 
			return session.itemExists(path);
		}
		catch (RepositoryException e)
		{
			throw new PersistenceException(
					"Impossible to check if the object exist", e);
		}
	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#isPersistent(java.lang.Class)
	 */
	public boolean isPersistent(final Class clazz)
	{
		boolean isPersistent = false;
		ClassDescriptor classDescriptor = mapper.getClassDescriptor(clazz);
		if (classDescriptor != null)
		{
			isPersistent = true;
		}
		return isPersistent;
	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#getObject(org.apache.portals.graffito.jcr.query.Query)
	 */
	public Object getObject(Query query)
	{

		try
		{

			String jcrExpression = this.queryManager.buildJCRExpression(query);
			log.debug("Get Object with expression : " + jcrExpression);

			javax.jcr.query.Query jcrQuery = session.getWorkspace()
					.getQueryManager().createQuery(jcrExpression,
							javax.jcr.query.Query.XPATH);
			QueryResult queryResult = jcrQuery.execute();
			NodeIterator nodeIterator = queryResult.getNodes();

			if (nodeIterator.getSize() > 1)
			{
				throw new PersistenceException(
						"Impossible to get the object - the query returns more than one object");
			}

			Object object = null;
			if (nodeIterator.hasNext())
			{
				Node node = nodeIterator.nextNode();
				object = objectConverter.getObject(session, query.getFilter()
						.getFilterClass(), node.getPath());
			}

			return object;
		}
		catch (RepositoryException e)
		{
			throw new PersistenceException(
					"Impossible to get the object collection", e);
		}

	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#getObjects(org.apache.portals.graffito.jcr.query.Query)
	 */
	public Collection getObjects(Query query)
	{
		try
		{

			String jcrExpression = this.queryManager.buildJCRExpression(query);
			log.debug("Get Objects with expression : " + jcrExpression);

			javax.jcr.query.Query jcrQuery = session.getWorkspace()
					.getQueryManager().createQuery(jcrExpression,
							javax.jcr.query.Query.XPATH);
			QueryResult queryResult = jcrQuery.execute();
			NodeIterator nodeIterator = queryResult.getNodes();

			ArrayList result = new ArrayList();
			while (nodeIterator.hasNext())
			{
				Node node = nodeIterator.nextNode();
				log.debug("Node found : " + node.getPath());
				result.add(objectConverter.getObject(session, query.getFilter()
						.getFilterClass(), node.getPath()));
			}

			return result;
		}
		catch (RepositoryException e)
		{
			throw new PersistenceException(
					"Impossible to get the object collection", e);
		}
	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#getObjectIterator(org.apache.portals.graffito.jcr.query.Query)
	 */
	public Iterator getObjectIterator(Query query)
	{
		try
		{

			String jcrExpression = this.queryManager.buildJCRExpression(query);
			log.debug("Get Object with expression : " + jcrExpression);

			javax.jcr.query.Query jcrQuery = session.getWorkspace()
					.getQueryManager().createQuery(jcrExpression,
							javax.jcr.query.Query.XPATH);
			QueryResult queryResult = jcrQuery.execute();
			NodeIterator nodeIterator = queryResult.getNodes();
			return new ObjectIterator(nodeIterator, query.getFilter()
					.getFilterClass(), this.objectConverter, this.session);

		}
		catch (RepositoryException e)
		{
			throw new PersistenceException(
					"Impossible to get the object collection", e);
		}
	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#checkin(java.lang.String)
	 */
	public void checkin(String path)
	{
		this.checkin(path, null);
	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#checkin(java.lang.String, java.lang.String[])
	 */
	public void checkin(String path, String[] versionLabels)
	{
		try
		{
			Node node = (Node) session.getItem(path);
			checkIfNodeLocked(node.getPath());
			if (!node.isNodeType("mix:versionable"))
			{
				throw new VersionException("The object " + path
						+ "is not versionable");
			}
			javax.jcr.version.Version newVersion = node.checkin();

			if (versionLabels != null)
			{
				VersionHistory versionHistory = node.getVersionHistory();
				for (int i = 0; i < versionLabels.length; i++)
				{
					versionHistory.addVersionLabel(newVersion.getName(),
							versionLabels[i], false);
				}
			}
		}
		catch (RepositoryException e)
		{
			throw new VersionException("Impossible to checkin the object "
					+ path, e);
		}

	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#checkout(java.lang.String)
	 */
	public void checkout(String path)
	{
		try
		{
			Node node = (Node) session.getItem(path);
			if (!node.isNodeType("mix:versionable"))
			{
				throw new VersionException("The object " + path
						+ "is not versionable");
			}

			node.checkout();

		}
		catch (RepositoryException e)
		{
			throw new VersionException("Impossible to checkout the object "
					+ path, e);
		}

	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#addVersionLabel(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void addVersionLabel(String path, String versionName,
			String versionLabel)
	{
		try
		{
			Node node = (Node) session.getItem(path);
			checkIfNodeLocked(path);
			if (!node.isNodeType("mix:versionable"))
			{
				throw new VersionException("The object " + path
						+ "is not versionable");
			}

			VersionHistory history = node.getVersionHistory();
			history.addVersionLabel(versionName, versionLabel, false);
		}
		catch (RepositoryException e)
		{
			throw new VersionException(
					"Impossible to add a new version label to  " + path
							+ " - version name : " + versionName, e);
		}
	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#getVersion(java.lang.String, java.lang.String)
	 */
	public Version getVersion(String path, String versionName)
	{

		try
		{
			Node node = (Node) session.getItem(path);
			if (!node.isNodeType("mix:versionable"))
			{
				throw new VersionException("The object " + path
						+ "is not versionable");
			}

			VersionHistory history = node.getVersionHistory();

			return new Version(history.getVersion(versionName));
		}
		catch (RepositoryException e)
		{
			throw new PersistenceException("Impossible to get the version : "
					+ path + " - version name : " + versionName, e);
		}
	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#getVersionLabels(java.lang.String, java.lang.String)
	 */
	public String[] getVersionLabels(String path, String versionName)
	{

		try
		{
			Node node = (Node) session.getItem(path);
			if (!node.isNodeType("mix:versionable"))
			{
				throw new VersionException("The object " + path
						+ "is not versionable");
			}

			VersionHistory history = node.getVersionHistory();
			javax.jcr.version.Version version = history.getVersion(versionName);
			return history.getVersionLabels(version);

		}
		catch (RepositoryException e)
		{
			throw new PersistenceException(
					"Impossible to get the version labels : " + path
							+ " - version name : " + versionName, e);
		}
	}

	/**
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#getAllVersionLabels(java.lang.String)
	 */
	public String[] getAllVersionLabels(String path)
			throws javax.jcr.version.VersionException
	{

		try
		{
			Node node = (Node) session.getItem(path);
			if (!node.isNodeType("mix:versionable"))
			{
				throw new VersionException("The object " + path
						+ "is not versionable");
			}

			VersionHistory history = node.getVersionHistory();
			return history.getVersionLabels();

		}
		catch (RepositoryException e)
		{
			throw new PersistenceException(
					"Impossible to get the all version labels : " + path, e);
		}
	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#getAllVersions(java.lang.String)
	 */
	public VersionIterator getAllVersions(String path)
	{
		try
		{
			Node node = (Node) session.getItem(path);
			if (!node.isNodeType("mix:versionable"))
			{
				throw new VersionException("The object " + path
						+ "is not versionable");
			}

			VersionHistory history = node.getVersionHistory();
			return new VersionIterator(history.getAllVersions());
		}
		catch (RepositoryException e)
		{
			throw new PersistenceException("Impossible to checkin the object "
					+ path, e);
		}

	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#getRootVersion(java.lang.String)
	 */
	public Version getRootVersion(String path)
	{
		try
		{
			Node node = (Node) session.getItem(path);
			if (!node.isNodeType("mix:versionable"))
			{
				throw new VersionException("The object " + path
						+ "is not versionable");
			}

			VersionHistory history = node.getVersionHistory();

			return new Version(history.getRootVersion());
		}
		catch (RepositoryException e)
		{
			throw new PersistenceException(
					"Impossible to get the root version  for the object "
							+ path, e);
		}

	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#getBaseVersion(java.lang.String)
	 */
	public Version getBaseVersion(String path)
	{
		try
		{
			Node node = (Node) session.getItem(path);
			if (!node.isNodeType("mix:versionable"))
			{
				throw new VersionException("The object " + path
						+ "is not versionable");
			}

			return new Version(node.getBaseVersion());
		}
		catch (RepositoryException e)
		{
			throw new PersistenceException(
					"Impossible to get the base version for the object " + path,
					e);
		}

	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#lock(java.lang.String, java.lang.Object, boolean, boolean)
	 */
	public String lock(final String absPath, final boolean isDeep, final boolean isSessionScoped) throws LockedException
	{
		try
		{

			// Calling this method will throw exception if node is locked
			// and this operation cant be done (exception translation)
			checkIfNodeLocked(absPath);

			Node node = getNode(absPath);
			Lock lock = node.lock(isDeep, isSessionScoped);
			return lock.getLockToken();
		}
		catch (LockException e)
		{
			// Only one case with LockException remains: if node is not mix:lockable, propably error in custom node types definitions
			throw new org.apache.portals.graffito.jcr.exception.RepositoryException("Node of type is not type mix:lockable", e);
		}
		catch (RepositoryException e)
		{
			throw new org.apache.portals.graffito.jcr.exception.RepositoryException(e.getMessage(), e);
		}
	}


	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#unlock(java.lang.String, java.lang.Object, java.lang.String)
	 */
	public void unlock(final String absPath, final String lockToken) throws JcrMappingException, CannotUnlockException
	{
		Node node;
		Lock lock;
		String lockOwner = null;
		try
		{
			maybeAddLockToken(lockToken);

			node = getNode(absPath);

			if (node.isLocked() == false)
			{
				// Safe - if not locked return
				return;
			}

			lock = node.getLock();
			lockOwner = lock.getLockOwner();

			node.unlock();
		}
		catch (LockException e)
		{
			// LockException if this node does not currently hold a lock (see upper code)
			// or holds a lock for which this Session does not have the correct lock token
			log
					.error("Cannot unlock path: "
							+ absPath
							+ " Jcr user: "
							+ session.getUserID()
							+ " has no lock token to do this. Lock was placed with user: "
							+ lockOwner);
			throw new CannotUnlockException(lockOwner, absPath);
		}
		catch (RepositoryException e)
		{
			// This also catch UnsupportedRepositoryOperationException - we assume that implementation supports it (jackrabbit does)
			throw new org.apache.portals.graffito.jcr.exception.RepositoryException(
					e.getMessage(), e);
		}
	}
	
	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#isLocked(java.lang.String)
	 */
	public boolean isLocked(final String absPath)
	{
		try
		{
			final Node node = getNode(absPath);
			return node.isLocked();
		}
		catch (RepositoryException e)
		{
			// node.isLocked() RepositoryException if an error occurs.
			throw new org.apache.portals.graffito.jcr.exception.RepositoryException(
					"General error with JCR", e);
		}
	}
	

	/**
	 * Throws {@link LockedException} id node is locked so alter nopde cannot be done
	 * 
	 * @param absPath
	 *            abs path to node
	 * @throws RepositoryException
	 * @throws LockedException
	 *             if node is locked
	 */
	protected void checkIfNodeLocked(final String absPath) 	throws RepositoryException, LockedException
	{
		Node node = getNode(absPath);
		// Node can hold nock or can be locked with precedencor
		if (node.isLocked())
		{
			Lock lock = node.getLock();
			String lockOwner = lock.getLockOwner();
			final String path = lock.getNode().getPath();
			throw new LockedException(lockOwner, path);
		}
	}

	protected void maybeAddLockToken(final String lockToken)
	{
		if (lockToken != null)
		{
			// This user (this instance of PM) potentionally placed lock so
			// session already has lock token
			final String[] lockTokens = getSession().getLockTokens();
			if (ArrayUtils.contains(lockTokens, lockToken))
			{
				// Ok = this session can unlock
			}
			else
			{
				getSession().addLockToken(lockToken);
			}
		}
	}

	protected Node getNode(final String absPath) throws PathNotFoundException,
			RepositoryException
	{

		if (!getSession().itemExists(absPath))
		{
			throw new org.apache.portals.graffito.jcr.exception.PersistenceException(
					"No object stored on path: " + absPath);
		}
		Item item = getSession().getItem(absPath);
		if (!item.isNode())
		{
			throw new org.apache.portals.graffito.jcr.exception.PersistenceException(
					"No object stored on path: " + absPath
							+ " on absPath is item (leaf)");
		}
		Node node = (Node) item;
		return node;
	}


	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#logout()
	 */
	public void logout()
	{
		try
		{
			session.save();
			session.logout();
		}
		catch (Exception e)
		{
			throw new PersistenceException("Impossible to logout", e);
		}
	}

	/**
	 * 
	 * @see org.apache.portals.graffito.jcr.persistence.PersistenceManager#save()
	 */
	public void save()
	{
		try
		{
			session.save();
		}
		catch (Exception e)
		{
			throw new PersistenceException("Impossible to save", e);
		}
	}

	/**
	 * @return The JCR Session
	 */
	public Session getSession()
	{
		return this.session;
	}

}
