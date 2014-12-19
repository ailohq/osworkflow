/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
/*
 * Created on Feb 11, 2004
 *
 *
 */
package com.opensymphony.workflow.spi.prevayler;

import com.opensymphony.module.propertyset.PropertySet;

import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.query.WorkflowExpressionQuery;
import com.opensymphony.workflow.query.WorkflowQuery;
import com.opensymphony.workflow.spi.Step;
import com.opensymphony.workflow.spi.WorkflowEntry;
import com.opensymphony.workflow.spi.WorkflowStore;

import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;
import org.prevayler.Query;
import org.prevayler.Transaction;
import org.prevayler.TransactionWithQuery;

import java.io.IOException;
import java.io.Serializable;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 
 * This is a WorkflowStore implementation which uses Prevaylence as its
 * datastore. See http://www.prevayler.org/wiki.jsp
 * 
 * It creates a transient or non-transient store depending upon the properties
 * set.
 * 
 * @author Christopher Farnham
 **/
public class PrevaylerWorkflowStore implements WorkflowStore, Serializable {
	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	private transient Prevayler _prevayler = null;
	private transient String _prevalenceBase;
	private WorkflowStore _store = null;

	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	public PrevaylerWorkflowStore() throws IOException, ClassNotFoundException {
		this("WorkflowPrevaylenceBase");
	}

