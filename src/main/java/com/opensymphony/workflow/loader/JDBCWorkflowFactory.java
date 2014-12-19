/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.loader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.FunctionProvider;

/**
 * Workflow Factory that stores workflows in a database. The database requires a
 * property called 'datasource' which is the JNDI name of the datasource for
 * this factory.
 * <p>
 * Also required is a database table called OS_WORKFLOWDEFS with two columns,
 * WF_NAME which contains the workflow name, and WF_DEFINITION which will
 * contain the xml workflow descriptor, the latter can be either a TEXT or
 * BINARY type.
 * <p>
 * Note that this class is provided as an example, and users are encouraged to
 * use their own implementations that are more suited to their particular needs.
 * 
 * @author Hubert Felber, Philipp Hug Date: May 01, 2003 Time: 11:17:06 AM
 */
public class JDBCWorkflowFactory extends XMLWorkflowFactory implements FunctionProvider {
	// ~ Static fields/initializers
	// /////////////////////////////////////////////

	private static final Log log = LogFactory.getLog(JDBCWorkflowFactory.class);
	final static String wfTable = "OS_WORKFLOWDEFS";
	final static String wfName = "WF_NAME";
	final static String wfDefinition = "WF_DEFINITION";
	final static String wfBranch = "branch_id";

	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	protected DataSource dataSource;
	protected Map workflows;
	protected boolean reload;

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public WorkflowDescriptor getWorkflowBAK(String name, boolean validate) throws FactoryException {
		WfConfig c = (WfConfig) workflows.get(name);

		if (c == null) {
			throw new RuntimeException("Unknown workflow name \"" + name + "\"");
		}

		if (log.isDebugEnabled()) {
			log.debug("getWorkflow " + name + " descriptor=" + c.descriptor);
		}

		if (c.descriptor != null) {
			if (reload) {
				// @todo check timestamp
				try {
					c.descriptor = load(c.wfName, validate);
				} catch (FactoryException e) {
					throw e;
				} catch (Exception e) {
					throw new FactoryException("Error reloading workflow", e);
				}
			}
		} else {
			try {
				c.descriptor = load(c.wfName, validate);
			} catch (FactoryException e) {
				throw e;
			} catch (Exception e) {
				throw new FactoryException("Error loading workflow", e);
			}
		}

		return c.descriptor;
	}

	public WorkflowDescriptor getWorkflow(String name, boolean validate) throws FactoryException {

		if (reload) {
			// @todo check timestamp
			try {
				return load(name, validate);
			} catch (FactoryException e) {
				throw e;
			} catch (Exception e) {
				throw new FactoryException("Error reloading workflow", e);
			}
		}

		return null;
	}

	public String[] getWorkflowNames() {
		int i = 0;
		String[] res = new String[workflows.keySet().size()];
		Iterator it = workflows.keySet().iterator();

		while (it.hasNext()) {
			res[i++] = (String) it.next();
		}

		return res;
	}

	public void execute(Map transientVars, Map args, PropertySet ps) {
		String name = (String) args.get("name");
		WorkflowDescriptor wfds = (WorkflowDescriptor) transientVars.get("descriptor");

		try {
			saveWorkflow(name, wfds, false);
		} catch (Exception e) {
		}
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Return the DataSource to be used by the SessionFactory.
	 */
	public DataSource getDataSource() {
		return this.dataSource;
	}

	public void initDone() throws FactoryException {
		Connection conn = null;

		try {
			init();
			reload = getProperties().getProperty("reload", "false").equalsIgnoreCase("true");

			conn = dataSource.getConnection();

			PreparedStatement ps = conn.prepareStatement("SELECT " + wfName + "," + wfDefinition + " FROM " + wfTable);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				String name = rs.getString(1);
				WfConfig config = new WfConfig(name);
				workflows.put(rs.getString(1), config);
			}

			rs.close();
			ps.close();
		} catch (Exception e) {
			throw new FactoryException("Could not read workflow names from datasource", e);
		} finally {
			try {
				conn.close();
			} catch (Exception ex) {
			}
		}
	}

