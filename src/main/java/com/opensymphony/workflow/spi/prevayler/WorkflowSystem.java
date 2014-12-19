/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
/*
 * Created on Feb 12, 2004
 *
 *
 */
package com.opensymphony.workflow.spi.prevayler;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;

import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.query.WorkflowExpressionQuery;
import com.opensymphony.workflow.query.WorkflowQuery;
import com.opensymphony.workflow.spi.SimpleStep;
import com.opensymphony.workflow.spi.SimpleWorkflowEntry;
import com.opensymphony.workflow.spi.Step;
import com.opensymphony.workflow.spi.WorkflowEntry;
import com.opensymphony.workflow.spi.WorkflowStore;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 * 
 * 
 * @author Christopher Farnham chris.farnham@wrycan.com
 **/
public class WorkflowSystem implements WorkflowStore, Serializable {
	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	private HashMap _currentStepsCache = new HashMap();
	private HashMap _entryCache = new HashMap();
	private HashMap _historyStepsCache = new HashMap();
	private HashMap _propertySetCache = new HashMap();
	private transient QueryLogic _queryLogic = new QueryLogic(this);
	private long _globalEntryId = 1;
	private long _globalStepId = 1;

	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	/**
     *
     */
	public WorkflowSystem() {
		super();
	}

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.workflow.spi.WorkflowStore#setEntryState(long, int)
	 */
	public void setEntryState(long entryId, int state) throws StoreException {
		SimpleWorkflowEntry entry = (SimpleWorkflowEntry) findEntry(entryId);
		entry.setState(state);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.workflow.spi.WorkflowStore#getPropertySet(long)
	 */
	public PropertySet getPropertySet(long entryId) throws StoreException {
		PropertySet ps = (PropertySet) _propertySetCache.get(new Long(entryId));

		if (ps == null) {
			ps = PropertySetManager.getInstance("serializable", null);
			_propertySetCache.put(new Long(entryId), ps);
		}

		return ps;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.workflow.spi.WorkflowStore#createCurrentStep(long,
	 * int, java.lang.String, java.util.Date, java.util.Date, java.lang.String,
	 * long[])
	 */
	public Step createCurrentStep(long entryId, int stepId, String owner, Date startDate, Date dueDate, String status, long[] previousIds) throws StoreException {
		long id = _globalStepId++;
		SimpleStep step = new SimpleStep(id, entryId, stepId, 0, owner, startDate, dueDate, null, status, previousIds, null);

		List currentSteps = (List) _currentStepsCache.get(new Long(entryId));

		if (currentSteps == null) {
			currentSteps = new ArrayList();
			_currentStepsCache.put(new Long(entryId), currentSteps);
		}

		currentSteps.add(step);

		return step;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.opensymphony.workflow.spi.WorkflowStore#createEntry(java.lang.String)
	 */
	public WorkflowEntry createEntry(String workflowName) throws StoreException {
		long id = _globalEntryId++;
		SimpleWorkflowEntry entry = new SimpleWorkflowEntry(id, workflowName, WorkflowEntry.CREATED);
		_entryCache.put(new Long(id), entry);

		return entry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.workflow.spi.WorkflowStore#findCurrentSteps(long)
	 */
	public List findCurrentSteps(long entryId) throws StoreException {
		List currentSteps = (List) _currentStepsCache.get(new Long(entryId));

		if (currentSteps == null) {
			currentSteps = new ArrayList();
			_currentStepsCache.put(new Long(entryId), currentSteps);
		}

		return currentSteps;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.workflow.spi.WorkflowStore#findEntry(long)
	 */
	public WorkflowEntry findEntry(long entryId) throws StoreException {
		return (WorkflowEntry) _entryCache.get(new Long(entryId));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.workflow.spi.WorkflowStore#findHistorySteps(long)
	 */
	public List findHistorySteps(long entryId) throws StoreException {
		List historySteps = (List) _historyStepsCache.get(new Long(entryId));

		if (historySteps == null) {
			historySteps = new ArrayList();
			_historyStepsCache.put(new Long(entryId), historySteps);
		}

		return historySteps;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.workflow.spi.WorkflowStore#init(java.util.Map)
	 */
	public void init(Map props) throws StoreException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.opensymphony.workflow.spi.WorkflowStore#markFinished(com.opensymphony
	 * .workflow.spi.Step, int, java.util.Date, java.lang.String,
	 * java.lang.String)
	 */
	public Step markFinished(Step step, int actionId, Date finishDate, String status, String caller) throws StoreException {
		List currentSteps = (List) _currentStepsCache.get(new Long(step.getEntryId()));

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

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.opensymphony.workflow.spi.WorkflowStore#moveToHistory(com.opensymphony
	 * .workflow.spi.Step)
	 */
	public void moveToHistory(Step step) throws StoreException {
		List currentSteps = (List) _currentStepsCache.get(new Long(step.getEntryId()));

		List historySteps = (List) _historyStepsCache.get(new Long(step.getEntryId()));

		if (historySteps == null) {
			historySteps = new ArrayList();
			_historyStepsCache.put(new Long(step.getEntryId()), historySteps);
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.opensymphony.workflow.spi.WorkflowStore#query(com.opensymphony.workflow
	 * .query.WorkflowQuery)
	 */
	public List query(WorkflowQuery query) throws StoreException {
		ArrayList results = new ArrayList();

		for (Iterator iterator = _entryCache.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry mapEntry = (Map.Entry) iterator.next();
			Long entryId = (Long) mapEntry.getKey();

			if (_queryLogic.query(entryId, query)) {
				results.add(entryId);
			}
		}

		return results;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.opensymphony.workflow.spi.WorkflowStore#query(com.opensymphony.workflow
	 * .query.WorkflowExpressionQuery)
	 */
	public List query(WorkflowExpressionQuery query) throws StoreException {
		ArrayList results = new ArrayList();

		for (Iterator iterator = _entryCache.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry mapEntry = (Map.Entry) iterator.next();
			Long entryId = (Long) mapEntry.getKey();

			if (_queryLogic.query(entryId, query)) {
				results.add(entryId);
			}
		}

		return results;
	}
}
