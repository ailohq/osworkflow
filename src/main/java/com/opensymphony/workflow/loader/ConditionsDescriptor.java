/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.loader;

import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.util.Validatable;
import com.opensymphony.workflow.util.XMLizable;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: May 12, 2004 Time: 9:04:25 AM
 * 
 * @author hani
 */
public class ConditionsDescriptor extends AbstractDescriptor implements Validatable {
	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	private List conditions = new ArrayList();
	private String type;

	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	/**
	 * @deprecated use {@link DescriptorFactory} instead
	 */
	ConditionsDescriptor() {
	}

	/**
	 * @deprecated use {@link DescriptorFactory} instead
	 */
	ConditionsDescriptor(Element element) {
		type = element.getAttribute("type");

		NodeList children = element.getChildNodes();
		int size = children.getLength();

		for (int i = 0; i < size; i++) {
			Node child = children.item(i);

			if (child instanceof Element) {
				if ("condition".equals(child.getNodeName())) {
					ConditionDescriptor condition = DescriptorFactory.getFactory().createConditionDescriptor((Element) child);
					conditions.add(condition);
				} else if ("conditions".equals(child.getNodeName())) {
					ConditionsDescriptor condition = DescriptorFactory.getFactory().createConditionsDescriptor((Element) child);
					conditions.add(condition);
				}
			}
		}
	}

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public void setConditions(List conditions) {
		this.conditions = conditions;
	}

	public List getConditions() {
		return conditions;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void validate() throws InvalidWorkflowDescriptorException {
		ValidationHelper.validate(conditions);

		if (conditions.size() == 0) {
			AbstractDescriptor desc = getParent();

			if ((desc != null) && (desc instanceof ConditionalResultDescriptor)) {
				throw new InvalidWorkflowDescriptorException("Conditional result from " + ((ActionDescriptor) desc.getParent()).getName() + " to "
						+ ((ConditionalResultDescriptor) desc).getDestination() + " must have at least one condition");
			}
		}

		if ((conditions.size() > 1) && (type == null)) {
			throw new InvalidWorkflowDescriptorException("Conditions must have AND or OR type specified");
		}
	}

	public void writeXML(PrintWriter out, int indent) {
		if (conditions.size() > 0) {
			XMLUtil.printIndent(out, indent++);

			StringBuffer sb = new StringBuffer("<conditions");

			if (conditions.size() > 1) {
				sb.append(" type=\"").append(type).append('\"');
			}

			sb.append('>');
			out.println(sb.toString());

			for (int i = 0; i < conditions.size(); i++) {
				XMLizable condition = (XMLizable) conditions.get(i);
				condition.writeXML(out, indent);
			}

			XMLUtil.printIndent(out, --indent);
			out.println("</conditions>");
		}
	}
}
