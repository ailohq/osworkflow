/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.query;

/**
 * Field expressions are used when constructing a workflow query on the fields
 * of persistent workflow instances like (START_DATE, OWNER,....). Field
 * expressions have three attributes. These are: <li>operator: This is the
 * operator to apply on the expression. <li>field: The workflow field to test
 * agains <li>Context: The context to search in, which can be one history,
 * current steps, or a workflow instance.
 * 
 * @author Christine Zimmermann
 */
public class FieldExpression extends Expression {
	// ~ Static fields/initializers
	// /////////////////////////////////////////////

	// field operators

	/**
	 * Constant for the equality operator.
	 */
	public final static int EQUALS = 1;

	/**
	 * Constant for the less than operator.
	 */
	public final static int LT = 2;

	/**
	 * Constant for the greater than operator.
	 */
	public final static int GT = 3;

	/**
	 * Constant for the not equals operator.
	 */
	public final static int NOT_EQUALS = 5;

	// fields

	/**
	 * Constant for the workflow owner field.
	 */
	public final static int OWNER = 1;

	/**
	 * Constant for the workflow start date field.
	 */
	public final static int START_DATE = 2;

	/**
	 * Constant for the workflow finish date field.
	 */
	public final static int FINISH_DATE = 3;

	/**
	 * Constant for the workflow action field.
	 */
	public final static int ACTION = 4;

	/**
	 * Constant for the workflow step field.
	 */
	public final static int STEP = 5;

	/**
	 * Constant for the workflow caller field.
	 */
	public final static int CALLER = 6;

	/**
	 * Constant for the workflow status field.
	 */
	public final static int STATUS = 7;

	/**
	 * Constant for the workflow name field.
	 */
	public final static int NAME = 8;

	/**
	 * Constant for the state field.
	 */
	public final static int STATE = 9;

	/**
	 * Constant for the workflow due date field.
	 */
	public final static int DUE_DATE = 10;

	// field context

	/**
	 * Constant for the history steps context. Specifying this context means
	 * that the search should be performed against the workflow steps.
	 */
	public final static int HISTORY_STEPS = 1;

	/**
	 * Constant for the history steps context. Specifying this context means
	 * that the search should be performed against the workflow current steps.
	 */
	public final static int CURRENT_STEPS = 2;

	/**
	 * Constant for the workflow entry context. Specifying this context means
	 * that the search should be performed against the workflow entries.
	 */
	public final static int ENTRY = 3;

	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	private Object value;
	private int context;
	private int field;
	private int operator;

	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	public FieldExpression() {
	}

	public FieldExpression(int field, int context, int operator, Object value) {
		this.context = context;
		this.operator = operator;
		this.field = field;
		this.value = value;
	}

	public FieldExpression(int field, int context, int operator, Object value, boolean negate) {
		this(field, context, operator, value);
		super.negate = negate;
	}

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public void setContext(int context) {
		this.context = context;
	}

	public int getContext() {
		return context;
	}

	public void setField(int field) {
		this.field = field;
	}

	public int getField() {
		return field;
	}

	public boolean isNested() {
		return false;
	}

	public void setOperator(int operator) {
		this.operator = operator;
	}

	public int getOperator() {
		return operator;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}
}
