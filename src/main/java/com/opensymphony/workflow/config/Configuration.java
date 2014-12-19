/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.config;

import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.spi.WorkflowStore;
import com.opensymphony.workflow.util.VariableResolver;

import java.net.URL;

import java.util.Map;

/**
 * Configuration object that is responsible for all 'static' workflow
 * information. This includes loading of workflow configurations, setting up the
 * workflow descriptor factory, as well as proxying calls to the underlying
 * descriptor factory. Date: Mar 22, 2004 Time: 3:42:19 PM
 * 
 * @author hani
 */
public interface Configuration {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	/**
	 * @return true if this factory has been initialised. If the factory is not
	 *         initialised, then {@link #load(java.net.URL)} will be called.
	 */
	boolean isInitialized();

	/**
	 * Check if a particular workflow can be modified or not.
	 * 
	 * @param name
	 *            The workflow name.
	 * @return true if the workflow can be modified, false otherwise.
	 */
	boolean isModifiable(String name);

	/**
	 * Get the fully qualified class name of the persistence store.
	 */
	String getPersistence();

	/**
	 * Get the persistence args for the persistence store. Note that this
	 * returns the actual args and not a copy, so modifications to the returned
	 * Map could potentially affect store behaviour.
	 */
	Map getPersistenceArgs();

	/**
	 * Return the resolver to use for all variables specified in scripts
	 */
	VariableResolver getVariableResolver();

	/**
	 * Get the named workflow descriptor.
	 * 
	 * @param name
	 *            the workflow name
	 * @throws FactoryException
	 *             if there was an error looking up the descriptor or if it
	 *             could not be found.
	 */
	WorkflowDescriptor getWorkflow(String name) throws FactoryException;

	/**
	 * Get a list of all available workflow descriptor names.
	 * 
	 * @throws FactoryException
	 *             if the underlying factory does not support this method or if
	 *             there was an error looking up workflow names.
	 */
	String[] getWorkflowNames() throws FactoryException;

	WorkflowStore getWorkflowStore() throws StoreException;

	/**
	 * Load the specified configuration file.
	 * 
	 * @param url
	 *            url to the configuration file.
	 */
	void load(URL url) throws FactoryException;

	/**
	 * Remove the specified workflow.
	 * 
	 * @param workflow
	 *            The workflow name of the workflow to remove.
	 * @return true if the workflow was removed, false otherwise.
	 * @throws FactoryException
	 *             If the underlying workflow factory has an error removing the
	 *             workflow, or if it does not support the removal of workflows.
	 */
	boolean removeWorkflow(String workflow) throws FactoryException;

	boolean saveWorkflow(String name, WorkflowDescriptor descriptor, boolean replace) throws FactoryException;
}
