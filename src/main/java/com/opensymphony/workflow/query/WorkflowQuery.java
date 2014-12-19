/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.query;

import java.io.Serializable;

/**
 * @deprecated use {@link WorkflowExpressionQuery} instead
 * @author Hani
 */
public class WorkflowQuery implements Serializable {
	// ~ Static fields/initializers
	// /////////////////////////////////////////////

	private static final long serialVersionUID = 8130933224983412376L;
	public final static int EQUALS = 1;
	public final static int LT = 2;
	public final static int GT = 3;
	public final static int BETWEEN = 4;
	public final static int NOT_EQUALS = 5;
	public final static int AND = 6;
	public final static int OR = 7;
	public final static int XOR = 8;
	public final static int OWNER = 1;
	public final static int START_DATE = 2;
	public final static int FINISH_DATE = 3;
	public final static int ACTION = 4;
	public final static int STEP = 5;
	public final static int CALLER = 6;
	public final static int STATUS = 7;
	public final static int HISTORY = 1;
	public final static int CURRENT = 2;

	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	private Object value;
	private WorkflowQuery left;
	private WorkflowQuery right;
	private int field;
	private int operator;
	private int type;

	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	public WorkflowQuery() {
	}

	public WorkflowQuery(WorkflowQuery left, int operator, WorkflowQuery right) {
		this.operator = operator;
		this.left = left;
		this.right = right;
	}

	public WorkflowQuery(int field, int type, int operator, Object value) {
		this.type = type;
		this.operator = operator;
		this.field = field;
		this.value = value;
	}

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public int getField() {
		return field;
	}

	public WorkflowQuery getLeft() {
		return left;
	}

	public int getOperator() {
		return operator;
	}

	public WorkflowQuery getRight() {
		return right;
	}

	public int getType() {
		int qtype = type;

		if (qtype == 0) {
			if (getLeft() != null) {
				qtype = getLeft().getType();
			}
		}

		return qtype;
	}

	public Object getValue() {
		return value;
	}
}
