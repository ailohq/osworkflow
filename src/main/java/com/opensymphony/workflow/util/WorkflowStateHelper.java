/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util;

import com.opensymphony.workflow.spi.WorkflowEntry;

/**
 * @author Hani Suleiman (hani@formicary.net) Date: Aug 29, 2003 Time: 5:14:56
 *         PM
 */
public class WorkflowStateHelper {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public static int[] getPossibleStates(int state) {
		switch (state) {
		case WorkflowEntry.CREATED:
			return new int[] { WorkflowEntry.ACTIVATED };

		case WorkflowEntry.ACTIVATED:
			return new int[] { WorkflowEntry.SUSPENDED, WorkflowEntry.KILLED };

		case WorkflowEntry.SUSPENDED:
			return new int[] { WorkflowEntry.ACTIVATED, WorkflowEntry.KILLED };

		default:
			return new int[0];
		}
	}
}
