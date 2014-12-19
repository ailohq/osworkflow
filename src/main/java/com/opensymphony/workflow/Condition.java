/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow;

import com.opensymphony.module.propertyset.PropertySet;

import java.util.Map;

/**
 * Interface that must be implemented to define a java-based condition in your
 * workflow definition.
 * 
 * @author <a href="mailto:plightbo@hotmail.com">Patrick Lightbody</a>
 */
public interface Condition {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	/**
	 * Determines if a condition should signal pass or fail.
	 * 
	 * @param transientVars
	 *            Variables that will not be persisted. These include inputs
	 *            given in the {@link Workflow#initialize} and
	 *            {@link Workflow#doAction} method calls. There are a number of
	 *            special variable names:
	 *            <ul>
	 *            <li><code>entry</code>: (object type:
	 *            {@link com.opensymphony.workflow.spi.WorkflowEntry}) The
	 *            workflow instance
	 *            <li><code>context</code>: (object type:
	 *            {@link com.opensymphony.workflow.WorkflowContext}). The
	 *            workflow context.
	 *            <li><code>actionId</code>: The Integer ID of the current
	 *            action that was take (if applicable).
	 *            <li><code>currentSteps</code>: A Collection of the current
	 *            steps in the workflow instance.
	 *            <li><code>store</code>: The
	 *            {@link com.opensymphony.workflow.spi.WorkflowStore}.
	 *            <li><code>descriptor</code>: The
	 *            {@link com.opensymphony.workflow.loader.WorkflowDescriptor}.
	 *            </ul>
	 *            <p>
	 *            Also, any variable set as a
	 *            {@link com.opensymphony.workflow.Register}), will also be
	 *            available in the transient map, no matter what. These
	 *            transient variables only last through the method call that
	 *            they were invoked in, such as {@link Workflow#initialize} and
	 *            {@link Workflow#doAction}.
	 * @param args
	 *            The properties for this function invocation. Properties are
	 *            created from arg nested elements within the xml, an arg
	 *            element takes in a name attribute which is the properties key,
	 *            and the CDATA text contents of the element map to the property
	 *            value. There is a magic property of '<code>stepId</code>'; if
	 *            specified with a value of -1, then the value is replaced with
	 *            the current step's ID before the condition is called.
	 * @param ps
	 *            The persistent variables that are associated with the current
	 *            instance of the workflow. Any change made to this will be seen
	 *            on the next function call in the workflow lifetime.
	 */
	public boolean passesCondition(Map transientVars, Map args, PropertySet ps) throws WorkflowException;
}
