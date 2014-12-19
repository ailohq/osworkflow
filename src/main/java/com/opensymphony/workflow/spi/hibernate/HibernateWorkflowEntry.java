/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.spi.hibernate;

import com.opensymphony.workflow.spi.WorkflowEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hani
 */
public class HibernateWorkflowEntry implements WorkflowEntry {
	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	List currentSteps = new ArrayList();
	List historySteps = new ArrayList();
	String workflowName;
	long id = -1;
	private int state;
	private int version;

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public void setCurrentSteps(List currentSteps) {
		this.currentSteps = currentSteps;
	}

	public List getCurrentSteps() {
		return currentSteps;
	}

	public void setHistorySteps(List historySteps) {
		this.historySteps = historySteps;
	}

	public List getHistorySteps() {
		return historySteps;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public boolean isInitialized() {
		return state > 0;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getState() {
		return state;
	}

	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}

	public String getWorkflowName() {
		return workflowName;
	}

	public void addCurrentSteps(HibernateCurrentStep step) {
		step.setEntry(this);
		getCurrentSteps().add(step);
	}

	public void addHistorySteps(HibernateHistoryStep step) {
		step.setEntry(this);
		getHistorySteps().add(step);
	}

	public void removeCurrentSteps(HibernateCurrentStep step) {
		getCurrentSteps().remove(step);
	}

	protected void setVersion(int version) {
		this.version = version;
	}

	protected int getVersion() {
		return version;
	}
}
