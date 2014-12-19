/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util;

import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.spi.WorkflowEntry;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Listener interface to be implemented by remote session beans that wish to be
 * notified of changes in the workflow.
 * 
 * @author Hani Suleiman Date: Apr 6, 2002 Time: 11:48:14 PM
 */
public interface WorkflowListener extends Remote {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public void stateChanged(WorkflowEntry entry) throws RemoteException, WorkflowException;
}
