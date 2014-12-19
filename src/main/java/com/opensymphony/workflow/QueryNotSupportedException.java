/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow;

/**
 * Exception thrown to indicate that the query requested is not supported by the
 * current store
 * 
 * @author Hani Suleiman (hani@formicary.net) Date: Oct 4, 2003 Time: 5:26:31 PM
 */
public class QueryNotSupportedException extends InternalWorkflowException {
	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	public QueryNotSupportedException() {
	}

	public QueryNotSupportedException(String message) {
		super(message);
	}

	public QueryNotSupportedException(Exception cause) {
		super(cause);
	}

	public QueryNotSupportedException(String message, Exception cause) {
		super(message, cause);
	}
}
