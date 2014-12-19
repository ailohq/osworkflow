/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow;

import com.opensymphony.module.propertyset.PropertySet;

import com.opensymphony.workflow.config.Configuration;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.query.WorkflowExpressionQuery;
import com.opensymphony.workflow.query.WorkflowQuery;

import java.util.List;
import java.util.Map;

/**
 * The core workflow interface.
 * 
 * @author <a href="mailto:plightbo@hotmail.com">Patrick Lightbody</a>
 */
public interface Workflow {
	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	String BSF_COL = "col";
	String BSF_LANGUAGE = "language";
	String BSF_ROW = "row";
	String BSF_SCRIPT = "script";
	String BSF_SOURCE = "source";
	String BSH_SCRIPT = "script";

	// statics
	String CLASS_NAME = "class.name";
	String EJB_LOCATION = "ejb.location";
	String JNDI_LOCATION = "jndi.location";

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	/**
	 * @deprecated use {@link #getAvailableActions(long, Map)} with an empty Map
	 *             instead.
	 */
	public int[] getAvailableActions(long id);

	/**
	 * Returns a Collection of Step objects that are the current steps of the
	 * specified workflow instance.
	 * 
	 * @param id
	 *            The workflow instance id.
	 * @return The steps that the workflow instance is currently in.
	 */
	public List getCurrentSteps(long id);

	/**
	 * Return the state of the specified workflow instance id.
	 * 
	 * @param id
	 *            The workflow instance id.
	 * @return int The state id of the specified workflow
	 */
	public int getEntryState(long id);

	/**
	 * Returns a list of all steps that are completed for the given workflow
	 * instance id.
	 * 
	 * @param id
	 *            The workflow instance id.
	 * @return a List of Steps
	 * @see com.opensymphony.workflow.spi.Step
	 */
	public List getHistorySteps(long id);

	/**
	 * Get the PropertySet for the specified workflow instance id.
	 * 
	 * @param id
	 *            The workflow instance id.
	 */
	public PropertySet getPropertySet(long id);

	/**
	 * Get a collection (Strings) of currently defined permissions for the
	 * specified workflow instance.
	 * 
	 * @param id
	 *            the workflow instance id.
	 * @return A List of permissions specified currently (a permission is a
	 *         string name).
	 * @deprecated use {@link #getSecurityPermissions(long, java.util.Map)} with
	 *             a null map instead.
	 */
	public List getSecurityPermissions(long id);

	/**
	 * Get a collection (Strings) of currently defined permissions for the
	 * specified workflow instance.
	 * 
	 * @param id
	 *            id the workflow instance id.
	 * @param inputs
	 *            inputs The inputs to the workflow instance.
	 * @return A List of permissions specified currently (a permission is a
	 *         string name).
	 */
	public List getSecurityPermissions(long id, Map inputs);

	/**
	 * Get the workflow descriptor for the specified workflow name.
	 * 
	 * @param workflowName
	 *            The workflow name.
	 */
	public WorkflowDescriptor getWorkflowDescriptor(String workflowName);

	/**
	 * Get the name of the specified workflow instance.
	 * 
	 * @param id
	 *            the workflow instance id.
	 */
	public String getWorkflowName(long id);

	/**
	 * Check if the calling user has enough permissions to initialise the
	 * specified workflow.
	 * 
	 * @param workflowName
	 *            The name of the workflow to check.
	 * @param initialStep
	 *            The id of the initial state to check.
	 * @return true if the user can successfully call initialize, false
	 *         otherwise.
	 */
	public boolean canInitialize(String workflowName, int initialStep);

	/**
	 * Check if the state of the specified workflow instance can be changed to
	 * the new specified one.
	 * 
	 * @param id
	 *            The workflow instance id.
	 * @param newState
	 *            The new state id.
	 * @return true if the state of the workflow can be modified, false
	 *         otherwise.
	 */
	public boolean canModifyEntryState(long id, int newState);

	/**
	 * Modify the state of the specified workflow instance.
	 * 
	 * @param id
	 *            The workflow instance id.
	 * @param newState
	 *            the new state to change the workflow instance to. If the new
	 *            state is
	 *            {@link com.opensymphony.workflow.spi.WorkflowEntry.KILLED} or
	 *            {@link com.opensymphony.workflow.spi.WorkflowEntry.COMPLETED}
	 *            then all current steps are moved to history steps. If the new
	 *            state is
	 */
	public void changeEntryState(long id, int newState) throws WorkflowException;

