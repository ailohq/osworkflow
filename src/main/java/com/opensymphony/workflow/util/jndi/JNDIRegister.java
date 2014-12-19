/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util.jndi;

import com.opensymphony.module.propertyset.PropertySet;

import com.opensymphony.workflow.*;
import com.opensymphony.workflow.spi.WorkflowEntry;

import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Invoke a register registred in JNDI. Args must contain a
 * {@link AbstractWorkflow#JNDI_LOCATION} key.
 * 
 * @author $Author: hani $
 * @version $Revision: 1.5 $
 */
public class JNDIRegister implements Register {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public Object registerVariable(WorkflowContext context, WorkflowEntry entry, Map args, PropertySet ps) throws WorkflowException {
		String location = (String) args.get(AbstractWorkflow.JNDI_LOCATION);

		if (location == null) {
			throw new WorkflowException(AbstractWorkflow.JNDI_LOCATION + " argument is null");
		}

		Register r;

		try {
			try {
				r = (Register) new InitialContext().lookup(location);
			} catch (NamingException e) {
				// ok, couldn't find it, look in env
				r = (Register) new InitialContext().lookup("java:comp/env/" + location);
			}
		} catch (NamingException e) {
			String message = "Could not look up JNDI register at: " + location;
			throw new WorkflowException(message, e);
		}

		return r.registerVariable(context, entry, args, ps);
	}
}
