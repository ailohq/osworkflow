/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util;

import com.opensymphony.module.propertyset.PropertySet;

import com.opensymphony.provider.BeanProvider;
import com.opensymphony.provider.bean.DefaultBeanProvider;

import java.io.Serializable;

import java.util.Map;

/**
 * @author Hani Suleiman (hani@formicary.net) Date: Oct 14, 2003 Time: 11:58:12
 *         PM
 */
public class DefaultVariableResolver implements VariableResolver, Serializable {
	// ~ Static fields/initializers
	// /////////////////////////////////////////////

	private static final long serialVersionUID = -4819078273560683753L;

	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	private transient BeanProvider beanProvider = null;

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public void setBeanProvider(BeanProvider beanProvider) {
		this.beanProvider = beanProvider;
	}

	public BeanProvider getBeanProvider() {
		return beanProvider;
	}

	public Object getVariableFromMaps(String var, Map transientVars, PropertySet ps) {
		int firstDot = var.indexOf('.');
		String actualVar = var;

		if (firstDot != -1) {
			actualVar = var.substring(0, firstDot);
		}

		Object o = transientVars.get(actualVar);

		if (o == null) {
			o = ps.getAsActualType(actualVar);
		}

		if (firstDot != -1) {
			if (beanProvider == null) {
				beanProvider = new DefaultBeanProvider();
			}

			o = beanProvider.getProperty(o, var.substring(firstDot + 1));
		}

		return o;
	}

	/**
	 * Parses a string for instances of "${foo}" and returns a string with all
	 * instances replaced with the string value of the foo object
	 * (<b>foo.toString()</b>). If the string being passed in only refers to a
	 * single variable and contains no other characters (for example: ${foo}),
	 * then the actual object is returned instead of converting it to a string.
	 */
	public Object translateVariables(String s, Map transientVars, PropertySet ps) {
		String temp = s.trim();

		if (temp.startsWith("${") && temp.endsWith("}") && (temp.indexOf('$', 1) == -1)) {
			// the string is just a variable reference, don't convert it to a
			// string
			String var = temp.substring(2, temp.length() - 1);

			return getVariableFromMaps(var, transientVars, ps);
		} else {
			// the string passed in contains multiple variables (or none!) and
			// should be treated as a string
			while (true) {
				int x = s.indexOf("${");
				int y = s.indexOf("}", x);

				if ((x != -1) && (y != -1)) {
					String var = s.substring(x + 2, y);
					String t = null;
					Object o = getVariableFromMaps(var, transientVars, ps);

					if (o != null) {
						t = o.toString();
					}

					if (t != null) {
						s = s.substring(0, x) + t + s.substring(y + 1);
					} else {
						// the variable doesn't exist, so don't display anything
						s = s.substring(0, x) + s.substring(y + 1);
					}
				} else {
					break;
				}
			}

			return s;
		}
	}
}
