/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.spi;

import java.io.Serializable;

import java.util.Date;

/**
 * Simple implementation
 * 
 * @author <a href="mailto:plightbo@hotmail.com">Pat Lightbody</a>
 */
public class SimpleStep implements Step, Serializable {
	// ~ Static fields/initializers
	// /////////////////////////////////////////////

	private static final long serialVersionUID = 1093783480189853982L;

	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	private Date dueDate;
	private Date finishDate;
	private Date startDate;
	private String caller;
	private String owner;
	private String status;
	private long[] previousStepIds;
	private int actionId;
	private int stepId;
	private long entryId;
	private long id;

	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	public SimpleStep() {
	}

	public SimpleStep(long id, long entryId, int stepId, int actionId, String owner, Date startDate, Date dueDate, Date finishDate, String status, long[] previousStepIds, String caller) {
		this.id = id;
		this.entryId = entryId;
		this.stepId = stepId;
		this.actionId = actionId;
		this.owner = owner;
		this.startDate = startDate;
		this.finishDate = finishDate;
		this.dueDate = dueDate;
		this.status = status;
		this.previousStepIds = previousStepIds;
		this.caller = caller;
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

	public Date getDueDate() {
		return dueDate;
	}

	public void setEntryId(long entryId) {
		this.entryId = entryId;
	}

	public long getEntryId() {
		return entryId;
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

	public void setPreviousStepIds(long[] previousStepIds) {
		this.previousStepIds = previousStepIds;
	}

	public long[] getPreviousStepIds() {
		return previousStepIds;
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

	public String toString() {
		return "SimpleStep@" + stepId + "[owner=" + owner + ", actionId=" + actionId + ", status=" + status + "]";
	}
}
