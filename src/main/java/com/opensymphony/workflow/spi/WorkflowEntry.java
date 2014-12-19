/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.spi;

/**
 * Interface for a workflow entry.
 * 
 * @author <a href="mailto:plightbo@hotmail.com">Pat Lightbody</a>
 */
public interface WorkflowEntry {
	// ~ Static fields/initializers
	// /////////////////////////////////////////////

	public static final int CREATED = 0;
	public static final int ACTIVATED = 1;
	public static final int SUSPENDED = 2;
	public static final int KILLED = 3;
	public static final int COMPLETED = 4;
	public static final int UNKNOWN = -1;

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	/**
	 * Returns the unique ID of the workflow entry.
	 */
	public long getId();

	/**
	 * Returns true if the workflow entry has been initialized.
	 */
	public boolean isInitialized();

	public int getState();

	/**
	 * Returns the name of the workflow that this entry is an instance of.
	 */
	public String getWorkflowName();
}
