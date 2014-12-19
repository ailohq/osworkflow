/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.loader;

import com.opensymphony.workflow.FactoryException;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Hani Suleiman Date: Dec 17, 2004 Time: 12:00:36 AM
 */
public class HTTPWorkflowFactory extends AbstractWorkflowFactory {
	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	protected Map workflows;
	protected boolean reload;

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
		return null;
	}

	public WorkflowDescriptor getWorkflow(String name, boolean validate) throws FactoryException {
		HTTPWorkflowConfig c = (HTTPWorkflowConfig) workflows.get(name);

		if (c == null) {
			throw new FactoryException("Unknown workflow name \"" + name + '\"');
		}

		if (c.descriptor != null) {
			loadWorkflow(c);
		}

		c.descriptor.setName(name);

		return c.descriptor;
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

	public void initDone() throws FactoryException {
	}

	public boolean removeWorkflow(String name) throws FactoryException {
		throw new FactoryException("remove workflow not supported");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.opensymphony.workflow.loader.AbstractWorkflowFactory#renameWorkflow
	 * (java.lang.String, java.lang.String)
	 */
	public void renameWorkflow(String oldName, String newName) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.workflow.loader.AbstractWorkflowFactory#save()
	 */
	public void save() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.opensymphony.workflow.loader.AbstractWorkflowFactory#saveWorkflow
	 * (java.lang.String, com.opensymphony.workflow.loader.WorkflowDescriptor,
	 * boolean)
	 */
	public boolean saveWorkflow(String name, WorkflowDescriptor descriptor, boolean replace) throws FactoryException {
		HTTPWorkflowConfig c = (HTTPWorkflowConfig) workflows.get(name);

		if ((c != null) && !replace) {
			return false;
		}

		if (c == null) {
			throw new UnsupportedOperationException("Saving of new workflow is not currently supported");
		}

		Writer out;

		// [KAP] comment this line to disable all the validation while saving a
		// workflow
		// descriptor.validate();
		try {
			out = new OutputStreamWriter(null, "utf-8");
		} catch (UnsupportedEncodingException ex) {
			throw new FactoryException("utf-8 encoding not supported, contact your JVM vendor!");
		}

		writeXML(descriptor, out);

		// write it out to a new file, to ensure we don't end up with a messed
		// up file if we're interrupted halfway for some reason
		// now lets rename
		return true;
	}

	protected static String get(String urlValue, Map data) throws IOException {
		BufferedReader input;

		StringBuffer value = new StringBuffer(urlValue);

		if (data.size() > 0) {
			if (value.indexOf("?") == -1) {
				value.append("?");
			} else {
				value.append("&");
			}
		}

		Iterator i = data.entrySet().iterator();

		while (i.hasNext()) {
			Map.Entry entry = (Map.Entry) i.next();
			value.append(entry.getKey()).append('=');
			value.append(URLEncoder.encode((String) entry.getValue(), "utf-8"));

			if (i.hasNext()) {
				value.append("&");
			}
		}

		URL url = new URL(urlValue);
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);
		connection.setUseCaches(false);

		input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		StringBuffer output = new StringBuffer();
		String line;

		while (null != (line = input.readLine())) {
			output.append(line).append('\n');
		}

		input.close();

		return output.toString();
	}

	protected static String post(String urlValue, Map data) throws IOException {
		BufferedReader input;

		URL url = new URL(urlValue);
		URLConnection connection = url.openConnection();
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		DataOutputStream out = new DataOutputStream(connection.getOutputStream());

		StringBuffer content = new StringBuffer();
		Iterator i = data.entrySet().iterator();

		while (i.hasNext()) {
			Map.Entry entry = (Map.Entry) i.next();
			content.append(entry.getKey()).append('=');
			content.append(URLEncoder.encode((String) entry.getValue(), "utf-8"));

			if (i.hasNext()) {
				content.append("&");
			}
		}

		out.writeBytes(content.toString());
		out.flush();
		out.close();

		input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		StringBuffer output = new StringBuffer();
		String line;

		while (null != (line = input.readLine())) {
			output.append(line).append('\n');
		}

		input.close();

		return output.toString();
	}

	protected String readLayoutBuffer(final String url, final String docId) throws Exception {
		Map map = new HashMap();
		map.put("docId", docId);
		map.put("command", "layout");

		return get(url, map);
	}

	protected String readWorkflowBuffer(final String url, final String docId) throws Exception {
		Map map = new HashMap();
		map.put("docId", docId);
		map.put("command", "workflow");

		return get(url, map);
	}

	protected String writeWorkflowDescriptor(final String url, final String docId, final String name, final String workflowXML) throws Exception {
		String ret = null;

		Map map = new HashMap();
		map.put("docId", docId);
		map.put("data", workflowXML);
		map.put("command", "workflow");

		return post(url, map);
	}

	protected String writeWorkflowLayout(final String url, final String docId, final String name, final String layoutXML) throws Exception {
		Map map = new HashMap();
		map.put("docId", docId);
		map.put("data", layoutXML);
		map.put("command", "layout");

		return post(url, map);
	}

	protected void writeXML(WorkflowDescriptor descriptor, Writer out) {
		PrintWriter writer = new PrintWriter(new BufferedWriter(out));
		writer.println(WorkflowDescriptor.XML_HEADER);
		writer.println(WorkflowDescriptor.DOCTYPE_DECL);
		descriptor.writeXML(writer, 0);
		writer.flush();
		writer.close();
	}

	private void loadWorkflow(HTTPWorkflowConfig c) throws FactoryException {
		/*
		 * try { c.descriptor = WorkflowLoader.load(c.url); } catch (Exception
		 * e) { throw new FactoryException("Error in workflow descriptor: " +
		 * c.url, e); }
		 */
	}

	// ~ Inner Classes
	// //////////////////////////////////////////////////////////

	static class HTTPWorkflowConfig {
		String docId;
		String name;
		String service_addr;
		WorkflowDescriptor descriptor;

		// long lastModified;
		public HTTPWorkflowConfig(String service_addr, String name, String docId) {
			this.service_addr = service_addr;
			this.name = name;
			this.docId = docId;
		}
	}

	@Override
	public WorkflowDescriptor getWorkflowFromXml(String xml) throws FactoryException {
		throw new FactoryException("Not implemented");
	}
}
