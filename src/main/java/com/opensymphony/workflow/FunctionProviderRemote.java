/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow;

import com.opensymphony.module.propertyset.PropertySet;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.Map;

/**
 * Remote interface to be implemented by any class that are to be called from
 * within a workflow as a function, either as a pre-function or a post-function.
 * The args nested elements within the function xml call will be mapped to the
 * properties parameter.
 * 
 * @author <a href="mailto:plightbo@hotmail.com">Pat Lightbody</a>
 */
public interface FunctionProviderRemote extends Remote {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	/**
	 * Execute this function
	 * 
	 * @param transientVars
	 *            Variables that will not be persisted. These include inputs
	 *            given in the {@link Workflow#initialize} and
	 *            {@link Workflow#doAction} method calls. There are two special
	 *            variable names: <b>entry</b> (object type:
	 *            {@link com.opensymphony.workflow.spi.WorkflowEntry}) and
	 *            <b>context</b> (object type:
	 *            {@link com.opensymphony.workflow.WorkflowContext}). Also, any
	 *            variable set as a {@link com.opensymphony.workflow.Register}),
	 *            will also be available in the transient map, no matter what.
	 *            These transient variables only last through the method call
	 *            that they were invoked in, such as {@link Workflow#initialize}
	 *            and {@link Workflow#doAction}.
	 * @param args
	 *            The properties for this function invocation. Properties are
	 *            created from arg nested elements within the xml, an arg
	 *            element takes in a name attribute which is the properties key,
	 *            and the CDATA text contents of the element map to the property
	 *            value.
	 * @param ps
	 *            The persistent variables that are associated with the current
	 *            instance of the workflow. Any change made to the propertyset
	 *            are persisted to the propertyset implementation's persistent
	 *            store.
	 */
	public void execute(Map transientVars, Map args, PropertySet ps) throws RemoteException;
}
