/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util;

import com.opensymphony.module.propertyset.PropertySet;

import com.opensymphony.workflow.*;

import webwork.action.Action;
import webwork.action.ActionContext;

import webwork.dispatcher.ActionResult;
import webwork.dispatcher.GenericDispatcher;

import java.lang.reflect.Method;

import java.security.Principal;

import java.util.*;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.2 $
 */
public class WebWorkValidator implements Validator {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public void validate(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
		final WorkflowContext wfContext = (WorkflowContext) transientVars.get("context");

		String actionName = (String) args.get("action.name");
		GenericDispatcher gd = new GenericDispatcher(actionName);
		gd.prepareContext();
		ActionContext.setPrincipal(new Principal() {
			public String getName() {
				return wfContext.getCaller();
			}
		});
		ActionContext.setApplication(args);
		ActionContext.setSession(ps.getProperties(""));
		ActionContext.setLocale(Locale.getDefault());
		ActionContext.setParameters(Collections.unmodifiableMap(transientVars));

		boolean hasErrors = false;
		InvalidInputException iie = new InvalidInputException();

		try {
			gd.executeAction();

			ActionResult ar = gd.finish();
			gd.finalizeContext();

			List actions = ar.getActions();

			for (Iterator iterator = actions.iterator(); iterator.hasNext();) {
				Action action = (Action) iterator.next();

				List errorMessages = getErrorMessages(action);

				for (Iterator iterator2 = errorMessages.iterator(); iterator2.hasNext();) {
					String error = (String) iterator2.next();
					iie.addError(error);
					hasErrors = true;
				}

				Map errors = getErrors(action);

				for (Iterator iterator2 = errors.entrySet().iterator(); iterator2.hasNext();) {
					Map.Entry entry = (Map.Entry) iterator2.next();
					String error = (String) entry.getKey();
					String message = (String) entry.getValue();
					iie.addError(error, message);
					hasErrors = true;
				}
			}
		} catch (Exception e) {
			throw new WorkflowException("Could not execute action " + actionName, e);
		}

		if (hasErrors) {
			throw iie;
		}
	}

	private List getErrorMessages(Action action) {
		try {
			Method m = action.getClass().getMethod("getErrorMessages", new Class[] {

					});

			return (List) m.invoke(action, new Object[] {});
		} catch (Throwable t) {
			return Collections.EMPTY_LIST;
		}
	}

	private Map getErrors(Action action) {
		try {
			Method m = action.getClass().getMethod("getErrors", new Class[] {});

			return (Map) m.invoke(action, new Object[] {});
		} catch (Throwable t) {
			return Collections.EMPTY_MAP;
		}
	}
}
