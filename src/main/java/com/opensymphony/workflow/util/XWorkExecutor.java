/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util;

import com.opensymphony.module.propertyset.PropertySet;

import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.WorkflowException;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionProxy;
import com.opensymphony.xwork.ActionProxyFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Executes an XWork function. The following conversion is done:
 * <ul>
 * <li>inputs -> ActionContext#parameters</li>
 * <li>variables -> ActionContext#session</li>
 * <li>args -> ActionContext#application</li>
 * </ul>
 * <p>
 * 
 * <ul>
 * <li><b>action.name</b> - the actionName to ask from the ActionProxy</li>
 * <li><b>namespace</b> - the namespace to ask from the ActionProxy</li>
 * </ul>
 */
public class XWorkExecutor implements FunctionProvider {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
		String actionName = (String) args.get("action.name");
		String namespace = (String) args.get("namespace");

		Map extraContext = new HashMap();
		extraContext.put(ActionContext.APPLICATION, args);
		extraContext.put(ActionContext.SESSION, ps.getProperties(""));
		extraContext.put(ActionContext.LOCALE, Locale.getDefault());

		Map params = new HashMap(transientVars);
		params.putAll(args);
		extraContext.put(ActionContext.PARAMETERS, Collections.unmodifiableMap(params));

		try {
			ActionProxy proxy = ActionProxyFactory.getFactory().createActionProxy(namespace, actionName, extraContext, false);
			proxy.execute();
		} catch (Exception e) {
			throw new WorkflowException("Could not execute action " + actionName, e);
		}
	}
}
