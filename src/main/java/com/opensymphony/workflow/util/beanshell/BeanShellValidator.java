/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util.beanshell;

import bsh.Interpreter;
import bsh.TargetError;

import com.opensymphony.module.propertyset.PropertySet;

import com.opensymphony.workflow.*;
import com.opensymphony.workflow.spi.WorkflowEntry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Beanshell inline script validator. The input is determined to be invalid of
 * the script throws a {@link InvalidInputException}.
 */
public class BeanShellValidator implements Validator {
	// ~ Static fields/initializers
	// /////////////////////////////////////////////

	private static final Log log = LogFactory.getLog(BeanShellValidator.class);

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public void validate(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
		Interpreter i = new Interpreter();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		try {
			String contents = (String) args.get(AbstractWorkflow.BSH_SCRIPT);

			WorkflowContext context = (WorkflowContext) transientVars.get("context");
			WorkflowEntry entry = (WorkflowEntry) transientVars.get("entry");

			if (loader != null) {
				i.setClassLoader(loader);
			}

			i.set("entry", entry);
			i.set("context", context);
			i.set("transientVars", transientVars);
			i.set("propertySet", ps);

			Object o = i.eval(contents);

			if (o != null) {
				throw new InvalidInputException(o);
			}
		} catch (TargetError e) {
			if (e.getTarget() instanceof WorkflowException) {
				throw (WorkflowException) e.getTarget();
			} else {
				throw new WorkflowException("Unexpected exception in beanshell validator script:" + e.getMessage(), e);
			}
		} catch (Exception e) {
			String message = "Error executing beanshell validator";
			throw new WorkflowException(message, e);
		} finally {
			if (loader != null) {
				i.setClassLoader(null);
			}
		}
	}
}
