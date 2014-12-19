/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util;

import com.opensymphony.module.propertyset.PropertySet;

import com.opensymphony.user.*;

import com.opensymphony.workflow.*;

import java.util.Map;

/**
 * Simple utility class that uses OSUser to determine if the caller is in the
 * required argument "group".
 * 
 * @author <a href="mailto:plightbo@hotmail.com">Pat Lightbody</a>
 */
public class OSUserGroupCondition implements Condition {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public boolean passesCondition(Map transientVars, Map args, PropertySet ps) {
		try {
			WorkflowContext context = (WorkflowContext) transientVars.get("context");
			User user = UserManager.getInstance().getUser(context.getCaller());

			return user.inGroup((String) args.get("group"));
		} catch (EntityNotFoundException e) {
			return false;
		}
	}
}
