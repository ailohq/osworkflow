/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opensymphony.workflow.loader.ClassLoaderUtil;

/**
 * Date: Aug 3, 2004 Time: 11:04:43 PM
 * 
 * @author hani
 */
public class TypeResolver {
	// ~ Static fields/initializers
	// /////////////////////////////////////////////

	private static final Log log = LogFactory.getLog(TypeResolver.class);
	private static TypeResolver resolver = new TypeResolver();

	// ~ Instance fields
	// ////////////////////////////////////////////////////////

	protected Map conditions = new HashMap();
	protected Map functions = new HashMap();
	protected Map registers = new HashMap();
	protected Map validators = new HashMap();

	// ~ Constructors
	// ///////////////////////////////////////////////////////////

	public TypeResolver() {
		validators.put("remote-ejb", "com.opensymphony.workflow.util.ejb.remote.RemoteEJBValidator");
		validators.put("local-ejb", "com.opensymphony.workflow.util.ejb.local.LocalEJBValidator");
		validators.put("jndi", "com.opensymphony.workflow.util.jndi.JNDIValidator");
		validators.put("beanshell", "com.opensymphony.workflow.util.beanshell.BeanShellValidator");
		validators.put("bsf", "com.opensymphony.workflow.util.bsf.BSFValidator");
		conditions.put("remote-ejb", "com.opensymphony.workflow.util.ejb.remote.RemoteEJBCondition");
		conditions.put("local-ejb", "com.opensymphony.workflow.util.ejb.local.LocalEJBCondition");
		conditions.put("jndi", "com.opensymphony.workflow.util.jndi.JNDICondition");
		conditions.put("beanshell", "com.opensymphony.workflow.util.beanshell.BeanShellCondition");
		conditions.put("bsf", "com.opensymphony.workflow.util.bsf.BSFCondition");
		registers.put("remote-ejb", "com.opensymphony.workflow.util.ejb.remote.RemoteEJBRegister");
		registers.put("local-ejb", "com.opensymphony.workflow.util.ejb.local.LocalEJBRegister");
		registers.put("jndi", "com.opensymphony.workflow.util.jndi.JNDIRegister");
		registers.put("beanshell", "com.opensymphony.workflow.util.beanshell.BeanShellRegister");
		registers.put("bsf", "com.opensymphony.workflow.util.bsf.BSFRegister");
		functions.put("remote-ejb", "com.opensymphony.workflow.util.ejb.remote.RemoteEJBFunctionProvider");
		functions.put("local-ejb", "com.opensymphony.workflow.util.ejb.local.LocalEJBFunctionProvider");
		functions.put("jndi", "com.opensymphony.workflow.util.jndi.JNDIFunctionProvider");
		functions.put("beanshell", "com.opensymphony.workflow.util.beanshell.BeanShellFunctionProvider");
		functions.put("bsf", "com.opensymphony.workflow.util.bsf.BSFFunctionProvider");
	}

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public Condition getCondition(String type, Map args) throws WorkflowException {
		String className = (String) conditions.get(type);

		if (className == null) {
			className = (String) args.get(Workflow.CLASS_NAME);
		}

		if (className == null) {
			throw new WorkflowException("No type or class.name argument specified to TypeResolver");
		}

		return (Condition) loadObject(className);
	}

	public FunctionProvider getFunction(String type, Map args) throws WorkflowException {
		String className = (String) functions.get(type);

		if (className == null) {
			className = (String) args.get(Workflow.CLASS_NAME);
		}

		if (className == null) {
			throw new WorkflowException("No type or class.name argument specified to TypeResolver");
		}

		return (FunctionProvider) loadObject(className);
	}

	public Register getRegister(String type, Map args) throws WorkflowException {
		String className = (String) registers.get(type);

		if (className == null) {
			className = (String) args.get(Workflow.CLASS_NAME);
		}

		if (className == null) {
			throw new WorkflowException("No type or class.name argument specified to TypeResolver");
		}

		return (Register) loadObject(className);
	}

	public static void setResolver(TypeResolver resolver) {
		TypeResolver.resolver = resolver;
	}

	public static TypeResolver getResolver() {
		return resolver;
	}

	public Validator getValidator(String type, Map args) throws WorkflowException {
		String className = (String) validators.get(type);

		if (className == null) {
			className = (String) args.get(Workflow.CLASS_NAME);
		}

		if (className == null) {
			throw new WorkflowException("No type or class.name argument specified to TypeResolver");
		}

		return (Validator) loadObject(className);
	}

	protected Object loadObject(String clazz) {
		try {
			return ClassLoaderUtil.loadClass(clazz.trim(), getClass()).newInstance();
		} catch (Exception e) {
			log.error("Could not load class '" + clazz + "'",
					e);

			return null;
		}
	}

}
