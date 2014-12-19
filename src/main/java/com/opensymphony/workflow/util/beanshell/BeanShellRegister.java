/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util.beanshell;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.TargetError;

import com.opensymphony.module.propertyset.PropertySet;

import com.opensymphony.workflow.*;
import com.opensymphony.workflow.spi.WorkflowEntry;

import java.util.Map;

/**
 * A register that executes a beanshell script when invoked.
 * 
 * @author Hani
 */
public class BeanShellRegister implements Register {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public Object registerVariable(WorkflowContext context, WorkflowEntry entry, Map args, PropertySet ps) throws WorkflowException {
		String script = (String) args.get(AbstractWorkflow.BSH_SCRIPT);

		Interpreter i = new Interpreter();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		try {
			if (loader != null) {
				i.setClassLoader(loader);
			}

			i.set("entry", entry);
			i.set("context", context);
			i.set("propertySet", ps);

			return i.eval(script);
		} catch (TargetError targetError) {
			if (targetError.getTarget() instanceof WorkflowException) {
				throw (WorkflowException) targetError.getTarget();
			} else {
				String message = "Could not get object registered in to variable map";
				throw new WorkflowException(message, targetError.getTarget());
			}
		} catch (EvalError e) {
			String message = "Could not get object registered in to variable map";
			throw new WorkflowException(message, e);
		} finally {
			if (loader != null) {
				i.setClassLoader(null);
			}
		}
	}
}