	public byte[] read(String workflowname) throws SQLException {
		byte[] wf = new byte[0];

		Connection conn = null;

		try {
			conn = dataSource.getConnection();

			PreparedStatement ps = conn.prepareStatement("SELECT " + wfDefinition + " FROM " + wfTable + " WHERE " + wfName + " = ?");
			ps.setString(1, workflowname);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				wf = rs.getBytes(1);
			}

			rs.close();
			ps.close();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception ex) {
				}
			}
		}

		return wf;
	}

	public boolean removeWorkflow(String name) throws FactoryException {
		boolean removed = false;

		try {
			Connection conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement("DELETE FROM " + wfTable + " WHERE " + wfName + " = ?");
			ps.setString(1, name);

			int rows = ps.executeUpdate();

			if (rows == 1) {
				removed = true;
				workflows.remove(name);
			}

			ps.close();
			conn.close();
		} catch (SQLException e) {
			throw new FactoryException("Unable to remove workflow: " + e.toString(), e);
		}

		return removed;
	}

	public boolean saveWorkflow(String name, WorkflowDescriptor descriptor, boolean replace) throws FactoryException {
		WfConfig c = (WfConfig) workflows.get(name);

		if ((c != null) && !replace) {
			return false;
		}

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		Writer out = new OutputStreamWriter(bout);

		PrintWriter writer = new PrintWriter(out);
		writer.println(WorkflowDescriptor.XML_HEADER);
		writer.println(WorkflowDescriptor.DOCTYPE_DECL);
		descriptor.writeXML(writer, 0);
		writer.flush();
		writer.close();

		// @todo is a backup necessary?
		try {
			return write(name, bout.toByteArray());
		} catch (SQLException e) {
			throw new FactoryException("Unable to save workflow: " + e.toString(), e);
		} finally {
			WfConfig config = new WfConfig(name);
			workflows.put(name, config);
		}
	}

	public boolean write(String workflowname, byte[] wf) throws SQLException {
		boolean written = false;
		Connection conn = null;

		try {
			conn = dataSource.getConnection();

			PreparedStatement ps;

			if (exists(workflowname, conn)) {
				ps = conn.prepareStatement("UPDATE " + wfTable + " SET " + wfDefinition + " = ?" + "WHERE " + wfName + "= ?");

				try {
					ps.setBytes(1, wf);
				} catch (Exception e) {
				}

				ps.setString(2, workflowname);
			} else {
				ps = conn.prepareStatement("INSERT INTO " + wfTable + " (" + wfName + ", " + wfDefinition + ") VALUES (?, ?)");
				ps.setString(1, workflowname);

				try {
					ps.setBytes(2, wf);
				} catch (Exception e) {
				}
			}

			ps.executeUpdate();
			ps.close();
			conn.close();
			written = true;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
		}

		return written;
	}

	private boolean exists(String workflowname, Connection conn) {
		boolean exists = false;

		try {
			PreparedStatement ps = conn.prepareStatement("SELECT " + wfName + " FROM " + wfTable + " WHERE " + wfName + " = ?");
			ps.setString(1, workflowname);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				exists = true;
			}

			rs.close();
			ps.close();
		} catch (SQLException e) {
			log.fatal("Could not check if [" + workflowname + "] exists", e);
		}

		return exists;
	}

	private void init() {
		workflows = new HashMap();

		// ds = (DataSource) new
		// InitialContext().lookup(getProperties().getProperty("datasource"));
	}

	private WorkflowDescriptor load(final String wfName, boolean validate) throws IOException, FactoryException, Exception {
		byte[] wf;

		try {
			wf = read(wfName);
		} catch (SQLException e) {
			throw new FactoryException("Error loading workflow:" + e, e);
		}

		ByteArrayInputStream is = new ByteArrayInputStream(wf);

		return WorkflowLoader.load(is, validate);
	}

	// ~ Inner Classes
	// //////////////////////////////////////////////////////////

	class WfConfig {
		String wfName;
		WorkflowDescriptor descriptor;
		long lastModified;

		public WfConfig(String name) {
			wfName = name;
		}
	}
}
