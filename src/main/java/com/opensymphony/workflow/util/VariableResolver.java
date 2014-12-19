/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util;

import com.opensymphony.module.propertyset.PropertySet;

import java.util.Map;

/**
 * @author hani Date: Mar 29, 2005 Time: 8:17:31 PM
 */
public interface VariableResolver {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	Object translateVariables(String s, Map transientVars, PropertySet ps);
}
