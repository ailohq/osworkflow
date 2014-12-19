/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.spi.hibernate;

import com.opensymphony.workflow.loader.WorkflowDescriptor;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class WorkflowName {
	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	private String workflowName;
	private WorkflowDescriptor workflowDescriptor;

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public void setWorkflowDescriptor(WorkflowDescriptor workflowDescriptor) {
		this.workflowDescriptor = workflowDescriptor;
	}

	public WorkflowDescriptor getWorkflowDescriptor() {
		return workflowDescriptor;
	}

	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}

	public String getWorkflowName() {
		return workflowName;
	}
}
