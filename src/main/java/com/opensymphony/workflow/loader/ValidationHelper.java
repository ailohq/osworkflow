/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.loader;

import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.util.Validatable;

import java.util.Collection;
import java.util.Iterator;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.2 $
 */
public class ValidationHelper {
	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public static void validate(Collection c) throws InvalidWorkflowDescriptorException {
		Iterator iter = c.iterator();

		while (iter.hasNext()) {
			Object o = iter.next();

			if (o instanceof Validatable) {
				((Validatable) o).validate();
			}
		}
	}
}
