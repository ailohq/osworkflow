/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util;

import com.opensymphony.workflow.InvalidWorkflowDescriptorException;

/**
 * Abstact base class for elements that can be validated.
 * 
 * @author <a href="mailto:vorburger@users.sourceforge.net">Michael
 *         Vorburger</a>
 * @version $Revision: 1.2 $
 */
public interface Validatable {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	/**
	 * Validate this element, and propagate validation to all contained
	 * sub-elements. Should throw an InvalidWorkflowDescriptorException with
	 * details in message if the element is invalid. Validity checks should be
	 * checks that cannot be encapsulated in the DTD.
	 * 
	 * Validation has to be called explicitly on writting, a writeXML() does not
	 * validate implicitly; it *IS* thus possible to write invalid descriptor
	 * files. This could be useful for e.g. a graphical workflow definition
	 * editor which would like to write incomplete definitions. Validation *IS*
	 * performed on loading a workflow definition.
	 * 
	 * @see com.opensymphony.workflow.loader.WorkflowLoader#load
	 * @throws InvalidWorkflowDescriptorException
	 */
	public void validate() throws InvalidWorkflowDescriptorException;
}