	public PrevaylerWorkflowStore(String prevalenceBase) throws IOException, ClassNotFoundException {
		super();
		_prevalenceBase = prevalenceBase;
		_store = new WorkflowSystem();
	}

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.workflow.spi.WorkflowStore#setEntryState(long, int)
	 */
	public void setEntryState(long entryId, int state) throws StoreException {
		Object[] o = { new Long(entryId), new Integer(state) };

		try {
			_prevayler.execute(new TransactionImpl(o) {
				public void execute(WorkflowSystem store) {
					Object[] o = (Object[]) _object;
					long entryId = ((Long) o[0]).longValue();
					int state = ((Integer) o[1]).intValue();

					try {
						store.setEntryState(entryId, state);
					} catch (StoreException e) {
						throw new RuntimeException(e);
					}
				}
			});
		} catch (Exception e) {
			if (e.getCause() instanceof StoreException) {
				throw ((StoreException) e.getCause());
			} else {
				throw new StoreException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.workflow.spi.WorkflowStore#getPropertySet(long)
	 */
	public PropertySet getPropertySet(long entryId) throws StoreException {
		try {
			return (PropertySet) _prevayler.execute(new TransactionWithQueryImpl(new Long(entryId)) {
				public Object execute(WorkflowSystem store) throws StoreException {
					return store.getPropertySet(((Long) _object).longValue());
				}
			});
		} catch (Exception e) {
			throw new StoreException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.workflow.spi.WorkflowStore#createCurrentStep(long,
	 * int, java.lang.String, java.util.Date, java.util.Date, java.lang.String,
	 * long[])
	 */
	public Step createCurrentStep(long entryId, int stepId, String owner, Date startDate, Date dueDate, String status, long[] previousIds) throws StoreException {
		Object[] oArray = {
				new Long(entryId), new Integer(stepId), owner, startDate, dueDate,
				status, previousIds
		};

		try {
			return (Step) _prevayler.execute(new TransactionWithQueryImpl(oArray) {
				public Object execute(WorkflowSystem store) throws StoreException {
					Object[] o = (Object[]) _object;
					long entryId = ((Long) o[0]).longValue();
					int stepId = ((Integer) o[1]).intValue();
					String owner = (String) o[2];
					Date startDate = (Date) o[3];
					Date dueDate = (Date) o[4];
					String status = (String) o[5];
					long[] previousIds = (long[]) o[6];

					return store.createCurrentStep(entryId, stepId, owner, startDate, dueDate, status, previousIds);
				}
			});
		} catch (Exception e) {
			throw new StoreException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.opensymphony.workflow.spi.WorkflowStore#createEntry(java.lang.String)
	 */
	public WorkflowEntry createEntry(String workflowName) throws StoreException {
		try {
			return (WorkflowEntry) _prevayler.execute(new TransactionWithQueryImpl(workflowName) {
				public Object execute(WorkflowSystem store) throws StoreException {
					return store.createEntry(((String) _object));
				}
			});
		} catch (Exception e) {
			throw new StoreException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.workflow.spi.WorkflowStore#findCurrentSteps(long)
	 */
	public List findCurrentSteps(long entryId) throws StoreException {
		try {
			return (List) _prevayler.execute(new TransactionWithQueryImpl(new Long(entryId)) {
				public Object execute(WorkflowSystem store) throws StoreException {
					return store.findCurrentSteps(((Long) _object).longValue());
				}
			});
		} catch (Exception e) {
			throw new StoreException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.workflow.spi.WorkflowStore#findEntry(long)
	 */
	public WorkflowEntry findEntry(long entryId) throws StoreException {
		try {
			return (WorkflowEntry) _prevayler.execute(new TransactionWithQueryImpl(new Long(entryId)) {
				public Object execute(WorkflowSystem store) throws StoreException {
					return store.findEntry(((Long) _object).longValue());
				}
			});
		} catch (Exception e) {
			throw new StoreException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.workflow.spi.WorkflowStore#findHistorySteps(long)
	 */
	public List findHistorySteps(long entryId) throws StoreException {
		try {
			return (List) _prevayler.execute(new TransactionWithQueryImpl(new Long(entryId)) {
				public Object execute(WorkflowSystem store) throws StoreException {
					return store.findHistorySteps(((Long) _object).longValue());
				}
			});
		} catch (Exception e) {
			throw new StoreException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.workflow.spi.WorkflowStore#init(java.util.Map)
	 */
	public void init(Map props) throws StoreException {
		_store.init(props);

		boolean isTransient = false;
		String pBaseKey = "base";

		if (props.containsKey(pBaseKey)) {
			_prevalenceBase = (String) props.get(pBaseKey);
		}

		String transientKey = "transient";

		if (props.containsKey(transientKey)) {
			String value = (String) props.get(transientKey);

			if (value.equalsIgnoreCase("true")) {
				isTransient = true;
			}
		}

		try {
			initializePrevaylenceSystem(_prevalenceBase, isTransient);
		} catch (Exception e) {
			throw new StoreException(e);
		}
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
		Object[] oArray = {
				step, new Integer(actionId), finishDate, status, caller
		};

		try {
			return (Step) _prevayler.execute(new TransactionWithQueryImpl(oArray) {
				public Object execute(WorkflowSystem store) throws StoreException {
					Object[] o = (Object[]) _object;
					Step step = (Step) o[0];
					int actionId = ((Integer) o[1]).intValue();
					Date finishDate = (Date) o[2];
					String status = (String) o[3];
					String caller = (String) o[4];

					return store.markFinished(step, actionId, finishDate, status, caller);
				}
			});
		} catch (Exception e) {
			throw new StoreException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.opensymphony.workflow.spi.WorkflowStore#moveToHistory(com.opensymphony
	 * .workflow.spi.Step)
	 */
	public void moveToHistory(Step step) throws StoreException {
		try {
			_prevayler.execute(new TransactionImpl(step) {
				public void execute(WorkflowSystem store) throws StoreException {
					store.moveToHistory(((Step) _object));
				}
			});
		} catch (Exception e) {
			throw new StoreException(e);
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
		try {
			return (List) _prevayler.execute(new QueryImpl(query) {
				public Object execute(WorkflowSystem store) throws StoreException {
					return store.query(((WorkflowQuery) _object));
				}
			});
		} catch (Exception e) {
			throw new StoreException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.opensymphony.workflow.spi.WorkflowStore#query(com.opensymphony.workflow
	 * .query.WorkflowExpressionQuery)
	 */
	public List query(WorkflowExpressionQuery query) throws StoreException {
		try {
			return (List) _prevayler.execute(new QueryImpl(query) {
				public Object execute(WorkflowSystem store) throws StoreException {
					return store.query(((WorkflowExpressionQuery) _object));
				}
			});
		} catch (Exception e) {
			throw new StoreException(e);
		}
	}

	/**
	 * @param prevalenceBase
	 */
	private void initializePrevaylenceSystem(String prevalenceBase, boolean isTransient) throws IOException, ClassNotFoundException {
		if (isTransient) {
			_prevayler = PrevaylerFactory.createTransientPrevayler(((Serializable) _store));
		} else {
			_prevayler = PrevaylerFactory.createPrevayler(((Serializable) _store), prevalenceBase);
		}
	}

	// ~ Inner Classes
	// //////////////////////////////////////////////////////////

	private class ObjectActioner implements Serializable {
		protected Object _object = null;

		public ObjectActioner() {
		}

		public ObjectActioner(Object object) {
			_object = object;
		}
	}

	private abstract class QueryImpl extends ObjectActioner implements Query {
		public QueryImpl(Object object) {
			super(object);
		}

		public abstract Object execute(WorkflowSystem store) throws StoreException;

		public Object query(Object prevSystem, Date ignored) throws Exception {
			return execute(((WorkflowSystem) prevSystem));
		}
	}

	private abstract class TransactionImpl extends ObjectActioner implements Transaction {
		public TransactionImpl(Object object) {
			super(object);
		}

		public abstract void execute(WorkflowSystem store) throws StoreException;

		public void executeOn(Object prevSystem, Date ignored) {
			try {
				execute(((WorkflowSystem) prevSystem));
			} catch (StoreException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private abstract class TransactionWithQueryImpl extends ObjectActioner implements TransactionWithQuery {
		public TransactionWithQueryImpl(Object object) {
			super(object);
		}

		public abstract Object execute(WorkflowSystem store) throws StoreException;

		public Object executeAndQuery(Object prevSystem, Date ignored) throws Exception {
			return execute(((WorkflowSystem) prevSystem));
		}
	}
}
