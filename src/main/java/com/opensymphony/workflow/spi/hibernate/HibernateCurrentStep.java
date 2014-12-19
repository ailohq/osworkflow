/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.spi.hibernate;

/**
 * This class exists to seperate the persistence of the Steps. By seperating out
 * the Current Step from the Previous �* Step classes, they can be easily
 * written into seperate tables. �* @see {@link HibernateHistoryStep}
 */
public class HibernateCurrentStep extends HibernateStep {
	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	public HibernateCurrentStep() {
	}

	public HibernateCurrentStep(HibernateStep step) {
		super(step);
	}
}
