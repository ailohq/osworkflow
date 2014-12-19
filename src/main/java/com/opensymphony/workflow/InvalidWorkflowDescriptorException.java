/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow;

/**
 * Indicates that a Workflow Descriptor was invalid. Usually this indicates a
 * semantically incorrect XML workflow definition.
 * 
 * @author <a href="mailto:vorburger@users.sourceforge.net">Michael
 *         Vorburger</a>
 */
public class InvalidWorkflowDescriptorException extends FactoryException {
	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	public InvalidWorkflowDescriptorException(String message) {
		super(message);
	}

	public InvalidWorkflowDescriptorException(String message, Exception cause) {
		super(message, cause);
	}
}
