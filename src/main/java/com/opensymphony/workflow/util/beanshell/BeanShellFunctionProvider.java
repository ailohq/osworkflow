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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * 
 * 
 * @author Hani
 */
public class BeanShellFunctionProvider implements FunctionProvider {
	// ~ Static fields/initializers
	// /////////////////////////////////////////////

	private static final Log log = LogFactory.getLog(BeanShellFunctionProvider.class);

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
		String script = (String) args.get(AbstractWorkflow.BSH_SCRIPT);
		Interpreter i = null;
		ClassLoader loader = null;
		WorkflowContext context = (WorkflowContext) transientVars.get("context");
		WorkflowEntry entry = (WorkflowEntry) transientVars.get("entry");
		loader = Thread.currentThread().getContextClassLoader();

		try {
			i = new Interpreter();

			if (loader != null) {
				i.setClassLoader(loader);
			}

			i.set("entry", entry);
			i.set("context", context);
			i.set("transientVars", transientVars);
			i.set("propertySet", ps);
		} catch (EvalError evalError) {
			String message = "Could not set values for BSH script";
			log.error(message, evalError);
			throw new WorkflowException(message, evalError);
		}

		try {
			i.eval(script);
		} catch (TargetError targetError) {
			if (targetError.getTarget() instanceof WorkflowException) {
				throw (WorkflowException) targetError.getTarget();
			} else {
				String message = "Evaluation error while running BSH function script";
				throw new WorkflowException(message, targetError.getTarget());
			}
		} catch (EvalError evalError) {
			String message = "Evaluation error while running BSH function script";
			log.error(message, evalError);
			throw new WorkflowException(message, evalError);
		} finally {
			if (loader != null) {
				i.setClassLoader(null);
			}
		}
	}
}
