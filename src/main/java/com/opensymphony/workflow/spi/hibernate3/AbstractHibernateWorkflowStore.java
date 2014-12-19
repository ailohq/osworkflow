/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.spi.hibernate3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.QueryNotSupportedException;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.query.FieldExpression;
import com.opensymphony.workflow.query.NestedExpression;
import com.opensymphony.workflow.query.WorkflowExpressionQuery;
import com.opensymphony.workflow.query.WorkflowQuery;
import com.opensymphony.workflow.spi.Step;
import com.opensymphony.workflow.spi.WorkflowEntry;
import com.opensymphony.workflow.spi.WorkflowStore;
import com.opensymphony.workflow.spi.hibernate.HibernateCurrentStep;
import com.opensymphony.workflow.spi.hibernate.HibernateHistoryStep;
import com.opensymphony.workflow.spi.hibernate.HibernateStep;
import com.opensymphony.workflow.spi.hibernate.HibernateWorkflowEntry;
import com.opensymphony.workflow.util.PropertySetDelegate;

/**
 * @author Luca Masini
 * @since 2005-9-23
 * 
 */
public abstract class AbstractHibernateWorkflowStore implements WorkflowStore {
	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	private PropertySetDelegate propertySetDelegate;
	private String cacheRegion = null;
	private boolean cacheable = false;

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	// ~ Getter/Setter
	// ////////////////////////////////////////////////////////////////
	public void setCacheRegion(String cacheRegion) {
		this.cacheRegion = cacheRegion;
	}

	public void setCacheable(boolean cacheable) {
		this.cacheable = cacheable;
	}

	public void setEntryState(final long entryId, final int state) throws StoreException {
		loadEntry(entryId).setState(state);
	}

	public PropertySet getPropertySet(long entryId) throws StoreException {
		if (getPropertySetDelegate() == null) {
			throw new StoreException("PropertySetDelegate is not properly configured");
		}

		return getPropertySetDelegate().getPropertySet(entryId);
	}

	public void setPropertySetDelegate(PropertySetDelegate propertySetDelegate) {
		this.propertySetDelegate = propertySetDelegate;
	}

	public PropertySetDelegate getPropertySetDelegate() {
		return propertySetDelegate;
	}

	public Step createCurrentStep(final long entryId, final int stepId, final String owner, final Date startDate, final Date dueDate, final String status, final long[] previousIds)
			throws StoreException {
		final HibernateWorkflowEntry entry = loadEntry(entryId);
		final HibernateCurrentStep step = new HibernateCurrentStep();

		step.setStepId(stepId);
		step.setOwner(owner);
		step.setStartDate(startDate);
		step.setDueDate(dueDate);
		step.setStatus(status);

		// This is for backward compatibility, but current Store doesn't
		// persist this collection, nor is such property visibile outside
		// OSWF internal classes
		List previousSteps = new ArrayList(previousIds.length);

		for (int i = 0; i < previousIds.length; i++) {
			HibernateCurrentStep previousStep = new HibernateCurrentStep();
			previousSteps.add(previousStep);
		}

		step.setPreviousSteps(previousSteps);

		entry.addCurrentSteps(step);

		// We need to save here because we soon will need the stepId
		// that hibernate calculate on save or flush
		save(step);

		return step;
	}

	public WorkflowEntry createEntry(String workflowName) throws StoreException {
		final HibernateWorkflowEntry entry = new HibernateWorkflowEntry();
		entry.setState(WorkflowEntry.CREATED);
		entry.setWorkflowName(workflowName);
		save(entry);

		return entry;
	}

	public List findCurrentSteps(final long entryId) throws StoreException {
		// We are asking for current step list, so here we have an anti-lazy
		// copy of the Hibernate array in memory. This also prevents problem
		// in case the use is going with a pattern that span a session
		// for method call
		return new ArrayList(loadEntry(entryId).getCurrentSteps());
	}

	public WorkflowEntry findEntry(long entryId) throws StoreException {
		return loadEntry(entryId);
	}

	public List findHistorySteps(final long entryId) throws StoreException {
		// We are asking for current step list, so here we have an anti-lazy
		// copy of the Hibernate array in memory. This also prevents problem
		// in case the use is going with a pattern that span a session
		// for method call
		return new ArrayList(loadEntry(entryId).getHistorySteps());
	}

	public Step markFinished(Step step, int actionId, Date finishDate, String status, String caller) throws StoreException {
		final HibernateCurrentStep currentStep = (HibernateCurrentStep) step;

		currentStep.setActionId(actionId);
		currentStep.setFinishDate(finishDate);
		currentStep.setStatus(status);
		currentStep.setCaller(caller);

		return currentStep;
	}

