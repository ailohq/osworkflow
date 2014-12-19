/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.config;

import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.loader.WorkflowFactory;
import com.opensymphony.workflow.spi.WorkflowStore;
import com.opensymphony.workflow.util.DefaultVariableResolver;
import com.opensymphony.workflow.util.VariableResolver;

import java.net.URL;

import java.util.Map;

/**
 * @author Quake Wang
 * @since 2004-5-2
 * @version $Revision: 1.4 $
 * 
 **/
public class SpringConfiguration implements Configuration {
	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	// we init this for backward compat since existing spring configs likely
	// don't specify this
	private VariableResolver variableResolver = new DefaultVariableResolver();
	private WorkflowFactory factory;
	private WorkflowStore store;

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public void setFactory(WorkflowFactory factory) {
		this.factory = factory;
	}

	public boolean isInitialized() {
		return false;
	}

	public boolean isModifiable(String name) {
		return factory.isModifiable(name);
	}

	public String getPersistence() {
		return null;
	}

	public Map getPersistenceArgs() {
		return null;
	}

	public void setStore(WorkflowStore store) {
		this.store = store;
	}

	public void setVariableResolver(VariableResolver variableResolver) {
		this.variableResolver = variableResolver;
	}

	public VariableResolver getVariableResolver() {
		return variableResolver;
	}

	public WorkflowDescriptor getWorkflow(String name) throws FactoryException {
		WorkflowDescriptor workflow = factory.getWorkflow(name);

		if (workflow == null) {
			throw new FactoryException("Unknown workflow name");
		}

		return workflow;
	}

	public String[] getWorkflowNames() throws FactoryException {
		return factory.getWorkflowNames();
	}

	public WorkflowStore getWorkflowStore() throws StoreException {
		return store;
	}

	public void load(URL url) throws FactoryException {
	}

	public boolean removeWorkflow(String workflow) throws FactoryException {
		return factory.removeWorkflow(workflow);
	}

	public boolean saveWorkflow(String name, WorkflowDescriptor descriptor, boolean replace) throws FactoryException {
		return factory.saveWorkflow(name, descriptor, replace);
	}
}
