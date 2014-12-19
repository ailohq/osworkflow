/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util;

import com.opensymphony.module.propertyset.PropertySet;

import com.opensymphony.workflow.Condition;
import com.opensymphony.workflow.WorkflowException;

import java.util.Map;

/**
 * @deprecated Use IsUserOwnerCondition instead
 * @see com.opensymphony.workflow.util.IsUserOwnerCondition
 * 
 *      Checks owner of "stepId" in args and compares to current user
 * 
 * @author Travis reeder Date: Feb 18, 2003 Time: 4:47:00 PM
 * @version 0.1
 */
public class AllowOwnerOfStepCondition implements Condition {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////////
	public boolean passesCondition(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
		IsUserOwnerCondition isOwnerCondition = new IsUserOwnerCondition();

		return isOwnerCondition.passesCondition(transientVars, args, ps);
	}
}
