/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.loader;

import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.util.Validatable;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;

import java.util.*;

/**
 * @author <a href="mailto:plightbo@hotmail.com">Pat Lightbody</a>
 */
public class StepDescriptor extends AbstractDescriptor implements Validatable {
	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	protected List actions = new ArrayList();

	/**
	 * this list maintained internally to allow for proper xml serialization.
	 * All common-action elements in the XML file are expanded into
	 * ActionDescriptors and are available via getActions()
	 */
	protected List commonActions = new ArrayList();
	protected List permissions = new ArrayList();
	protected List postFunctions = new ArrayList();
	protected List preFunctions = new ArrayList();
	protected Map metaAttributes = new HashMap();
	protected String name;
	protected boolean hasActions = false;

	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	/**
	 * @deprecated use {@link DescriptorFactory} instead
	 */
	StepDescriptor() {
	}

	/**
	 * @deprecated use {@link DescriptorFactory} instead
	 */
	StepDescriptor(Element step) {
		init(step);
	}

	/** sets parent */
	StepDescriptor(Element step, AbstractDescriptor parent) {
		setParent(parent);
		init(step);
	}

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public ActionDescriptor getAction(int id) {
		for (Iterator iterator = actions.iterator(); iterator.hasNext();) {
			ActionDescriptor action = (ActionDescriptor) iterator.next();

			if (action.getId() == id) {
				return action;
			}
		}

		return null;
	}

	/**
	 * Get a List of {@link ActionDescriptor}s for this step
	 */
	public List getActions() {
		return actions;
	}

	/**
	 * Get a list of common actions.
	 * 
	 * @return a List of Integer action id's.
	 */
	public List getCommonActions() {
		return commonActions;
	}

	public void setMetaAttributes(Map metaAttributes) {
		this.metaAttributes = metaAttributes;
	}

	public Map getMetaAttributes() {
		return metaAttributes;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	 * Get a List of {@link PermissionDescriptor}s for this step
	 */
	public List getPermissions() {
		return permissions;
	}

	public void setPostFunctions(List postFunctions) {
		this.postFunctions = postFunctions;
	}

	public List getPostFunctions() {
		return postFunctions;
	}

	public void setPreFunctions(List preFunctions) {
		this.preFunctions = preFunctions;
	}

	public List getPreFunctions() {
		return preFunctions;
	}

	/**
	 * Remove all common and regular actions for this step.
	 */
	public void removeActions() {
		commonActions.clear();
		actions.clear();
		hasActions = false;
	}

	public boolean resultsInJoin(int join) {
		for (Iterator iterator = actions.iterator(); iterator.hasNext();) {
			ActionDescriptor actionDescriptor = (ActionDescriptor) iterator.next();

			if (actionDescriptor.getUnconditionalResult().getJoin() == join) {
				return true;
			}

			List results = actionDescriptor.getConditionalResults();

			for (Iterator iterator2 = results.iterator(); iterator2.hasNext();) {
				ConditionalResultDescriptor resultDescriptor = (ConditionalResultDescriptor) iterator2.next();

				if (resultDescriptor.getJoin() == join) {
					return true;
				}
			}
		}

		return false;
	}

	public void validate() throws InvalidWorkflowDescriptorException {
		if ((commonActions.size() == 0) && (actions.size() == 0) && hasActions) {
			throw new InvalidWorkflowDescriptorException("Step '" + name + "' actions element must contain at least one action or common-action");
		}

		if (getId() == -1) {
			throw new InvalidWorkflowDescriptorException("Cannot use a step ID of -1 as it is a reserved value");
		}

		ValidationHelper.validate(actions);
		ValidationHelper.validate(permissions);
		ValidationHelper.validate(preFunctions);
		ValidationHelper.validate(postFunctions);

		Iterator iter = commonActions.iterator();

		while (iter.hasNext()) {
			Object o = iter.next();

			try {
				Integer actionId = new Integer(o.toString());
				ActionDescriptor commonActionReference = (ActionDescriptor) ((WorkflowDescriptor) getParent()).getCommonActions().get(actionId);

				if (commonActionReference == null) {
					throw new InvalidWorkflowDescriptorException("Common action " + actionId + " specified in step " + getName() + " does not exist");
				}
			} catch (NumberFormatException ex) {
				throw new InvalidWorkflowDescriptorException("Common action " + o + " is not a valid action ID");
			}
		}
	}

	public void writeXML(PrintWriter out, int indent) {
		XMLUtil.printIndent(out, indent++);
		out.print("<step id=\"" + getId() + "\"");

		if ((name != null) && (name.length() > 0)) {
			out.print(" name=\"" + XMLUtil.encode(name) + "\"");
		}

		out.println(">");

		Iterator iter = metaAttributes.entrySet().iterator();

		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			XMLUtil.printIndent(out, indent);
			out.print("<meta name=\"");
			out.print(entry.getKey());
			out.print("\">");
			out.print(entry.getValue());
			out.println("</meta>");
		}

		if (preFunctions.size() > 0) {
			XMLUtil.printIndent(out, indent++);
			out.println("<pre-functions>");

			for (int i = 0; i < preFunctions.size(); i++) {
				FunctionDescriptor function = (FunctionDescriptor) preFunctions.get(i);
				function.writeXML(out, indent);
			}

			XMLUtil.printIndent(out, --indent);
			out.println("</pre-functions>");
		}

		if (permissions.size() > 0) {
			XMLUtil.printIndent(out, indent++);
			out.println("<external-permissions>");

			for (int i = 0; i < permissions.size(); i++) {
				PermissionDescriptor permission = (PermissionDescriptor) permissions.get(i);
				permission.writeXML(out, indent);
			}

			XMLUtil.printIndent(out, --indent);
			out.println("</external-permissions>");
		}

		if ((actions.size() > 0) || (commonActions.size() > 0)) {
			XMLUtil.printIndent(out, indent++);
			out.println("<actions>");

			// special serialization common-action elements
			for (int i = 0; i < commonActions.size(); i++) {
				out.println("<common-action id=\"" + commonActions.get(i) + "\" />");
			}

			for (int i = 0; i < actions.size(); i++) {
				ActionDescriptor action = (ActionDescriptor) actions.get(i);

				if (!action.isCommon()) {
					action.writeXML(out, indent);
				}
			}

			XMLUtil.printIndent(out, --indent);
			out.println("</actions>");
		}

		if (postFunctions.size() > 0) {
			XMLUtil.printIndent(out, indent++);
			out.println("<post-functions>");

			for (int i = 0; i < postFunctions.size(); i++) {
				FunctionDescriptor function = (FunctionDescriptor) postFunctions.get(i);
				function.writeXML(out, indent);
			}

			XMLUtil.printIndent(out, --indent);
			out.println("</post-functions>");
		}

		XMLUtil.printIndent(out, --indent);
		out.println("</step>");
	}

