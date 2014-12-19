/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util;

import com.opensymphony.module.propertyset.PropertySet;

import com.opensymphony.workflow.Condition;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.WorkflowContext;
import com.opensymphony.workflow.spi.Step;
import com.opensymphony.workflow.spi.WorkflowEntry;
import com.opensymphony.workflow.spi.WorkflowStore;

import java.util.*;

/**
 * <p>
 * A simple utility condition that returns true if the the current user (caller)
 * is the step owner.
 * </p>
 * 
 * <p>
 * This condition may be used to deny the owner of the step by negating the
 * condition in the workflow descriptor with <code>negate='true'</code>.
 * <p>
 * 
 * <p>
 * Looks at ALL current steps unless a step id is given in the optional argument
 * "stepId".
 * </p>
 * 
 * <p>
 * The implementation was originally contained in AllowOwnerOfStepCondition by
 * Pat Lightbody.
 * </p>
 * 
 * @author <a href="mailto:plightbo@hotmail.com">Pat Lightbody </a> (original
 *         implementation)
 * @author <a href="mailto:adam@southtech.co.uk">Adam Southall </a> (refactored
 *         owner conditions to use this generic class.
 */
public class IsUserOwnerCondition implements Condition {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////////
	public boolean passesCondition(Map transientVars, Map args, PropertySet ps) throws StoreException {
		int stepId = 0;
		String stepIdVal = (String) args.get("stepId");

		if (stepIdVal != null) {
			try {
				stepId = Integer.parseInt(stepIdVal);
			} catch (Exception ex) {
			}
		}

		WorkflowContext context = (WorkflowContext) transientVars.get("context");
		WorkflowEntry entry = (WorkflowEntry) transientVars.get("entry");
		WorkflowStore store = (WorkflowStore) transientVars.get("store");
		List currentSteps = store.findCurrentSteps(entry.getId());

		if (stepId == 0) {
			for (Iterator iterator = currentSteps.iterator(); iterator.hasNext();) {
				Step step = (Step) iterator.next();

				if ((step.getOwner() != null) && context.getCaller().equals(step.getOwner())) {
					return true;
				}
			}
		} else {
			for (Iterator iterator = currentSteps.iterator(); iterator.hasNext();) {
				Step step = (Step) iterator.next();

				if (stepId == step.getStepId()) {
					if ((step.getOwner() != null) && context.getCaller().equals(step.getOwner())) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
