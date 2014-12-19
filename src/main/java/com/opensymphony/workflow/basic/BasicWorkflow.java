/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
/*
 * Created by IntelliJ IDEA.
 * User: plightbo
 * Date: Apr 29, 2002
 * Time: 11:12:05 PM
 */
package com.opensymphony.workflow.basic;

import com.opensymphony.workflow.AbstractWorkflow;

/**
 * A basic workflow implementation which does not read in the current user from
 * any context, but allows one to be specified via the constructor. Also does
 * not support rollbacks.
 */
public class BasicWorkflow extends AbstractWorkflow {
	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	public BasicWorkflow(String caller) {
		super();
		super.context = new BasicWorkflowContext(caller);
	}
}
