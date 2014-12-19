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

import com.opensymphony.util.DataUtil;
import com.opensymphony.util.TextUtils;

import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.query.Expression;
import com.opensymphony.workflow.query.FieldExpression;
import com.opensymphony.workflow.query.NestedExpression;
import com.opensymphony.workflow.query.WorkflowExpressionQuery;
import com.opensymphony.workflow.query.WorkflowQuery;
import com.opensymphony.workflow.spi.SimpleStep;
import com.opensymphony.workflow.spi.SimpleWorkflowEntry;
import com.opensymphony.workflow.spi.WorkflowStore;

import java.security.InvalidParameterException;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * This is basically the query logic cut and pasted from MemoryWorkflowStore.
 * I've separated it into a separate class which relies on WorkflowStore for its
 * query base, that way any future or other WorkflowStore can rely on this
 * logic.
 * 
 * I thought about refactoring MemoryWorkflowStore to use this class, but as its
 * such a well used class I didn't want to do it before some more eyes looked at
 * things here.
 * 
 * @author Christopher Farnham
 **/
public class QueryLogic {
	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	private WorkflowStore _store = null;

	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	public QueryLogic(WorkflowStore store) {
		_store = store;
	}

	private QueryLogic() {
		super();
	}

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public boolean query(Long entryId, WorkflowExpressionQuery query) throws StoreException {
		Expression expression = query.getExpression();

		if (expression.isNested()) {
			return this.checkNestedExpression(entryId.longValue(), (NestedExpression) expression);
		} else {
			return this.checkExpression(entryId.longValue(), (FieldExpression) expression);
		}
	}

	public boolean query(Long entryId, WorkflowQuery query) throws StoreException {
		if (query.getLeft() == null) {
			return queryBasic(entryId, query);
		} else {
			int operator = query.getOperator();
			WorkflowQuery left = query.getLeft();
			WorkflowQuery right = query.getRight();

			switch (operator) {
			case WorkflowQuery.AND:
				return query(entryId, left) && query(entryId, right);

			case WorkflowQuery.OR:
				return query(entryId, left) || query(entryId, right);

			case WorkflowQuery.XOR:
				return query(entryId, left) ^ query(entryId, right);
			}
		}

		return false;
	}

	private boolean checkExpression(long entryId, FieldExpression expression) throws StoreException {
		Object value = expression.getValue();
		int operator = expression.getOperator();
		int field = expression.getField();
		int context = expression.getContext();

		Long id = new Long(entryId);

		if (context == FieldExpression.ENTRY) {
			SimpleWorkflowEntry theEntry = (SimpleWorkflowEntry) _store.findEntry(entryId);

			if (field == FieldExpression.NAME) {
				return this.compareText(theEntry.getWorkflowName(), (String) value, operator);
			}

			if (field == FieldExpression.STATE) {
				return this.compareLong(DataUtil.getInt((Integer) value), theEntry.getState(), operator);
			}

			throw new InvalidParameterException("unknown field");
		}

		List steps;

		if (context == FieldExpression.CURRENT_STEPS) {
			// steps = (List) currentStepsCache.get(id);
			steps = (List) _store.findCurrentSteps(id.longValue());
		} else if (context == FieldExpression.HISTORY_STEPS) {
			// steps = (List) historyStepsCache.get(id);
			steps = (List) _store.findHistorySteps(id.longValue());
		} else {
			throw new InvalidParameterException("unknown field context");
		}

		if (steps == null) {
			return false;
		}

		boolean expressionResult = false;

		switch (field) {
		case FieldExpression.ACTION:

			long actionId = DataUtil.getInt((Integer) value);

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (this.compareLong(step.getActionId(), actionId, operator)) {
					expressionResult = true;

					break;
				}
			}

			break;

		case FieldExpression.CALLER:

			String caller = (String) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (this.compareText(step.getCaller(), caller, operator)) {
					expressionResult = true;

					break;
				}
			}

			break;

		case FieldExpression.FINISH_DATE:

