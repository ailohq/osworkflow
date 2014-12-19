/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util.jndi;

import com.opensymphony.module.propertyset.PropertySet;

import com.opensymphony.workflow.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * 
 * 
 * @author $Author: hani $
 * @version $Revision: 1.3 $
 */
public class JNDICondition implements Condition {
	// ~ Static fields/initializers
	// /////////////////////////////////////////////

	private static final Log log = LogFactory.getLog(JNDICondition.class);

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public boolean passesCondition(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
		String location = (String) args.get(AbstractWorkflow.JNDI_LOCATION);
		location = location.trim();

		Condition condition = null;

		try {
			try {
				condition = (Condition) new InitialContext().lookup(location);
			} catch (NamingException e) {
				// ok, couldn't find it, look in env
				condition = (Condition) new InitialContext().lookup("java:comp/env/" + location);
			}
		} catch (NamingException e) {
			String message = "Could not lookup JNDI condition at: " + location;
			throw new WorkflowException(message, e);
		}

		return condition.passesCondition(transientVars, args, ps);
	}
}
