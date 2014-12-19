/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow;

import com.opensymphony.module.propertyset.PropertySet;

import com.opensymphony.workflow.spi.WorkflowEntry;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.Map;

/**
 * A remote register interface. This interface is used when the register type is
 * remote-ejb. The stateless session bean would then implement this interface in
 * order for a register to be successfully registered.
 * 
 * @author <a href="mailto:hani@formicary.net">Hani Suleiman</a>
 * @version $Revision: 1.3 $
 */
public interface RegisterRemote extends Remote {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	/**
	 * Register a variable within a particular workflow
	 * 
	 * @param context
	 *            The current workflow context
	 * @param entry
	 *            The workflow entry. Note that this might be null, for example
	 *            in a pre function before the workflow has been initialised
	 * @param args
	 *            Map of arguments as set in the workflow descriptor
	 * @return An object which is now exposed as a register to the rest of the
	 *         workflow
	 * @throws RemoteException
	 */
	public Object registerVariable(WorkflowContext context, WorkflowEntry entry, Map args, PropertySet ps) throws RemoteException;
}
