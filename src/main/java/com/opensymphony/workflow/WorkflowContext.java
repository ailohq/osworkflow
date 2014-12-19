/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow;

/**
 * Interface to be implemented if a new OSWorkflow interaction is to be created
 * (SOAP, EJB, Ofbiz, etc).
 * 
 * @author <a href="mailto:plightbo@hotmail.com">Pat Lightbody</a>
 */
public interface WorkflowContext extends java.io.Serializable {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	/**
	 * @return the workflow caller.
	 */
	public String getCaller();

	/**
	 * Sets the current transaction to be rolled back.
	 */
	public void setRollbackOnly();
}
