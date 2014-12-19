/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.spi.memory;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

import com.opensymphony.workflow.query.WorkflowQuery;
import com.opensymphony.workflow.spi.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

import java.util.*;

/**
 * Simple flat file implementation.
 * 
 * Following properties are <b>required</b>:
 * <ul>
 * <li><b>storeFile</b> - the absolute path to the store file
 * (<i>ex:c:\workflow.store</i>)</li>
 * </ul>
 * 
 * @author <a href="mailto:gbort@msn.com">Guillaume Bort</a>
 */
public class SerializableWorkflowStore extends MemoryWorkflowStore {
	// ~ Static fields/initializers
	// /////////////////////////////////////////////

	protected static final Log log = LogFactory.getLog(SerializableWorkflowStore.class);
	static String storeFile;

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public PropertySet getPropertySet(long entryId) {
		PropertySet ps = (PropertySet) SerializableCache.getInstance().propertySetCache.get(new Long(entryId));

		if (ps == null) {
			ps = PropertySetManager.getInstance("serializable", null);
			SerializableCache.getInstance().propertySetCache.put(new Long(entryId), ps);
		}

		return ps;
	}

	public static void setStoreFile(String storeFile) {
		SerializableWorkflowStore.storeFile = storeFile;
	}

	public static String getStoreFile() {
		return storeFile;
	}

	public Step createCurrentStep(long entryId, int stepId, String owner, Date startDate, Date dueDate, String status, long[] previousIds) {
		long id = SerializableCache.getInstance().globalStepId++;
		SimpleStep step = new SimpleStep(id, entryId, stepId, 0, owner, startDate, dueDate, null, status, previousIds, null);

		List currentSteps = (List) SerializableCache.getInstance().currentStepsCache.get(new Long(entryId));

		if (currentSteps == null) {
			currentSteps = new ArrayList();
			SerializableCache.getInstance().currentStepsCache.put(new Long(entryId), currentSteps);
		}

		currentSteps.add(step);
		SerializableCache.store();

		return step;
	}

	public WorkflowEntry createEntry(String workflowName) {
		long id = SerializableCache.getInstance().globalEntryId++;
		SimpleWorkflowEntry entry = new SimpleWorkflowEntry(id, workflowName, WorkflowEntry.CREATED);
		SerializableCache.getInstance().entryCache.put(new Long(id), entry);
		SerializableCache.store();

		return entry;
	}

	public List findCurrentSteps(long entryId) {
		List currentSteps = (List) SerializableCache.getInstance().currentStepsCache.get(new Long(entryId));

		if (currentSteps == null) {
			currentSteps = new ArrayList();
			SerializableCache.getInstance().currentStepsCache.put(new Long(entryId), currentSteps);
		}

		return currentSteps;
	}

	public WorkflowEntry findEntry(long entryId) {
		return (WorkflowEntry) SerializableCache.getInstance().entryCache.get(new Long(entryId));
	}

	public List findHistorySteps(long entryId) {
		List historySteps = (List) SerializableCache.getInstance().historyStepsCache.get(new Long(entryId));

		if (historySteps == null) {
			historySteps = new ArrayList();
			SerializableCache.getInstance().historyStepsCache.put(new Long(entryId), historySteps);
		}

		return historySteps;
	}

	public void init(Map props) {
		storeFile = (String) props.get("storeFile");

		// check whether the file denoted by the storeFile property is a normal
		// file.
		if (!new File(storeFile).isFile()) {
			log.fatal("storePath property should indicate a normal file");
		}

		// check wheter the directory containing the storeFile exist
		if (!new File(storeFile).getParentFile().exists()) {
			log.fatal("directory " + new File(storeFile).getParent() + " not found");
		}
	}

	public Step markFinished(Step step, int actionId, Date finishDate, String status, String caller) {
		List currentSteps = (List) SerializableCache.getInstance().currentStepsCache.get(new Long(step.getEntryId()));

		for (Iterator iterator = currentSteps.iterator(); iterator.hasNext();) {
			SimpleStep theStep = (SimpleStep) iterator.next();

			if (theStep.getId() == step.getId()) {
				theStep.setStatus(status);
				theStep.setActionId(actionId);
				theStep.setFinishDate(finishDate);
				theStep.setCaller(caller);

				return theStep;
			}
		}

		SerializableCache.store();

		return null;
	}

	public void moveToHistory(Step step) {
		List currentSteps = (List) SerializableCache.getInstance().currentStepsCache.get(new Long(step.getEntryId()));

		List historySteps = (List) SerializableCache.getInstance().historyStepsCache.get(new Long(step.getEntryId()));

		if (historySteps == null) {
			historySteps = new ArrayList();
			SerializableCache.getInstance().historyStepsCache.put(new Long(step.getEntryId()), historySteps);
		}

		SimpleStep simpleStep = (SimpleStep) step;

		for (Iterator iterator = currentSteps.iterator(); iterator.hasNext();) {
			Step currentStep = (Step) iterator.next();

			if (simpleStep.getId() == currentStep.getId()) {
				iterator.remove();
				historySteps.add(simpleStep);

				break;
			}
		}

		SerializableCache.store();
	}
}

class SerializableCache implements Serializable {
	// ~ Static fields/initializers
	// /////////////////////////////////////////////

	private static transient SerializableCache instance;

	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	HashMap currentStepsCache;
	HashMap entryCache;
	HashMap historyStepsCache;
	HashMap propertySetCache;
	long globalEntryId = 1;
	long globalStepId = 1;

	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	private SerializableCache() {
		entryCache = new HashMap();
		currentStepsCache = new HashMap();
		historyStepsCache = new HashMap();
		propertySetCache = new HashMap();
	}

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public List query(WorkflowQuery query) {
		// not implemented
		return Collections.EMPTY_LIST;
	}

	static SerializableCache getInstance() {
		if (instance == null) {
			instance = load();
		}

		return instance;
	}

	static SerializableCache load() {
		try {
			FileInputStream fis = new FileInputStream(new File(SerializableWorkflowStore.storeFile));
			ObjectInputStream ois = new ObjectInputStream(fis);
			SerializableCache o = (SerializableCache) ois.readObject();
			fis.close();

			return o;
		} catch (Exception e) {
			SerializableWorkflowStore.log.fatal("cannot store in file " + SerializableWorkflowStore.storeFile + ". Create a new blank store.");
		}

		return new SerializableCache();
	}

	static void store() {
		try {
			FileOutputStream fos = new FileOutputStream(new File(SerializableWorkflowStore.storeFile));
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(getInstance());
			fos.close();
		} catch (Exception e) {
			SerializableWorkflowStore.log.fatal("cannot store in file " + SerializableWorkflowStore.storeFile + ".");
		}
	}
}
