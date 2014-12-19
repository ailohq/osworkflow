/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
/*
 * Created on 30-nov-2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.opensymphony.workflow.spi.hibernate;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.loader.AbstractWorkflowFactory;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class SpringHibernateWorkflowFactory extends AbstractWorkflowFactory {
	// ~ Static fields/initializers
	// /////////////////////////////////////////////

	private static boolean forceReload;

	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	private Map workflows;
	private SessionFactory sessionFactory;
	private boolean reload = false;
	private boolean validate = false;

	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	public SpringHibernateWorkflowFactory() {
		super();
	}

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public void setLayout(String workflowName, Object layout) {
	}

	public Object getLayout(String workflowName) {
		return null;
	}

	public boolean isModifiable(String name) {
		return true;
	}

	public String getName() {
		return "";
	}

	public void setReload(boolean reload) {
		this.reload = reload;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setValidate(boolean validate) {
		this.validate = validate;
	}

	public WorkflowDescriptor getWorkflow(String name, boolean validate) throws FactoryException {
		if (!workflows.containsKey(name)) {
			throw new FactoryException("Unknown workflow name \"" + name + '\"');
		}

		if (reload || forceReload) {
			forceReload = false;
			loadWorkflow(name, validate);
		}

		return (WorkflowDescriptor) workflows.get(name);
	}

	public String[] getWorkflowNames() throws FactoryException {
		int i = 0;
		String[] res = new String[workflows.keySet().size()];
		Iterator it = workflows.keySet().iterator();

		while (it.hasNext()) {
			res[i++] = (String) it.next();
		}

		return res;
	}

	public void createWorkflow(String name) {
	}

	public static void forceReload() {
		forceReload = true;
	}

	public void initDone() throws FactoryException {
		try {
			workflows = new HashMap();

			List workflowNames = new HibernateTemplate(sessionFactory).find("select wfn.workflowName from WorkflowName wfn");

			for (Iterator iter = workflowNames.iterator(); iter.hasNext();) {
				String wfn = iter.next().toString();
				loadWorkflow(wfn, validate);
			}
		} catch (Exception e) {
			throw new FactoryException(e);
		}
	}

	public boolean removeWorkflow(String name) throws FactoryException {
		final HibernateTemplate hibernateTemplate = new HibernateTemplate(sessionFactory);
		WorkflowName wfn = (WorkflowName) hibernateTemplate.load(WorkflowName.class, name);

		if (wfn != null) {
			hibernateTemplate.delete(wfn);

			return true;
		}

		return false;
	}

	public void renameWorkflow(String oldName, String newName) {
		final HibernateTemplate hibernateTemplate = new HibernateTemplate(sessionFactory);
		WorkflowName wfn = (WorkflowName) hibernateTemplate.load(WorkflowName.class, oldName);
		wfn.setWorkflowName(newName);
	}

	public void save() {
	}

	public boolean saveWorkflow(String name, WorkflowDescriptor descriptor, boolean replace) throws FactoryException {
		WorkflowName wfn = new WorkflowName();
		wfn.setWorkflowName(name);
		wfn.setWorkflowDescriptor(descriptor);

		final HibernateTemplate hibernateTemplate = new HibernateTemplate(sessionFactory);
		hibernateTemplate.saveOrUpdate(wfn);

		initDone();

		return false;
	}

	private synchronized void loadWorkflow(final String workflowName, final boolean validate) throws FactoryException {
		try {
			new HibernateTemplate(sessionFactory).execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					try {
						WorkflowName wfn = (WorkflowName) session.load(WorkflowName.class, workflowName);

						if (validate) {
							wfn.getWorkflowDescriptor().validate();
						}

						workflows.put(wfn.getWorkflowName(), wfn.getWorkflowDescriptor());

						return null;
					} catch (InvalidWorkflowDescriptorException e) {
						throw new RuntimeException(e);
					}
				}
			});
		} catch (Exception e) {
			throw new FactoryException(e);
		}
	}

	@Override
	public WorkflowDescriptor getWorkflowFromXml(String xml) throws FactoryException {
		throw new FactoryException("Not implemented");
	}
}
