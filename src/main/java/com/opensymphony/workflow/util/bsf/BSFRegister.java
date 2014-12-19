/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util.bsf;

import com.opensymphony.module.propertyset.PropertySet;

import com.opensymphony.util.TextUtils;

import com.opensymphony.workflow.*;
import com.opensymphony.workflow.spi.WorkflowEntry;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

import java.util.Map;

/**
 * Register that invokes a BSF script. args parameter is expected to contain the
 * follow arguments:
 * 
 * <li>{@link AbstractWorkflow#BSF_LANGUAGE}: The language of the script <li>
 * {@link AbstractWorkflow#BSF_SOURCE}: The source of the script <li>
 * {@link AbstractWorkflow#BSF_ROW}: The row of the script <li>
 * {@link AbstractWorkflow#BSF_COL}: The column of the script
 * 
 * @author $Author: hani $
 * @version $Revision: 1.4 $
 */
public class BSFRegister implements Register {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public Object registerVariable(WorkflowContext context, WorkflowEntry entry, Map args, PropertySet ps) throws WorkflowException {
		String language = (String) args.get(AbstractWorkflow.BSF_LANGUAGE);
		String source = (String) args.get(AbstractWorkflow.BSF_SOURCE);
		int row = TextUtils.parseInt((String) args.get(AbstractWorkflow.BSF_ROW));
		int col = TextUtils.parseInt((String) args.get(AbstractWorkflow.BSF_COL));
		String script = (String) args.get(AbstractWorkflow.BSF_SCRIPT);

		BSFManager mgr = new BSFManager();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		if (loader != null) {
			mgr.setClassLoader(loader);
		}

		mgr.registerBean("propertySet", ps);
		mgr.registerBean("entry", entry);
		mgr.registerBean("context", context);

		try {
			BSFEngine engine = mgr.loadScriptingEngine(language);

			return engine.eval(source, row, col, script);
		} catch (BSFException e) {
			String message = "Could not get object registered in to variable map";
			throw new WorkflowException(message, e);
		}
	}
}
