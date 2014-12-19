/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

import java.util.HashMap;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.2 $
 */
public class PropertySetDelegateImpl implements PropertySetDelegate {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public PropertySet getPropertySet(long entryId) {
		return PropertySetManager.getInstance("memory", new HashMap());
	}
}
