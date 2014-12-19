/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow;

/**
 * @author Hani Suleiman (hani@formicary.net) Date: Apr 8, 2003 Time: 9:42:15 AM
 */
public class FactoryException extends WorkflowException {
	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	public FactoryException() {
	}

	public FactoryException(String message) {
		super(message);
	}

	public FactoryException(Exception cause) {
		super(cause);
	}

	public FactoryException(String message, Exception cause) {
		super(message, cause);
	}
}
