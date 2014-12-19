/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util;

import com.opensymphony.module.propertyset.PropertySet;

import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.WorkflowContext;

import java.util.Map;

/**
 * Sets the transient variable "caller" to the current user executing an action.
 * 
 * @author <a href="mailto:plightbo@hotmail.com">Pat Lightbody</a>
 * @version $Revision: 1.4 $
 */
public class Caller implements FunctionProvider {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public void execute(Map transientVars, Map args, PropertySet ps) {
		WorkflowContext context = (WorkflowContext) transientVars.get("context");
		transientVars.put("caller", context.getCaller());
	}
}
