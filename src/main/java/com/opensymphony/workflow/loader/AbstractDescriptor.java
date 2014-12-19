/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.loader;

import com.opensymphony.workflow.util.XMLizable;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

/**
 * User: hani Date: May 28, 2003 Time: 12:44:54 AM
 */
public abstract class AbstractDescriptor implements XMLizable, Serializable {
	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	private AbstractDescriptor parent;
	private boolean hasId = false;
	private int entityId;
	private int id;

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}

	public int getEntityId() {
		return entityId;
	}

	public void setId(int id) {
		this.id = id;
		hasId = true;
	}

	public int getId() {
		return id;
	}

	public void setParent(AbstractDescriptor parent) {
		this.parent = parent;
	}

	public AbstractDescriptor getParent() {
		return parent;
	}

	public String asXML() {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		this.writeXML(writer, 0);
		writer.close();

		return stringWriter.toString();
	}

	public boolean hasId() {
		return hasId;
	}
}
