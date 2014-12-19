/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow;

/**
 * Exception thrown to indicate that the action just attempt is invalid for the
 * specified workflow.
 * 
 * @author Hani Suleiman (hani@formicary.net)
 */
public class InvalidActionException extends InternalWorkflowException {
	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	public InvalidActionException() {
	}

	public InvalidActionException(String message) {
		super(message);
	}

	public InvalidActionException(Exception cause) {
		super(cause);
	}

	public InvalidActionException(String message, Exception cause) {
		super(message, cause);
	}
}
