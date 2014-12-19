/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util;

import java.io.PrintWriter;
import java.io.Serializable;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.2 $
 */
public interface XMLizable extends Serializable {
	// ~ Static fields/initializers
	// /////////////////////////////////////////////

	public static final String INDENT = "  ";

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public void writeXML(PrintWriter writer, int indent);
}
