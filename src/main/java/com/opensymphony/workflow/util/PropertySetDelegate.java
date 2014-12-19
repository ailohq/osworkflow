/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util;

import com.opensymphony.module.propertyset.PropertySet;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.1 $
 */
public interface PropertySetDelegate {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	PropertySet getPropertySet(long entryId);
}
