/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.spi.hibernate;

import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.loader.WorkflowLoader;

import org.springframework.beans.factory.FactoryBean;

import org.springframework.core.io.Resource;

import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class SpringWorkflowDescriptorFactory implements FactoryBean {
	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	/** The descriptor resource. */
	private Resource descriptorResource;

	/** Should the descriptor be reloaded on each call. */
	private boolean reload;

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public final void setDescriptorResource(final Resource inDescriptorResource) {
		this.descriptorResource = inDescriptorResource;
	}

	public Object getObject() throws InvalidWorkflowDescriptorException, SAXException, IOException {
		return WorkflowLoader.load(this.descriptorResource.getInputStream(), false);
	}

	public Class getObjectType() {
		return WorkflowDescriptor.class;
	}

	public final void setReload(final boolean inReload) {
		this.reload = inReload;
	}

	public boolean isSingleton() {
		return !this.reload;
	}
}