	/**
	 * Perform an action on the specified workflow instance.
	 * 
	 * @param id
	 *            The workflow instance id.
	 * @param actionId
	 *            The action id to perform (action id's are listed in the
	 *            workflow descriptor).
	 * @param inputs
	 *            The inputs to the workflow instance.
	 * @throws InvalidInputException
	 *             if a validator is specified and an input is invalid.
	 * @throws InvalidActionException
	 *             if the action is invalid for the specified workflow
	 *             instance's current state.
	 */
	public void doAction(long id, int actionId, Map inputs) throws InvalidInputException, WorkflowException;

	/**
	 * Executes a special trigger-function using the context of the given
	 * workflow instance id. Note that this method is exposed for Quartz trigger
	 * jobs, user code should never call it.
	 * 
	 * @param id
	 *            The workflow instance id
	 * @param triggerId
	 *            The id of the speciail trigger-function
	 */
	public void executeTriggerFunction(long id, int triggerId) throws WorkflowException;

	/**
	 * Initializes a workflow so that it can begin processing. A workflow must
	 * be initialized before it can begin any sort of activity. It can only be
	 * initialized once.
	 * 
	 * @param workflowName
	 *            The workflow name to create and initialize an instance for
	 * @param initialAction
	 *            The initial step to start the workflow
	 * @param inputs
	 *            The inputs entered by the end-user
	 * @throws InvalidRoleException
	 *             if the user can't start this function
	 * @throws InvalidInputException
	 *             if a validator is specified and an input is invalid.
	 * @throws InvalidActionException
	 *             if the specified initial action is invalid for the specified
	 *             workflow.
	 */
	public long initialize(String workflowName, int initialAction, Map inputs) throws InvalidRoleException, InvalidInputException, WorkflowException, InvalidEntryStateException,
			InvalidActionException;

	/**
	 * Query the workflow store for matching instances
	 * 
	 * @deprecated use {@link Workflow#query(WorkflowExpressionQuery)} instead.
	 */
	public List query(WorkflowQuery query) throws WorkflowException;

	/**
	 * Query the workflow store for matching instances
	 */
	public List query(WorkflowExpressionQuery query) throws WorkflowException;

	/**
	 * Get the available actions for the specified workflow instance.
	 * 
	 * @ejb.interface-method
	 * @param id
	 *            The workflow instance id.
	 * @param inputs
	 *            The inputs map to pass on to conditions
	 * @return An array of action id's that can be performed on the specified
	 *         entry
	 * @throws IllegalArgumentException
	 *             if the specified id does not exist, or if its workflow
	 *             descriptor is no longer available or has become invalid.
	 */
	int[] getAvailableActions(long id, Map inputs);

	/**
	 * Set the configuration for this workflow. If not set, then the workflow
	 * will use the default configuration static instance.
	 * 
	 * @param configuration
	 *            a workflow configuration
	 */
	void setConfiguration(Configuration configuration);

	/**
	 * Get all available workflow names.
	 */
	String[] getWorkflowNames();

	/**
	 * Determine if a particular workflow can be initialized.
	 * 
	 * @param workflowName
	 *            The workflow name to check.
	 * @param initialAction
	 *            The potential initial action.
	 * @param inputs
	 *            The inputs to check.
	 * @return true if the workflow can be initialized, false otherwise.
	 */
	boolean canInitialize(String workflowName, int initialAction, Map inputs);

	/**
	 * Remove the specified workflow descriptor.
	 * 
	 * @param workflowName
	 *            The workflow name of the workflow to remove.
	 * @return true if the workflow was removed, false otherwise.
	 * @throws FactoryException
	 *             If the underlying workflow factory has an error removing the
	 *             workflow, or if it does not support the removal of workflows.
	 */
	boolean removeWorkflowDescriptor(String workflowName) throws FactoryException;

	/**
	 * Add a new workflow descriptor
	 * 
	 * @param workflowName
	 *            The workflow name of the workflow to add
	 * @param descriptor
	 *            The workflow descriptor to add
	 * @param replace
	 *            true, if an existing descriptor should be overwritten
	 * @return true if the workflow was added, fales otherwise
	 * @throws FactoryException
	 *             If the underlying workflow factory has an error adding the
	 *             workflow, or if it does not support adding workflows.
	 */
	boolean saveWorkflowDescriptor(String workflowName, WorkflowDescriptor descriptor, boolean replace) throws FactoryException;
}