			Date finishDate = (Date) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (this.compareDate(step.getFinishDate(), finishDate, operator)) {
					expressionResult = true;

					break;
				}
			}

			break;

		case FieldExpression.OWNER:

			String owner = (String) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (this.compareText(step.getOwner(), owner, operator)) {
					expressionResult = true;

					break;
				}
			}

			break;

		case FieldExpression.START_DATE:

			Date startDate = (Date) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (this.compareDate(step.getStartDate(), startDate, operator)) {
					expressionResult = true;

					break;
				}
			}

			break;

		case FieldExpression.STEP:

			int stepId = DataUtil.getInt((Integer) value);

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (this.compareLong(step.getStepId(), stepId, operator)) {
					expressionResult = true;

					break;
				}
			}

			break;

		case FieldExpression.STATUS:

			String status = (String) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (this.compareText(step.getStatus(), status, operator)) {
					expressionResult = true;

					break;
				}
			}

			break;

		case FieldExpression.DUE_DATE:

			Date dueDate = (Date) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (this.compareDate(step.getDueDate(), dueDate, operator)) {
					expressionResult = true;

					break;
				}
			}

			break;
		}

		if (expression.isNegate()) {
			return !expressionResult;
		} else {
			return expressionResult;
		}
	}

	private boolean checkNestedExpression(long entryId, NestedExpression nestedExpression) throws StoreException {
		for (int i = 0; i < nestedExpression.getExpressionCount(); i++) {
			boolean expressionResult;
			Expression expression = nestedExpression.getExpression(i);

			if (expression.isNested()) {
				expressionResult = this.checkNestedExpression(entryId, (NestedExpression) expression);
			} else {
				expressionResult = this.checkExpression(entryId, (FieldExpression) expression);
			}

			if (nestedExpression.getExpressionOperator() == NestedExpression.AND) {
				if (expressionResult == false) {
					return nestedExpression.isNegate();
				}
			} else if (nestedExpression.getExpressionOperator() == NestedExpression.OR) {
				if (expressionResult == true) {
					return !nestedExpression.isNegate();
				}
			}
		}

		if (nestedExpression.getExpressionOperator() == NestedExpression.AND) {
			return !nestedExpression.isNegate();
		} else if (nestedExpression.getExpressionOperator() == NestedExpression.OR) {
			return nestedExpression.isNegate();
		}

		throw new InvalidParameterException("unknown operator");
	}

	private boolean compareDate(Date value1, Date value2, int operator) {
		switch (operator) {
		case FieldExpression.EQUALS:
			return value1.compareTo(value2) == 0;

		case FieldExpression.NOT_EQUALS:
			return value1.compareTo(value2) != 0;

		case FieldExpression.GT:
			return (value1.compareTo(value2) > 0);

		case FieldExpression.LT:
			return value1.compareTo(value2) < 0;
		}

		throw new InvalidParameterException("unknown field operator");
	}

	private boolean compareLong(long value1, long value2, int operator) {
		switch (operator) {
		case FieldExpression.EQUALS:
			return value1 == value2;

		case FieldExpression.NOT_EQUALS:
			return value1 != value2;

		case FieldExpression.GT:
			return value1 > value2;

		case FieldExpression.LT:
			return value1 < value2;
		}

		throw new InvalidParameterException("unknown field operator");
	}

	private boolean compareText(String value1, String value2, int operator) {
		switch (operator) {
		case FieldExpression.EQUALS:
			return TextUtils.noNull(value1).equals(value2);

		case FieldExpression.NOT_EQUALS:
			return !TextUtils.noNull(value1).equals(value2);

		case FieldExpression.GT:
			return TextUtils.noNull(value1).compareTo(value2) > 0;

		case FieldExpression.LT:
			return TextUtils.noNull(value1).compareTo(value2) < 0;
		}

		throw new InvalidParameterException("unknown field operator");
	}

	private boolean queryBasic(Long entryId, WorkflowQuery query) throws StoreException {
		// the query object is a comparison
		Object value = query.getValue();
		int operator = query.getOperator();
		int field = query.getField();
		int type = query.getType();

		switch (operator) {
		case WorkflowQuery.EQUALS:
			return queryEquals(entryId, field, type, value);

		case WorkflowQuery.NOT_EQUALS:
			return queryNotEquals(entryId, field, type, value);

		case WorkflowQuery.GT:
			return queryGreaterThan(entryId, field, type, value);

		case WorkflowQuery.LT:
			return queryLessThan(entryId, field, type, value);
		}

		return false;
	}

	private boolean queryEquals(Long entryId, int field, int type, Object value) throws StoreException {
		List steps;

		if (type == WorkflowQuery.CURRENT) {
			// steps = (List) currentStepsCache.get(entryId);
			steps = (List) _store.findCurrentSteps(entryId.longValue());
		} else {
			// steps = (List) historyStepsCache.get(entryId);
			steps = (List) _store.findCurrentSteps(entryId.longValue());
		}

		switch (field) {
		case WorkflowQuery.ACTION:

			long actionId = DataUtil.getInt((Integer) value);

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (step.getActionId() == actionId) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.CALLER:

			String caller = (String) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (TextUtils.noNull(step.getCaller()).equals(caller)) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.FINISH_DATE:

			Date finishDate = (Date) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (finishDate.equals(step.getFinishDate())) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.OWNER:

			String owner = (String) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (TextUtils.noNull(step.getOwner()).equals(owner)) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.START_DATE:

			Date startDate = (Date) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (startDate.equals(step.getStartDate())) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.STEP:

			int stepId = DataUtil.getInt((Integer) value);

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (stepId == step.getStepId()) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.STATUS:

			String status = (String) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (TextUtils.noNull(step.getStatus()).equals(status)) {
					return true;
				}
			}

			return false;
		}

		return false;
	}

	private boolean queryGreaterThan(Long entryId, int field, int type, Object value) throws StoreException {
		List steps;

		if (type == WorkflowQuery.CURRENT) {
			// steps = (List) currentStepsCache.get(entryId);
			steps = (List) _store.findCurrentSteps(entryId.longValue());
		} else {
			// steps = (List) historyStepsCache.get(entryId);
			steps = (List) _store.findHistorySteps(entryId.longValue());
		}

		switch (field) {
		case WorkflowQuery.ACTION:

			long actionId = DataUtil.getLong((Long) value);

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (step.getActionId() > actionId) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.CALLER:

			String caller = (String) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (TextUtils.noNull(step.getCaller()).compareTo(caller) > 0) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.FINISH_DATE:

			Date finishDate = (Date) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (step.getFinishDate().compareTo(finishDate) > 0) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.OWNER:

			String owner = (String) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (TextUtils.noNull(step.getOwner()).compareTo(owner) > 0) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.START_DATE:

			Date startDate = (Date) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (step.getStartDate().compareTo(startDate) > 0) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.STEP:

			int stepId = DataUtil.getInt((Integer) value);

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (step.getStepId() > stepId) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.STATUS:

			String status = (String) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (TextUtils.noNull(step.getStatus()).compareTo(status) > 0) {
					return true;
				}
			}

			return false;
		}

		return false;
	}

	private boolean queryLessThan(Long entryId, int field, int type, Object value) throws StoreException {
		List steps;

		if (type == WorkflowQuery.CURRENT) {
			// steps = (List) currentStepsCache.get(entryId);
			steps = (List) _store.findCurrentSteps(entryId.longValue());
		} else {
			// steps = (List) historyStepsCache.get(entryId);
			steps = (List) _store.findHistorySteps(entryId.longValue());
		}

		switch (field) {
		case WorkflowQuery.ACTION:

			long actionId = DataUtil.getLong((Long) value);

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (step.getActionId() < actionId) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.CALLER:

			String caller = (String) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (TextUtils.noNull(step.getCaller()).compareTo(caller) < 0) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.FINISH_DATE:

			Date finishDate = (Date) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (step.getFinishDate().compareTo(finishDate) < 0) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.OWNER:

			String owner = (String) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (TextUtils.noNull(step.getOwner()).compareTo(owner) < 0) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.START_DATE:

			Date startDate = (Date) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (step.getStartDate().compareTo(startDate) < 0) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.STEP:

			int stepId = DataUtil.getInt((Integer) value);

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (step.getStepId() < stepId) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.STATUS:

			String status = (String) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (TextUtils.noNull(step.getStatus()).compareTo(status) < 0) {
					return true;
				}
			}

			return false;
		}

		return false;
	}

	private boolean queryNotEquals(Long entryId, int field, int type, Object value) throws StoreException {
		List steps;

		if (type == WorkflowQuery.CURRENT) {
			// steps = (List) currentStepsCache.get(entryId);
			steps = (List) _store.findCurrentSteps(entryId.longValue());
		} else {
			// steps = (List) historyStepsCache.get(entryId);
			steps = (List) _store.findHistorySteps(entryId.longValue());
		}

		switch (field) {
		case WorkflowQuery.ACTION:

			long actionId = DataUtil.getLong((Long) value);

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (step.getActionId() != actionId) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.CALLER:

			String caller = (String) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (!TextUtils.noNull(step.getCaller()).equals(caller)) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.FINISH_DATE:

			Date finishDate = (Date) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (!finishDate.equals(step.getFinishDate())) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.OWNER:

			String owner = (String) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (!TextUtils.noNull(step.getOwner()).equals(owner)) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.START_DATE:

			Date startDate = (Date) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (!startDate.equals(step.getStartDate())) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.STEP:

			int stepId = DataUtil.getInt((Integer) value);

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (stepId != step.getStepId()) {
					return true;
				}
			}

			return false;

		case WorkflowQuery.STATUS:

			String status = (String) value;

			for (Iterator iterator = steps.iterator(); iterator.hasNext();) {
				SimpleStep step = (SimpleStep) iterator.next();

				if (!TextUtils.noNull(step.getStatus()).equals(status)) {
					return true;
				}
			}

			return false;
		}

		return false;
	}
}
