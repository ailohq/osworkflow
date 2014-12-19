/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.loader;

import org.w3c.dom.Element;

import java.io.PrintWriter;

import java.util.*;

/**
 * Desrives a function that can be applied to a workflow step.
 * 
 * @author <a href="mailto:plightbo@hotmail.com">Pat Lightbody</a>
 */
public class FunctionDescriptor extends AbstractDescriptor {
	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	protected Map args = new HashMap();

	/**
	 * The name field helps the editor identify the condition template used.
	 */
	protected String name;
	protected String type;

	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	/**
	 * @deprecated use {@link DescriptorFactory} instead
	 */
	FunctionDescriptor() {
	}

	/**
	 * @deprecated use {@link DescriptorFactory} instead
	 */
	FunctionDescriptor(Element function) {
		init(function);
	}

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public Map getArgs() {
		return args;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void writeXML(PrintWriter out, int indent) {
		XMLUtil.printIndent(out, indent++);
		out.println("<function " + (hasId() ? ("id=\"" + getId() + "\" ") : "") + (((name != null) && (name.length() > 0)) ? ("name=\"" + XMLUtil.encode(getName()) + "\" ") : "") + "type=\"" + type
				+ "\">");

		Iterator iter = args.entrySet().iterator();

		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			XMLUtil.printIndent(out, indent);
			out.print("<arg name=\"");
			out.print(entry.getKey());
			out.print("\">");

			if ("beanshell".equals(type) || "bsf".equals(type)) {
				out.print("<![CDATA[");
				out.print(entry.getValue());
				out.print("]]>");
			} else {
				out.print(XMLUtil.encode(entry.getValue()));
			}

			out.println("</arg>");
		}

		XMLUtil.printIndent(out, --indent);
		out.println("</function>");
	}

	protected void init(Element function) {
		type = function.getAttribute("type");

		try {
			setId(Integer.parseInt(function.getAttribute("id")));
		} catch (NumberFormatException e) {
		}

		if (function.getAttribute("name") != null) {
			name = function.getAttribute("name");
		}

		List args = XMLUtil.getChildElements(function, "arg");

		for (int l = 0; l < args.size(); l++) {
			Element arg = (Element) args.get(l);
			String value = XMLUtil.getText(arg);

			this.args.put(arg.getAttribute("name"), value);
		}
	}
}
