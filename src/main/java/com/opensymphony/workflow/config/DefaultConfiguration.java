/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.config;

import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.loader.*;
import com.opensymphony.workflow.loader.ClassLoaderUtil;
import com.opensymphony.workflow.spi.WorkflowStore;
import com.opensymphony.workflow.util.DefaultVariableResolver;
import com.opensymphony.workflow.util.VariableResolver;

import org.w3c.dom.*;

import java.io.InputStream;
import java.io.Serializable;

import java.net.URL;

import java.util.*;

import javax.xml.parsers.*;

/**
 * Default implementation for a configuration object. This configuration object
 * is passed to the
 * {@link com.opensymphony.workflow.Workflow#setConfiguration(Configuration)}
 * method. If the configuration is not initialized, the
 * {@link #load(java.net.URL)} method will be called by the workflow.
 * Alternatively, the caller can explicitly load the configuration by calling
 * that method before calling
 * {@link com.opensymphony.workflow.Workflow#setConfiguration(Configuration)}.
 * <p>
 * The loading behaviour comes into play when specifying a configuration
 * remotely, for example in an EJB environment. It might be desirable to ensure
 * that the configuration is loaded from within the EJB server, rather than in
 * the calling client.
 * 
 * @author Hani Suleiman
 * @version $Revision: 1.14 $
 */
public class DefaultConfiguration implements Configuration, Serializable {
	// ~ Static fields/initializers
	// /////////////////////////////////////////////

	private static final long serialVersionUID = 4120889092947132961L;
	public static DefaultConfiguration INSTANCE = new DefaultConfiguration();

	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	private Map persistenceArgs = new HashMap();
	private String persistenceClass;
	private WorkflowFactory factory = new URLWorkflowFactory();
	private transient WorkflowStore store = null;
	private VariableResolver variableResolver = new DefaultVariableResolver();
	private boolean initialized;

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public boolean isInitialized() {
		return initialized;
	}

	public boolean isModifiable(String name) {
		return factory.isModifiable(name);
	}

	public void setPersistence(String persistence) {
		persistenceClass = persistence;
	}

	public String getPersistence() {
		return persistenceClass;
	}

	public Map getPersistenceArgs() {
		return persistenceArgs;
	}

	public VariableResolver getVariableResolver() {
		return variableResolver;
	}

	public WorkflowDescriptor getWorkflow(String name) throws FactoryException {
		WorkflowDescriptor workflow = factory.getWorkflow(name);

		if (workflow == null) {
			throw new FactoryException("Unknown workflow name");
		}

		return workflow;
	}

	public String[] getWorkflowNames() throws FactoryException {
		return factory.getWorkflowNames();
	}

	public WorkflowStore getWorkflowStore() throws StoreException {
		if (store == null) {
			String clazz = getPersistence();

			try {
				store = (WorkflowStore) Class.forName(clazz).newInstance();
			} catch (Exception ex) {
				throw new StoreException("Error creating store", ex);
			}

			store.init(getPersistenceArgs());
		}

		return store;
	}

	public void load(URL url) throws FactoryException {
		InputStream is = getInputStream(url);

		if (is == null) {
			throw new FactoryException("Cannot find osworkflow.xml configuration file in classpath or in META-INF");
		}

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);

			DocumentBuilder db;

			try {
				db = dbf.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new FactoryException("Error creating document builder", e);
			}

			Document doc = db.parse(is);

			Element root = (Element) doc.getElementsByTagName("osworkflow").item(0);
			Element p = XMLUtil.getChildElement(root, "persistence");
			Element resolver = XMLUtil.getChildElement(root, "resolver");
			Element factoryElement = XMLUtil.getChildElement(root, "factory");

			if (resolver != null) {
				String resolverClass = resolver.getAttribute("class");

				if (resolverClass != null) {
					variableResolver = (VariableResolver) ClassLoaderUtil.loadClass(resolverClass, getClass()).newInstance();
				}
			}

			persistenceClass = p.getAttribute("class");

			List args = XMLUtil.getChildElements(p, "property");

			// persistenceArgs = new HashMap();
			for (int i = 0; i < args.size(); i++) {
				Element e = (Element) args.get(i);
				persistenceArgs.put(e.getAttribute("key"), e.getAttribute("value"));
			}

			if (factoryElement != null) {
				String clazz = null;

				try {
					clazz = factoryElement.getAttribute("class");

					if (clazz == null) {
						throw new FactoryException("factory does not specify a class attribute");
					}

					factory = (WorkflowFactory) ClassLoaderUtil.loadClass(clazz, getClass()).newInstance();

					Properties properties = new Properties();
					List props = XMLUtil.getChildElements(factoryElement, "property");

					for (int i = 0; i < props.size(); i++) {
						Element e = (Element) props.get(i);
						properties.setProperty(e.getAttribute("key"), e.getAttribute("value"));
					}

					factory.init(properties);
					factory.initDone();
				} catch (FactoryException ex) {
					throw ex;
				} catch (Exception ex) {
					throw new FactoryException("Error creating workflow factory " + clazz, ex);
				}
			}

			initialized = true;
		} catch (FactoryException e) {
			throw e;
		} catch (Exception e) {
			throw new FactoryException("Error in workflow config", e);
		}
	}

	public boolean removeWorkflow(String workflow) throws FactoryException {
		return factory.removeWorkflow(workflow);
	}

	public boolean saveWorkflow(String name, WorkflowDescriptor descriptor, boolean replace) throws FactoryException {
		return factory.saveWorkflow(name, descriptor, replace);
	}

	/**
	 * Load the default configuration from the current context classloader. The
	 * search order is: <li>Specified URL</li> <li>osworkflow.xml</li> <li>
	 * /osworkflow.xml</li> <li>META-INF/osworkflow.xml</li> <li>
	 * /META-INF/osworkflow.xml</li>
	 */
	protected InputStream getInputStream(URL url) {
		InputStream is = null;

		if (url != null) {
			try {
				is = url.openStream();
			} catch (Exception ex) {
			}
		}

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		if (is == null) {
			try {
				is = classLoader.getResourceAsStream("osworkflow.xml");
			} catch (Exception e) {
			}
		}

		if (is == null) {
			try {
				is = classLoader.getResourceAsStream("/osworkflow.xml");
			} catch (Exception e) {
			}
		}

		if (is == null) {
			try {
				is = classLoader.getResourceAsStream("META-INF/osworkflow.xml");
			} catch (Exception e) {
			}
		}

		if (is == null) {
			try {
				is = classLoader.getResourceAsStream("/META-INF/osworkflow.xml");
			} catch (Exception e) {
			}
		}

		return is;
	}

	/**
	 * Get the workflow factory for this configuration. This method should never
	 * ever be called from client code!
	 */
	WorkflowFactory getFactory() {
		return factory;
	}
}
