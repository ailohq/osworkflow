/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.config;

import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Hani Suleiman Date: Dec 17, 2004 Time: 12:21:44 PM
 */
public class WorkflowFactoryServlet extends HttpServlet {
	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	private WorkflowFactory factory;

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public void init() throws ServletException {
		DefaultConfiguration config = new DefaultConfiguration();
		URL url = null;
		String configFile = getInitParameter("config");

		if (configFile != null) {
			try {
				url = new URL(configFile);
			} catch (MalformedURLException e) {
				try {
					url = getServletContext().getResource(configFile);
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
			}
		}

		try {
			config.load(url);
		} catch (FactoryException e) {
			throw new ServletException("Unable to create workflow factory", e);
		}

		this.factory = config.getFactory();
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String command = req.getParameter("command");
		String docId = req.getParameter("docId");

		if ("layout".equals(command)) {
			Object layout = factory.getLayout(docId);

			if (layout != null) {
				resp.setContentType("text/plain");
				resp.getWriter().write(layout.toString());
			}
		} else if ("workflow".equals(command)) {
			try {
				WorkflowDescriptor descriptor = factory.getWorkflow(docId);
				resp.setContentType("text/xml");
				descriptor.writeXML(resp.getWriter(), 0);
			} catch (FactoryException e) {
				e.printStackTrace(resp.getWriter());
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String command = req.getParameter("command");
		String docId = req.getParameter("docId");
		String data = req.getParameter("data");

		if ("layout".equals(command)) {
			factory.setLayout(docId, data.toString());
		} else if ("workflow".equals(command)) {
			boolean replace = "true".equals(req.getParameter("replace"));

			try {
				WorkflowDescriptor descriptor = WorkflowLoader.load(new ByteArrayInputStream(data.getBytes()), false);
				factory.saveWorkflow(docId, descriptor, replace);
			} catch (Exception e) {
				e.printStackTrace(resp.getWriter());
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
	}
}