	public void moveToHistory(final Step step) throws StoreException {
		final HibernateCurrentStep currentStep = (HibernateCurrentStep) step;
		final HibernateWorkflowEntry entry = currentStep.getEntry();
		final HibernateHistoryStep hStep = new HibernateHistoryStep(currentStep);

		entry.removeCurrentSteps(currentStep);
		delete(currentStep);
		entry.addHistorySteps(hStep);

		// We need to save here because we soon will need the stepId
		// that hibernate calculate on save or flush
		save(hStep);
	}

	public List query(final WorkflowQuery query) throws StoreException {
		return (List) execute(new InternalCallback() {
			public Object doInHibernate(Session session) throws HibernateException, StoreException {
				Class entityClass;

				int qtype = query.getType();

				if (qtype == 0) { // then not set, so look in sub queries

					if (query.getLeft() != null) {
						qtype = query.getLeft().getType();
					}
				}

				if (qtype == WorkflowQuery.CURRENT) {
					entityClass = HibernateCurrentStep.class;
				} else {
					entityClass = HibernateHistoryStep.class;
				}

				Criteria criteria = session.createCriteria(entityClass);
				Criterion expression = buildExpression(query);
				criteria.setCacheable(isCacheable());

				if (isCacheable()) {
					criteria.setCacheRegion(getCacheRegion());
				}

				criteria.add(expression);

				Set results = new HashSet();
				Iterator iter = criteria.list().iterator();

				while (iter.hasNext()) {
					HibernateStep step = (HibernateStep) iter.next();
					results.add(new Long(step.getEntryId()));
				}

				return new ArrayList(results);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.opensymphony.workflow.spi.WorkflowStore#query(com.opensymphony.workflow
	 * .query.WorkflowExpressionQuery)
	 */
	public List query(final WorkflowExpressionQuery query) throws StoreException {
		return (List) execute(new InternalCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				com.opensymphony.workflow.query.Expression expression = query.getExpression();

				Criterion expr;

				Class entityClass = getQueryClass(expression, null);

				if (expression.isNested()) {
					expr = buildNested((NestedExpression) expression);
				} else {
					expr = queryComparison((FieldExpression) expression);
				}

				Criteria criteria = session.createCriteria(entityClass);
				criteria.setCacheable(isCacheable());

				if (isCacheable()) {
					criteria.setCacheRegion(getCacheRegion());
				}

				criteria.add(expr);

				Set results = new HashSet();

				Iterator iter = criteria.list().iterator();

				while (iter.hasNext()) {
					Object next = iter.next();
					Object item;

					if (next instanceof HibernateStep) {
						HibernateStep step = (HibernateStep) next;
						item = new Long(step.getEntryId());
					} else {
						WorkflowEntry entry = (WorkflowEntry) next;
						item = new Long(entry.getId());
					}

					results.add(item);
				}

				return new ArrayList(results);
			}
		});
	}

	// Companion method of InternalCallback class
	protected abstract Object execute(InternalCallback action) throws StoreException;

	protected String getCacheRegion() {
		return cacheRegion;
	}

	protected boolean isCacheable() {
		return cacheable;
	}

	protected Criterion getExpression(final WorkflowQuery query) throws StoreException {
		return (Criterion) execute(new InternalCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				int operator = query.getOperator();

				switch (operator) {
				case WorkflowQuery.EQUALS:
					return Expression.eq(getFieldName(query.getField()), query.getValue());

				case WorkflowQuery.NOT_EQUALS:
					return Expression.not(Expression.like(getFieldName(query.getField()), query.getValue()));

				case WorkflowQuery.GT:
					return Expression.gt(getFieldName(query.getField()), query.getValue());

				case WorkflowQuery.LT:
					return Expression.lt(getFieldName(query.getField()), query.getValue());

				default:
					return Expression.eq(getFieldName(query.getField()), query.getValue());
				}
			}
		});
	}

	protected void delete(final Object entry) throws StoreException {
		execute(new InternalCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				session.delete(entry);

				return null;
			}
		});
	}

	// ~ DAO Methods
	// ////////////////////////////////////////////////////////////////
	protected HibernateWorkflowEntry loadEntry(final long entryId) throws StoreException {
		return (HibernateWorkflowEntry) execute(new InternalCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return session.load(HibernateWorkflowEntry.class, new Long(entryId));
			}
		});
	}

	protected void save(final Object entry) throws StoreException {
		execute(new InternalCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				session.save(entry);

				return null;
			}
		});
	}

	private String getFieldName(int field) {
		switch (field) {
		case FieldExpression.ACTION: // actionId
			return "actionId";

		case FieldExpression.CALLER:
			return "caller";

		case FieldExpression.FINISH_DATE:
			return "finishDate";

		case FieldExpression.OWNER:
			return "owner";

		case FieldExpression.START_DATE:
			return "startDate";

		case FieldExpression.STEP: // stepId
			return "stepId";

		case FieldExpression.STATUS:
			return "status";

		case FieldExpression.STATE:
			return "state";

		case FieldExpression.NAME:
			return "workflowName";

		case FieldExpression.DUE_DATE:
			return "dueDate";

		default:
			return "1";
		}
	}

	private Class getQueryClass(com.opensymphony.workflow.query.Expression expr, Collection classesCache) {
		if (classesCache == null) {
			classesCache = new HashSet();
		}

		if (expr instanceof FieldExpression) {
			FieldExpression fieldExpression = (FieldExpression) expr;

			switch (fieldExpression.getContext()) {
			case FieldExpression.CURRENT_STEPS:
				classesCache.add(HibernateCurrentStep.class);

				break;

			case FieldExpression.HISTORY_STEPS:
				classesCache.add(HibernateHistoryStep.class);

				break;

			case FieldExpression.ENTRY:
				classesCache.add(HibernateWorkflowEntry.class);

				break;

			default:
				throw new QueryNotSupportedException("Query for unsupported context " + fieldExpression.getContext());
			}
		} else {
			NestedExpression nestedExpression = (NestedExpression) expr;

			for (int i = 0; i < nestedExpression.getExpressionCount(); i++) {
				com.opensymphony.workflow.query.Expression expression = nestedExpression.getExpression(i);

				if (expression.isNested()) {
					classesCache.add(getQueryClass(nestedExpression.getExpression(i), classesCache));
				} else {
					classesCache.add(getQueryClass(expression, classesCache));
				}
			}
		}

		if (classesCache.size() > 1) {
			throw new QueryNotSupportedException("Store does not support nested queries of different types (types found:" + classesCache + ")");
		}

		return (Class) classesCache.iterator().next();
	}

	private Criterion buildExpression(WorkflowQuery query) throws StoreException {
		if (query.getLeft() == null) {
			if (query.getRight() == null) {
				return getExpression(query); // leaf node
			} else {
				throw new StoreException("Invalid WorkflowQuery object.  QueryLeft is null but QueryRight is not.");
			}
		} else {
			if (query.getRight() == null) {
				throw new StoreException("Invalid WorkflowQuery object.  QueryLeft is not null but QueryRight is.");
			}

			int operator = query.getOperator();
			WorkflowQuery left = query.getLeft();
			WorkflowQuery right = query.getRight();

			switch (operator) {
			case WorkflowQuery.AND:
				return Expression.and(buildExpression(left), buildExpression(right));

			case WorkflowQuery.OR:
				return Expression.or(buildExpression(left), buildExpression(right));

			case WorkflowQuery.XOR:
				throw new QueryNotSupportedException("XOR Operator in Queries not supported by " + this.getClass().getName());

			default:
				throw new QueryNotSupportedException("Operator '" + operator + "' is not supported by " + this.getClass().getName());
			}
		}
	}

	private Criterion buildNested(NestedExpression nestedExpression) {
		Criterion full = null;

		for (int i = 0; i < nestedExpression.getExpressionCount(); i++) {
			Criterion expr;
			com.opensymphony.workflow.query.Expression expression = nestedExpression.getExpression(i);

			if (expression.isNested()) {
				expr = buildNested((NestedExpression) nestedExpression.getExpression(i));
			} else {
				FieldExpression sub = (FieldExpression) nestedExpression.getExpression(i);
				expr = queryComparison(sub);

				if (sub.isNegate()) {
					expr = Expression.not(expr);
				}
			}

			if (full == null) {
				full = expr;
			} else {
				switch (nestedExpression.getExpressionOperator()) {
				case NestedExpression.AND:
					full = Expression.and(full, expr);

					break;

				case NestedExpression.OR:
					full = Expression.or(full, expr);
				}
			}
		}

		return full;
	}

	private Criterion queryComparison(FieldExpression expression) {
		int operator = expression.getOperator();

		switch (operator) {
		case FieldExpression.EQUALS:
			return Expression.eq(getFieldName(expression.getField()), expression.getValue());

		case FieldExpression.NOT_EQUALS:
			return Expression.not(Expression.like(getFieldName(expression.getField()), expression.getValue()));

		case FieldExpression.GT:
			return Expression.gt(getFieldName(expression.getField()), expression.getValue());

		case FieldExpression.LT:
			return Expression.lt(getFieldName(expression.getField()), expression.getValue());

		default:
			return Expression.eq(getFieldName(expression.getField()), expression.getValue());
		}
	}

	// ~ Inner Interfaces
	// ///////////////////////////////////////////////////////

	// ~ Internal Interfaces
	// /////////////////////////////////////////////////////
	// Template method pattern to delegate implementation of Session
	// management to subclasses
	protected interface InternalCallback {
		public Object doInHibernate(Session session) throws HibernateException, StoreException;
	}
}
