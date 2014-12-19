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
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

/**
 * Describes a single workflow
 * 
 * @author <a href="mailto:plightbo@hotmail.com">Pat Lightbody</a>
 */
public class WorkflowDescriptor extends AbstractDescriptor implements Validatable {
	// ~ Static fields/initializers
	// /////////////////////////////////////////////

	public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	public static final String DOCTYPE_DECL = "<!DOCTYPE workflow PUBLIC \"-//OpenSymphony Group//DTD OSWorkflow 2.6//EN\" \"dtds/workflow_2_8.dtd\">";

	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	protected ConditionsDescriptor globalConditions = null;
	protected List commonActionsList = new ArrayList(); // for preserving order
	protected List globalActions = new ArrayList();
	protected List initialActions = new ArrayList();
	protected List joins = new ArrayList();
	protected List registers = new ArrayList();
	protected List splits = new ArrayList();
	protected List steps = new ArrayList();
	protected Map commonActions = new HashMap();
	protected Map metaAttributes = new HashMap();
	protected Map timerFunctions = new HashMap();
	protected String workflowName = null;

	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	/**
	 * @deprecated use {@link DescriptorFactory} instead
	 */
	public WorkflowDescriptor() {
	}

	/**
	 * @deprecated use {@link DescriptorFactory} instead
	 */
	public WorkflowDescriptor(Element root) {
		init(root);
	}

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public ActionDescriptor getAction(int id) {
		// check global actions
		for (Iterator iterator = globalActions.iterator(); iterator.hasNext();) {
			ActionDescriptor actionDescriptor = (ActionDescriptor) iterator.next();

			if (actionDescriptor.getId() == id) {
				return actionDescriptor;
			}
		}

		// check steps
		for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
			StepDescriptor stepDescriptor = (StepDescriptor) iterator.next();
			ActionDescriptor actionDescriptor = stepDescriptor.getAction(id);

			if (actionDescriptor != null) {
				return actionDescriptor;
			}
		}

		// check initial actions, which we now must have unique id's
		for (Iterator iterator = initialActions.iterator(); iterator.hasNext();) {
			ActionDescriptor actionDescriptor = (ActionDescriptor) iterator.next();

			if (actionDescriptor.getId() == id) {
				return actionDescriptor;
			}
		}

