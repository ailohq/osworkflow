/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.spi.hibernate;

import com.opensymphony.workflow.spi.Step;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * This abstract class provides all the implementation of the step interface It
 * is abstract because the current and historical steps are stored in seperate
 * tables. To split the history and current steps into two tables in hibernate,
 * the easiest approach is to use two separate classes.
 */
public abstract class HibernateStep implements Step {
	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	Date dueDate;
	Date finishDate;
	Date startDate;
	HibernateWorkflowEntry entry;
	List previousSteps;
	String caller;
	String owner;
	String status;
	int actionId;
	int stepId;
	long id = -1;

	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	public HibernateStep() {
	}

	public HibernateStep(HibernateStep step) {
		this.actionId = step.getActionId();
		this.caller = step.getCaller();
		this.finishDate = step.getFinishDate();
		this.dueDate = step.getDueDate();
		this.startDate = step.getStartDate();

		// do not copy this value, it's for unsaved-value
		// this.id = step.getId();
		this.owner = step.getOwner();
		this.status = step.getStatus();
		this.stepId = step.getStepId();
		this.previousSteps = step.getPreviousSteps();
		this.entry = step.entry;
	}

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public void setActionId(int actionId) {
		this.actionId = actionId;
	}

	public int getActionId() {
		return actionId;
	}

	public void setCaller(String caller) {
		this.caller = caller;
	}

	public String getCaller() {
		return caller;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setEntry(HibernateWorkflowEntry entry) {
		this.entry = entry;
	}

	public HibernateWorkflowEntry getEntry() {
		return entry;
	}

	public long getEntryId() {
		return entry.getId();
	}

	public void setFinishDate(Date finishDate) {
		this.finishDate = finishDate;
	}

	public Date getFinishDate() {
		return finishDate;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOwner() {
		return owner;
	}

	public long[] getPreviousStepIds() {
		if (previousSteps == null) {
			return new long[0];
		}

		long[] previousStepIds = new long[previousSteps.size()];
		int i = 0;

		for (Iterator iterator = previousSteps.iterator(); iterator.hasNext();) {
			HibernateStep hibernateStep = (HibernateStep) iterator.next();
			previousStepIds[i] = hibernateStep.getId();
			i++;
		}

		return previousStepIds;
	}

	public void setPreviousSteps(List previousSteps) {
		this.previousSteps = previousSteps;
	}

	public List getPreviousSteps() {
		return previousSteps;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setStepId(int stepId) {
		this.stepId = stepId;
	}

	public int getStepId() {
		return stepId;
	}
}
