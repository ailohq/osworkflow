/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util;

import com.opensymphony.module.propertyset.PropertySet;

import com.opensymphony.util.TextUtils;

import com.opensymphony.workflow.*;
import com.opensymphony.workflow.spi.Step;
import com.opensymphony.workflow.spi.WorkflowEntry;
import com.opensymphony.workflow.spi.WorkflowStore;

import java.util.*;

/**
 * Simple utility condition that returns true if the current step's status is
 * the same as the required argument "status". Looks at ALL current steps unless
 * a stepId is given in the optional argument "stepId".
 * 
 * @author <a href="mailto:plightbo@hotmail.com">Pat Lightbody</a>
 */
public class StatusCondition implements Condition {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public boolean passesCondition(Map transientVars, Map args, PropertySet ps) throws StoreException {
		String status = TextUtils.noNull((String) args.get("status"));
		int stepId = 0;
		Object stepIdVal = args.get("stepId");

		if (stepIdVal != null) {
			try {
				stepId = Integer.parseInt(stepIdVal.toString());
			} catch (Exception ex) {
			}
		}

		WorkflowEntry entry = (WorkflowEntry) transientVars.get("entry");
		WorkflowStore store = (WorkflowStore) transientVars.get("store");
		List currentSteps = store.findCurrentSteps(entry.getId());

		if (stepId == 0) {
			for (Iterator iterator = currentSteps.iterator(); iterator.hasNext();) {
				Step step = (Step) iterator.next();

				if (status.equals(step.getStatus())) {
					return true;
				}
			}
		} else {
			for (Iterator iterator = currentSteps.iterator(); iterator.hasNext();) {
				Step step = (Step) iterator.next();

				if (stepId == step.getStepId()) {
					if (status.equals(step.getStatus())) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