		return null;
	}

	/**
	 * Get a Map of the common actions specified, keyed on actionId (an Integer)
	 * 
	 * @return A list of {@link ActionDescriptor} objects
	 */
	public Map getCommonActions() {
		return commonActions;
	}

	/**
	 * Get a List of the global actions specified
	 * 
	 * @return A list of {@link ActionDescriptor} objects
	 */
	public List getGlobalActions() {
		return globalActions;
	}

	public ConditionsDescriptor getGlobalConditions() {
		return globalConditions;
	}

	public ActionDescriptor getInitialAction(int id) {
		for (Iterator iterator = initialActions.iterator(); iterator.hasNext();) {
			ActionDescriptor actionDescriptor = (ActionDescriptor) iterator.next();

			if (actionDescriptor.getId() == id) {
				return actionDescriptor;
			}
		}

		return null;
	}

	/**
	 * Get a List of initial steps for this workflow
	 * 
	 * @return A list of {@link ActionDescriptor} objects
	 */
	public List getInitialActions() {
		return initialActions;
	}

	public JoinDescriptor getJoin(int id) {
		for (Iterator iterator = joins.iterator(); iterator.hasNext();) {
			JoinDescriptor joinDescriptor = (JoinDescriptor) iterator.next();

			if (joinDescriptor.getId() == id) {
				return joinDescriptor;
			}
		}

		return null;
	}

	/**
	 * Get a List of initial steps for this workflow
	 * 
	 * @return A list of {@link JoinDescriptor} objects
	 */
	public List getJoins() {
		return joins;
	}

	public Map getMetaAttributes() {
		return metaAttributes;
	}

	public void setName(String name) {
		workflowName = name;
	}

	public String getName() {
		return workflowName;
	}

	public List getRegisters() {
		return registers;
	}

	public SplitDescriptor getSplit(int id) {
		for (Iterator iterator = splits.iterator(); iterator.hasNext();) {
			SplitDescriptor splitDescriptor = (SplitDescriptor) iterator.next();

			if (splitDescriptor.getId() == id) {
				return splitDescriptor;
			}
		}

		return null;
	}

	/**
	 * Get a List of initial steps for this workflow
	 * 
	 * @return A list of {@link SplitDescriptor} objects
	 */
	public List getSplits() {
		return splits;
	}

	public StepDescriptor getStep(int id) {
		for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
			StepDescriptor step = (StepDescriptor) iterator.next();

			if (step.getId() == id) {
				return step;
			}
		}

		return null;
	}

	/**
	 * Get a List of steps in this workflow
	 * 
	 * @return a List of {@link StepDescriptor} objects
	 */
	public List getSteps() {
		return steps;
	}

	/**
	 * Update a trigger function
	 * 
	 * @param id
	 *            The id for the trigger function
	 * @param descriptor
	 *            The descriptor for the trigger function
	 * @return The old trigger function with the specified ID, if any existed
	 */
	public FunctionDescriptor setTriggerFunction(int id, FunctionDescriptor descriptor) {
		return (FunctionDescriptor) timerFunctions.put(new Integer(id), descriptor);
	}

	public FunctionDescriptor getTriggerFunction(int id) {
		return (FunctionDescriptor) this.timerFunctions.get(new Integer(id));
	}

	/**
	 * Get a Map of all trigger functions in this workflow
	 * 
	 * @return a Map with Integer keys and {@link FunctionDescriptor} values
	 */
	public Map getTriggerFunctions() {
		return timerFunctions;
	}

	/**
	 * Add a common action
	 * 
	 * @param descriptor
	 *            The action descriptor to add
	 * @throws IllegalArgumentException
	 *             if the descriptor's ID already exists in the workflow
	 */
	public void addCommonAction(ActionDescriptor descriptor) {
		descriptor.setCommon(true);
		addAction(commonActions, descriptor);
		addAction(commonActionsList, descriptor);
	}

	/**
	 * Add a global action
	 * 
	 * @param descriptor
	 *            The action descriptor to add
	 * @throws IllegalArgumentException
	 *             if the descriptor's ID already exists in the workflow
	 */
	public void addGlobalAction(ActionDescriptor descriptor) {
		addAction(globalActions, descriptor);
	}

	/**
	 * Add an initial action
	 * 
	 * @param descriptor
	 *            The action descriptor to add
	 * @throws IllegalArgumentException
	 *             if the descriptor's ID already exists in the workflow
	 */
	public void addInitialAction(ActionDescriptor descriptor) {
		addAction(initialActions, descriptor);
	}

	/**
	 * Add a join
	 * 
	 * @param descriptor
	 *            The join descriptor to add
	 * @throws IllegalArgumentException
	 *             if the descriptor's ID already exists in the workflow
	 */
	public void addJoin(JoinDescriptor descriptor) {
		if (getJoin(descriptor.getId()) != null) {
			throw new IllegalArgumentException("Join with id " + descriptor.getId() + " already exists");
		}

		joins.add(descriptor);
	}

	/**
	 * Add a split
	 * 
	 * @param descriptor
	 *            The split descriptor to add
	 * @throws IllegalArgumentException
	 *             if the descriptor's ID already exists in the workflow
	 */
	public void addSplit(SplitDescriptor descriptor) {
		if (getSplit(descriptor.getId()) != null) {
			throw new IllegalArgumentException("Split with id " + descriptor.getId() + " already exists");
		}

		splits.add(descriptor);
	}

	/**
	 * Add a step
	 * 
	 * @param descriptor
	 *            The step descriptor to add
	 * @throws IllegalArgumentException
	 *             if the descriptor's ID already exists in the workflow
	 */
	public void addStep(StepDescriptor descriptor) {
		if (getStep(descriptor.getId()) != null) {
			throw new IllegalArgumentException("Step with id " + descriptor.getId() + " already exists");
		}

		steps.add(descriptor);
	}

	/**
	 * Remove an action from this workflow completely.
	 * <p>
	 * This method will check global actions and all steps.
	 * 
	 * @return true if the action was successfully removed, false if it was not
	 *         found
	 */
	public boolean removeAction(ActionDescriptor actionToRemove) {
		// global actions
		for (Iterator iterator = getGlobalActions().iterator(); iterator.hasNext();) {
			ActionDescriptor actionDescriptor = (ActionDescriptor) iterator.next();

			if (actionDescriptor.getId() == actionToRemove.getId()) {
				getGlobalActions().remove(actionDescriptor);

				return true;
			}
		}

		// steps
		for (Iterator iterator = getSteps().iterator(); iterator.hasNext();) {
			StepDescriptor stepDescriptor = (StepDescriptor) iterator.next();
			ActionDescriptor actionDescriptor = stepDescriptor.getAction(actionToRemove.getId());

			if (actionDescriptor != null) {
				stepDescriptor.getActions().remove(actionDescriptor);

				return true;
			}
		}

		return false;
	}

	public void validate() throws InvalidWorkflowDescriptorException {
		ValidationHelper.validate(this.getRegisters());
		ValidationHelper.validate(this.getTriggerFunctions().values());
		ValidationHelper.validate(this.getGlobalActions());
		ValidationHelper.validate(this.getInitialActions());
		ValidationHelper.validate(this.getCommonActions().values());
		ValidationHelper.validate(this.getSteps());
		ValidationHelper.validate(this.getSplits());
		ValidationHelper.validate(this.getJoins());

		Set actions = new HashSet();
		Iterator i = globalActions.iterator();

		while (i.hasNext()) {
			ActionDescriptor action = (ActionDescriptor) i.next();
			actions.add(new Integer(action.getId()));
		}

		i = getSteps().iterator();

		while (i.hasNext()) {
			StepDescriptor step = (StepDescriptor) i.next();
			Iterator j = step.getActions().iterator();

			while (j.hasNext()) {
				ActionDescriptor action = (ActionDescriptor) j.next();

				// check to see if it's a common action (dups are ok, since
				// that's the point of common actions!)
				if (!action.isCommon()) {
					if (!actions.add(new Integer(action.getId()))) {
						throw new InvalidWorkflowDescriptorException("Duplicate occurance of action ID " + action.getId() + " found in step " + step.getId());
					}
				}
			}
		}

		// now we have all our unique actions, let's check that no common action
		// id's exist in them
		i = commonActions.keySet().iterator();

		while (i.hasNext()) {
			Integer action = (Integer) i.next();

			if (actions.contains(action)) {
				throw new InvalidWorkflowDescriptorException("common-action ID " + action + " is duplicated in a step action");
			}
		}

		i = initialActions.iterator();

		while (i.hasNext()) {
			ActionDescriptor action = (ActionDescriptor) i.next();

			if (actions.contains(new Integer(action.getId()))) {
				throw new InvalidWorkflowDescriptorException("initial-action ID " + action + " is duplicated in a step action");
			}
		}

		validateDTD();
	}

	public void writeXML(PrintWriter out, int indent) {
		XMLUtil.printIndent(out, indent++);
		out.println("<workflow>");

		Iterator iter = metaAttributes.entrySet().iterator();

		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			XMLUtil.printIndent(out, indent);
			out.print("<meta name=\"");
			out.print(XMLUtil.encode(entry.getKey()));
			out.print("\">");
			out.print(XMLUtil.encode(entry.getValue()));
			out.println("</meta>");
		}

		if (registers.size() > 0) {
			XMLUtil.printIndent(out, indent++);
			out.println("<registers>");

			for (int i = 0; i < registers.size(); i++) {
				RegisterDescriptor register = (RegisterDescriptor) registers.get(i);
				register.writeXML(out, indent);
			}

			XMLUtil.printIndent(out, --indent);
			out.println("</registers>");
		}

		if (timerFunctions.size() > 0) {
			XMLUtil.printIndent(out, indent++);
			out.println("<trigger-functions>");

			Iterator iterator = timerFunctions.entrySet().iterator();

			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				XMLUtil.printIndent(out, indent++);
				out.println("<trigger-function id=\"" + entry.getKey() + "\">");

				FunctionDescriptor trigger = (FunctionDescriptor) entry.getValue();
				trigger.writeXML(out, indent);
				XMLUtil.printIndent(out, --indent);
				out.println("</trigger-function>");
			}

			while (iterator.hasNext()) {
			}

			XMLUtil.printIndent(out, --indent);
			out.println("</trigger-functions>");
		}

		if (getGlobalConditions() != null) {
			XMLUtil.printIndent(out, indent++);
			out.println("<global-conditions>");

			getGlobalConditions().writeXML(out, indent);

			out.println("</global-conditions>");
		}

		XMLUtil.printIndent(out, indent++);
		out.println("<initial-actions>");

		for (int i = 0; i < initialActions.size(); i++) {
			ActionDescriptor action = (ActionDescriptor) initialActions.get(i);
			action.writeXML(out, indent);
		}

		XMLUtil.printIndent(out, --indent);
		out.println("</initial-actions>");

		if (globalActions.size() > 0) {
			XMLUtil.printIndent(out, indent++);
			out.println("<global-actions>");

			for (int i = 0; i < globalActions.size(); i++) {
				ActionDescriptor action = (ActionDescriptor) globalActions.get(i);
				action.writeXML(out, indent);
			}

			XMLUtil.printIndent(out, --indent);
			out.println("</global-actions>");
		}

		if (commonActions.size() > 0) {
			XMLUtil.printIndent(out, indent++);
			out.println("<common-actions>");

			Iterator iterator = getCommonActions().values().iterator();

			while (iterator.hasNext()) {
				ActionDescriptor action = (ActionDescriptor) iterator.next();
				action.writeXML(out, indent);
			}

			XMLUtil.printIndent(out, --indent);
			out.println("</common-actions>");
		}

		XMLUtil.printIndent(out, indent++);
		out.println("<steps>");

		for (int i = 0; i < steps.size(); i++) {
			StepDescriptor step = (StepDescriptor) steps.get(i);
			step.writeXML(out, indent);
		}

		XMLUtil.printIndent(out, --indent);
		out.println("</steps>");

		if (splits.size() > 0) {
			XMLUtil.printIndent(out, indent++);
			out.println("<splits>");

			for (int i = 0; i < splits.size(); i++) {
				SplitDescriptor split = (SplitDescriptor) splits.get(i);
				split.writeXML(out, indent);
			}

			XMLUtil.printIndent(out, --indent);
			out.println("</splits>");
		}

		if (joins.size() > 0) {
			XMLUtil.printIndent(out, indent++);
			out.println("<joins>");

			for (int i = 0; i < joins.size(); i++) {
				JoinDescriptor join = (JoinDescriptor) joins.get(i);
				join.writeXML(out, indent);
			}

			XMLUtil.printIndent(out, --indent);
			out.println("</joins>");
		}

		XMLUtil.printIndent(out, --indent);
		out.println("</workflow>");
	}

	protected void init(Element root) {
		NodeList children = root.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if (child.getNodeName().equals("meta")) {
				Element meta = (Element) child;
				String value = XMLUtil.getText(meta);
				this.metaAttributes.put(meta.getAttribute("name"), value);
			}
		}

		// handle registers - OPTIONAL
		Element r = XMLUtil.getChildElement(root, "registers");

		if (r != null) {
			List registers = XMLUtil.getChildElements(r, "register");

			for (int i = 0; i < registers.size(); i++) {
				Element register = (Element) registers.get(i);
				RegisterDescriptor registerDescriptor = DescriptorFactory.getFactory().createRegisterDescriptor(register);
				registerDescriptor.setParent(this);
				this.registers.add(registerDescriptor);
			}
		}

		// handle global-conditions - OPTIONAL
		Element globalConditionsElement = XMLUtil.getChildElement(root, "global-conditions");

		if (globalConditionsElement != null) {
			Element globalConditions = XMLUtil.getChildElement(globalConditionsElement, "conditions");

			ConditionsDescriptor conditionsDescriptor = DescriptorFactory.getFactory().createConditionsDescriptor(globalConditions);
			conditionsDescriptor.setParent(this);
			this.globalConditions = conditionsDescriptor;
		}

		// handle initial-steps - REQUIRED
		Element intialActionsElement = XMLUtil.getChildElement(root, "initial-actions");
		List initialActions = XMLUtil.getChildElements(intialActionsElement, "action");

		for (int i = 0; i < initialActions.size(); i++) {
			Element initialAction = (Element) initialActions.get(i);
			ActionDescriptor actionDescriptor = DescriptorFactory.getFactory().createActionDescriptor(initialAction);
			actionDescriptor.setParent(this);
			this.initialActions.add(actionDescriptor);
		}

		// handle global-actions - OPTIONAL
		Element globalActionsElement = XMLUtil.getChildElement(root, "global-actions");

		if (globalActionsElement != null) {
			List globalActions = XMLUtil.getChildElements(globalActionsElement, "action");

			for (int i = 0; i < globalActions.size(); i++) {
				Element globalAction = (Element) globalActions.get(i);
				ActionDescriptor actionDescriptor = DescriptorFactory.getFactory().createActionDescriptor(globalAction);
				actionDescriptor.setParent(this);
				this.globalActions.add(actionDescriptor);
			}
		}

		// handle common-actions - OPTIONAL
		// - Store actions in HashMap for now. When parsing Steps, we'll resolve
		// any common actions into local references.
		Element commonActionsElement = XMLUtil.getChildElement(root, "common-actions");

		if (commonActionsElement != null) {
			List commonActions = XMLUtil.getChildElements(commonActionsElement, "action");

			for (int i = 0; i < commonActions.size(); i++) {
				Element commonAction = (Element) commonActions.get(i);
				ActionDescriptor actionDescriptor = DescriptorFactory.getFactory().createActionDescriptor(commonAction);
				actionDescriptor.setParent(this);
				addCommonAction(actionDescriptor);
			}
		}

		// handle timer-functions - OPTIONAL
		Element timerFunctionsElement = XMLUtil.getChildElement(root, "trigger-functions");

		if (timerFunctionsElement != null) {
			List timerFunctions = XMLUtil.getChildElements(timerFunctionsElement, "trigger-function");

			for (int i = 0; i < timerFunctions.size(); i++) {
				Element timerFunction = (Element) timerFunctions.get(i);
				Integer id = new Integer(timerFunction.getAttribute("id"));
				FunctionDescriptor function = DescriptorFactory.getFactory().createFunctionDescriptor(XMLUtil.getChildElement(timerFunction, "function"));
				function.setParent(this);
				this.timerFunctions.put(id, function);
			}
		}

		// handle steps - REQUIRED
		Element stepsElement = XMLUtil.getChildElement(root, "steps");
		List steps = XMLUtil.getChildElements(stepsElement, "step");

		for (int i = 0; i < steps.size(); i++) {
			Element step = (Element) steps.get(i);
			StepDescriptor stepDescriptor = DescriptorFactory.getFactory().createStepDescriptor(step, this);
			this.steps.add(stepDescriptor);
		}

		// handle splits - OPTIONAL
		Element splitsElement = XMLUtil.getChildElement(root, "splits");

		if (splitsElement != null) {
			List split = XMLUtil.getChildElements(splitsElement, "split");

			for (int i = 0; i < split.size(); i++) {
				Element s = (Element) split.get(i);
				SplitDescriptor splitDescriptor = DescriptorFactory.getFactory().createSplitDescriptor(s);
				splitDescriptor.setParent(this);
				this.splits.add(splitDescriptor);
			}
		}

		// handle joins - OPTIONAL:
		Element joinsElement = XMLUtil.getChildElement(root, "joins");

		if (joinsElement != null) {
			List join = XMLUtil.getChildElements(joinsElement, "join");

			for (int i = 0; i < join.size(); i++) {
				Element s = (Element) join.get(i);
				JoinDescriptor joinDescriptor = DescriptorFactory.getFactory().createJoinDescriptor(s);
				joinDescriptor.setParent(this);
				this.joins.add(joinDescriptor);
			}
		}
	}

	// refactored this out from the three addAction methods above
	private void addAction(Object actionsCollectionOrMap, ActionDescriptor descriptor) {
		if (getAction(descriptor.getId()) != null) {
			throw new IllegalArgumentException("action with id " + descriptor.getId() + " already exists for this step.");
		}

		if (actionsCollectionOrMap instanceof Map) {
			((Map) actionsCollectionOrMap).put(new Integer(descriptor.getId()), descriptor);
		} else {
			((Collection) actionsCollectionOrMap).add(descriptor);
		}
	}

	private void validateDTD() throws InvalidWorkflowDescriptorException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setValidating(true);

		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		writer.println(XML_HEADER);
		writer.println(DOCTYPE_DECL);
		writeXML(writer, 0);

		WorkflowLoader.AllExceptionsErrorHandler errorHandler = new WorkflowLoader.AllExceptionsErrorHandler();

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setEntityResolver(new DTDEntityResolver());

			db.setErrorHandler(errorHandler);
			db.parse(new InputSource(new StringReader(sw.toString())));

			if (errorHandler.getExceptions().size() > 0) {
				throw new InvalidWorkflowDescriptorException(errorHandler.getExceptions().toString());
			}
		} catch (InvalidWorkflowDescriptorException e) {
			throw e;
		} catch (Exception e) {
			throw new InvalidWorkflowDescriptorException(e.toString());
		}
	}
}
