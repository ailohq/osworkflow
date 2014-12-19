/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.loader;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.2 $
 */
public class WorkflowSerializer {
	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	private VelocityEngine engine;

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public byte[] generateWorkflowXML(WorkflowDescriptor wf) throws Exception {
		initVelocity();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(baos);
		Template template = engine.getTemplate("workflowtemplate.vm");
		VelocityContext ctx = new VelocityContext();
		ctx.put("workflow", wf);
		template.merge(ctx, writer);
		writer.flush();
		baos.flush();

		return baos.toByteArray();
	}

	protected void initVelocity() throws Exception {
		if (engine == null) {
			engine = new VelocityEngine();
			engine.setProperty("resource.loader", "class");
			engine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			engine.init();
		}
	}
}
