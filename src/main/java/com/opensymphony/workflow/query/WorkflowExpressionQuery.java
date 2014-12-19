/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.query;

import java.io.Serializable;

/**
 * Workflow Query. A workflow expression-based query is constructed by
 * specifying a number of expressions in the query. Currently queries can only
 * have one operator act on them. Either the expressions are either evaluated
 * with an OR, whereby the first expression that passes results in inclusion of
 * a result, or with an AND, whereby all expressions must return true for a
 * result to be included.
 * 
 * @author Christine Zimmermann
 */
public class WorkflowExpressionQuery implements Serializable {
	// ~ Static fields/initializers
	// /////////////////////////////////////////////

	private static final long serialVersionUID = 5810528106491875046L;
	public static final int SORT_NONE = 0;
	public static final int SORT_ASC = 1;
	public static final int SORT_DESC = -1;

	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	private Expression expression = null;
	private int orderBy;
	private int sortOrder;

	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	public WorkflowExpressionQuery() {
	}

	/**
	 * Create a WorkflowExpressionQuery that consists of one expression.
	 */
	public WorkflowExpressionQuery(Expression expression) {
		this.expression = expression;
	}

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	public Expression getExpression() {
		return expression;
	}

	public void setOrderBy(int orderBy) {
		this.orderBy = orderBy;
	}

	public int getOrderBy() {
		return orderBy;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public int getSortOrder() {
		return sortOrder;
	}
}
