/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.spi;

import java.util.Date;

/**
 * Interface for a step associated with a workflow instance.
 * 
 * @author <a href="mailto:plightbo@hotmail.com">Pat Lightbody</a>
 */
public interface Step {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	/**
	 * Returns the ID of the action associated with this step, or 0 if there is
	 * no action associated.
	 */
	public int getActionId();

	public String getCaller();

	/**
	 * Returns an optional date signifying when this step must be finished.
	 */
	public Date getDueDate();

	/**
	 * Returns the unique ID of the workflow entry.
	 */
	public long getEntryId();

	/**
	 * Returns the date this step was finished, or null if it isn't finished.
	 */
	public Date getFinishDate();

	/**
	 * Returns the unique ID of this step.
	 */
	public long getId();

	/**
	 * Returns the owner of this step, or null if there is no owner.
	 */
	public String getOwner();

	/**
	 * Returns the unique ID of the previous step, or 0 if this is the first
	 * step.
	 */
	public long[] getPreviousStepIds();

	/**
	 * Returns the date that this step was created.
	 */
	public Date getStartDate();

	/**
	 * Returns the status of this step.
	 */
	public String getStatus();

	/**
	 * Returns the ID of the step in the workflow definition.
	 */
	public int getStepId();
}
