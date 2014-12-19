/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util;

import com.opensymphony.module.propertyset.PropertySet;

import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.spi.WorkflowEntry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

import javax.jms.*;

import javax.naming.InitialContext;

/**
 * Sends out a JMS TextMessage to a specified Queue or Topic. The following
 * arguments are expected:
 * 
 * <ul>
 * <li>queue-factory-location - the location to be passed to
 * InitialContext.lookup</li>
 * <li>queue-location - the location to be passed to InitialContext.lookup</li>
 * <li>topic-factory-location - the location to be passed to
 * InitialContext.lookup</li>
 * <li>topic-location - the location to be passed to InitialContext.lookup</li>
 * <li>text - the text message to be included in this JMS message</li>
 * </ul>
 * 
 * Also, please note that the entire set of properties will be passed through to
 * the constructor for InitialContext, meaning that if you need to use an
 * InintialContextFactory other than the default one, you are free to include
 * arguments that will do so.
 * 
 * Also note that all arguments are also passed to the TextMessage using
 * setObjectProperty(), except for "text" which is set using setText(). An extra
 * property is always added to denote the workflow entry for this message. This
 * is stored as a long property, with the name 'workflowEntry'.
 * 
 * @author Hani Suleiman
 */
public class JMSMessage implements FunctionProvider {
	// ~ Static fields/initializers
	// /////////////////////////////////////////////

	private static final Log log = LogFactory.getLog(JMSMessage.class);

	// ~ Methods
	// ////////////////////////////////////////////////////////////////

	public void execute(Map transientVars, Map args, PropertySet ps) {
		WorkflowEntry entry = (WorkflowEntry) transientVars.get("entry");

		try {
			Hashtable env = new Hashtable(args);
			InitialContext initialContext = new InitialContext(env);

			if (args.containsKey("queue-factory-location")) {
				QueueConnectionFactory queueFactory = (QueueConnectionFactory) initialContext.lookup((String) args.get("queue-factory-location"));
				QueueConnection conn = queueFactory.createQueueConnection();
				conn.start();

				QueueSession queueSession = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
				javax.jms.Queue queue = (javax.jms.Queue) initialContext.lookup((String) args.get("queue-location"));
				QueueSender sender = queueSession.createSender(queue);
				TextMessage message = queueSession.createTextMessage();
				populateMessage(message, entry, args);
				sender.send(message);
			} else if (args.containsKey("topic-factory-location")) {
				TopicConnectionFactory topicFactory = (TopicConnectionFactory) initialContext.lookup((String) args.get("topic-factory-location"));
				TopicConnection conn = topicFactory.createTopicConnection();
				conn.start();

				TopicSession topicSession = conn.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
				Topic topic = (Topic) initialContext.lookup((String) args.get("topic-location"));
				TopicPublisher publisher = topicSession.createPublisher(topic);
				TextMessage message = topicSession.createTextMessage();
				populateMessage(message, entry, args);
				publisher.publish(message);
			}
		} catch (Exception ex) {
			log.error("Error sending JMS message", ex);
		}
	}

	private void populateMessage(TextMessage message, WorkflowEntry entry, Map properties) throws JMSException {
		message.setText((String) properties.get("text"));
		message.setLongProperty("workflowEntry", entry.getId());

		for (Iterator iterator = properties.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry mapEntry = (Map.Entry) iterator.next();

			// don't include "text", it was already done
			if (!"text".equals(mapEntry.getKey())) {
				message.setObjectProperty((String) mapEntry.getKey(), mapEntry.getValue());
			}
		}
	}
}