	protected void init(Element step) {
		try {
			setId(Integer.parseInt(step.getAttribute("id")));
		} catch (Exception ex) {
			throw new IllegalArgumentException("Invalid step id value " + step.getAttribute("id"));
		}

		name = step.getAttribute("name");

		NodeList children = step.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if (child.getNodeName().equals("meta")) {
				Element meta = (Element) child;
				String value = XMLUtil.getText(meta);
				this.metaAttributes.put(meta.getAttribute("name"), value);
			}
		}

		// set up pre-functions - OPTIONAL
		Element pre = XMLUtil.getChildElement(step, "pre-functions");

		if (pre != null) {
			List preFunctions = XMLUtil.getChildElements(pre, "function");

			for (int k = 0; k < preFunctions.size(); k++) {
				Element preFunction = (Element) preFunctions.get(k);
				FunctionDescriptor functionDescriptor = DescriptorFactory.getFactory().createFunctionDescriptor(preFunction);
				functionDescriptor.setParent(this);
				this.preFunctions.add(functionDescriptor);
			}
		}

		// set up permissions - OPTIONAL
		Element p = XMLUtil.getChildElement(step, "external-permissions");

		if (p != null) {
			List permissions = XMLUtil.getChildElements(p, "permission");

			for (int i = 0; i < permissions.size(); i++) {
				Element permission = (Element) permissions.get(i);
				PermissionDescriptor permissionDescriptor = DescriptorFactory.getFactory().createPermissionDescriptor(permission);
				permissionDescriptor.setParent(this);
				this.permissions.add(permissionDescriptor);
			}
		}

		// set up actions - OPTIONAL
		Element a = XMLUtil.getChildElement(step, "actions");

		if (a != null) {
			hasActions = true;

			List actions = XMLUtil.getChildElements(a, "action");

			for (int i = 0; i < actions.size(); i++) {
				Element action = (Element) actions.get(i);
				ActionDescriptor actionDescriptor = DescriptorFactory.getFactory().createActionDescriptor(action);
				actionDescriptor.setParent(this);
				this.actions.add(actionDescriptor);
			}

			// look for common-action elements
			List commonActions = XMLUtil.getChildElements(a, "common-action");

			for (int i = 0; i < commonActions.size(); i++) {
				Element commonAction = (Element) commonActions.get(i);

				WorkflowDescriptor workflowDescriptor = (WorkflowDescriptor) getParent();

				try {
					Integer actionId = new Integer(commonAction.getAttribute("id"));

					ActionDescriptor commonActionReference = (ActionDescriptor) workflowDescriptor.getCommonActions().get(actionId);

					if (commonActionReference != null) {
						this.actions.add(commonActionReference);
					}

					this.commonActions.add(actionId);
				} catch (Exception ex) {
					// log.warn("Invalid common actionId:" + ex);
				}
			}
		}

		// set up post-functions - OPTIONAL
		Element post = XMLUtil.getChildElement(step, "post-functions");

		if (post != null) {
			List postFunctions = XMLUtil.getChildElements(post, "function");

			for (int k = 0; k < postFunctions.size(); k++) {
				Element postFunction = (Element) postFunctions.get(k);
				FunctionDescriptor functionDescriptor = DescriptorFactory.getFactory().createFunctionDescriptor(postFunction);
				functionDescriptor.setParent(this);
				this.postFunctions.add(functionDescriptor);
			}
		}
	}
}
