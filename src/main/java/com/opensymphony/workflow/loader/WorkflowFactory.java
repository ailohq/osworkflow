/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.loader;

import com.opensymphony.workflow.FactoryException;

import java.util.Properties;

/**
 * @author hani Date: Feb 15, 2005 Time: 11:18:48 PM
 */
public interface WorkflowFactory {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	void setLayout(String workflowName, Object layout);

	Object getLayout(String workflowName);

	boolean isModifiable(String name);

	String getName();

	Properties getProperties();

	WorkflowDescriptor getWorkflowFromXml(String xml) throws FactoryException;

	WorkflowDescriptor getWorkflow(String name) throws FactoryException;

	/**
	 * Get a workflow descriptor given a workflow name.
	 * 
	 * @param name
	 *            The name of the workflow to get.
	 * @return The descriptor for the specified workflow.
	 * @throws com.opensymphony.workflow.FactoryException
	 *             if the specified workflow name does not exist or cannot be
	 *             located.
	 */
	WorkflowDescriptor getWorkflow(String name, boolean validate) throws FactoryException;

	/**
	 * Get all workflow names in the current factory
	 * 
	 * @return An array of all workflow names
	 * @throws com.opensymphony.workflow.FactoryException
	 *             if the factory cannot determine the names of the workflows it
	 *             has.
	 */
	String[] getWorkflowNames() throws FactoryException;

	void createWorkflow(String name);

	void init(Properties p);

	void initDone() throws FactoryException;

	boolean removeWorkflow(String name) throws FactoryException;

	void renameWorkflow(String oldName, String newName);

	void save();

	/**
	 * Save the workflow. Is it the responsibility of the caller to ensure that
	 * the workflow is valid, through the {@link WorkflowDescriptor#validate()}
	 * method. Invalid workflows will be saved without being checked.
	 * 
	 * @param name
	 *            The name of the workflow to same.
	 * @param descriptor
	 *            The descriptor for the workflow.
	 * @param replace
	 *            true if an existing workflow with this name should be
	 *            replaced.
	 * @return true if the workflow was saved.
	 * @throws com.opensymphony.workflow.FactoryException
	 *             if there was an error saving the workflow
	 * @throws com.opensymphony.workflow.InvalidWorkflowDescriptorException
	 *             if the descriptor specified is invalid
	 */
	boolean saveWorkflow(String name, WorkflowDescriptor descriptor, boolean replace) throws FactoryException;
}
