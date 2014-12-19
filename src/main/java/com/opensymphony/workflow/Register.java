/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow;

import com.opensymphony.module.propertyset.PropertySet;

import com.opensymphony.workflow.spi.WorkflowEntry;

import java.util.Map;

/**
 * Interface that must be implemented for workflow registers to behave properly.
 * 
 * @author <a href="mailto:plightbo@hotmail.com">Patrick Lightbody</a>
 * @version $Revision: 1.3 $
 */
public interface Register {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	/**
	 * Returns the object to bind to the variable map for this workflow
	 * instance.
	 * 
	 * @param context
	 *            The current workflow context
	 * @param entry
	 *            The workflow entry. Note that this might be null, for example
	 *            in a pre function before the workflow has been initialised
	 * @param args
	 *            Map of arguments as set in the workflow descriptor
	 * 
	 * @param ps
	 * @return the object to bind to the variable map for this workflow instance
	 */
	public Object registerVariable(WorkflowContext context, WorkflowEntry entry, Map args, PropertySet ps) throws WorkflowException;
}
