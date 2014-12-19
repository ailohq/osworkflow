/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.query;

import java.io.Serializable;

/**
 * Abstract base class for expressions used in a workflow query. Expressions can
 * be negate and/or nested. The default is not negate.
 * <p>
 * </p>
 * Expressions which are supported by all stores are {@link FieldExpression} and
 * {@link NestedExpression}.
 * <p>
 * </p>
 * Store specific expressions like XPathExpression can be added.
 * 
 * @author Christine Zimmermann
 */
public abstract class Expression implements Serializable {
	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	protected boolean negate = false;

	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	protected Expression() {
	}

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public boolean isNegate() {
		return negate;
	}

	abstract public boolean isNested();
}
