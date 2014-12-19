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
 * @version $Revision: 1.4 $
 */
public class JNDIValidator implements Validator {
	// ~ Static fields/initializers
	// /////////////////////////////////////////////

	private static final Log log = LogFactory.getLog(JNDIValidator.class);

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public void validate(Map transientVars, Map args, PropertySet ps) throws InvalidInputException, WorkflowException {
		String location = (String) args.get(AbstractWorkflow.JNDI_LOCATION);

		if (location == null) {
			throw new WorkflowException(AbstractWorkflow.JNDI_LOCATION + " argument is null");
		}

		Validator validator;

		try {
			try {
				validator = (Validator) new InitialContext().lookup(location);
			} catch (NamingException e) {
				validator = (Validator) new InitialContext().lookup("java:comp/env/" + location);
			}
		} catch (NamingException e) {
			String message = "Could not look up JNDI Validator at: " + location;
			throw new WorkflowException(message, e);
		}

		validator.validate(transientVars, args, ps);
	}
}
