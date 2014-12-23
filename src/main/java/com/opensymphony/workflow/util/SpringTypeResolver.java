/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util;

import com.opensymphony.workflow.*;

import org.springframework.beans.BeansException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring-aware type resolver.
 */
public class SpringTypeResolver extends TypeResolver implements ApplicationContextAware {
    // ~ Static fields/initializers
    // /////////////////////////////////////////////

    private static final String BEANNAME = "bean.name";
    private static final String SPRING = "spring";

    // ~ Instance fields
    // ////////////////////////////////////////////////////////

    private ApplicationContext applicationContext;
    private Map springFunctions = new HashMap();

    // ~ Methods
    // ////////////////////////////////////////////////////////////////

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Condition getCondition(String type, Map args) throws WorkflowException {
        if (SPRING.equals(type)) {
            return (Condition) getApplicationContext().getBean((String) args.get(BEANNAME));
        }

        return super.getCondition(type, args);
    }

    public void setConditions(Map conditions) {
        this.conditions = conditions;
    }

    public FunctionProvider getFunction(String type, Map args) throws WorkflowException {
        if (SPRING.equals(type)) {
            return (FunctionProvider) getApplicationContext().getBean((String) args.get(BEANNAME));
        }

        String className = (String) springFunctions.get(type);

        if (className == null) {
            className = (String) args.get(Workflow.CLASS_NAME);
        }

        if (className != null) {
            return (FunctionProvider) loadObject(className);
        }

        return super.getFunction(type, args);
    }

    public void setFunctions(Map functions) {
        this.springFunctions = functions;
    }

    public Register getRegister(String type, Map args) throws WorkflowException {
        if (SPRING.equals(type)) {
            return (Register) getApplicationContext().getBean((String) args.get(BEANNAME));
        }

        return super.getRegister(type, args);
    }

    public void setRegisters(Map registers) {
        this.registers = registers;
    }

    public Validator getValidator(String type, Map args) throws WorkflowException {
        if (SPRING.equals(type)) {
            return (Validator) getApplicationContext().getBean((String) args.get(BEANNAME));
        }

        return super.getValidator(type, args);
    }

    public void setValidators(Map validators) {
        this.validators = validators;
    }

    private ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
