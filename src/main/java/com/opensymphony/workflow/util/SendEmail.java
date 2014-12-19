/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.config.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

/**
 * Sends an email to a group of users. The following arguments are expected:
 * <p/>
 * <ul>
 * <li>to - comma seperated list of email addresses</li>
 * <li>from - single email address</li>
 * <li>subject - the message subject</li>
 * <li>cc - comma seperated list of email addresses (optional)</li>
 * <li>message - the message body</li>
 * <li>smtpHost - the SMTP host that will relay the message</li>
 * <li>parseVariables - if 'true', then variables of the form ${} in subject,
 * message, to, and cc fields will be parsed</li>
 * </ul>
 *
 * @author <a href="mailto:plightbo@hotmail.com">Pat Lightbody</a>
 */
public class SendEmail implements FunctionProvider {
    // ~ Static fields/initializers
    // /////////////////////////////////////////////

    private static final Log log = LogFactory.getLog(SendEmail.class);

    // ~ Methods
    // ////////////////////////////////////////////////////////////////

    public void execute(Map transientVars, Map args, PropertySet ps) {
        String to = (String) args.get("to");
        String from = (String) args.get("from");
        String subject = (String) args.get("subject");
        String cc = (String) args.get("cc");
        String m = (String) args.get("message");
        String smtpHost = (String) args.get("smtpHost");
        boolean parseVariables = "true".equals(args.get("parseVariables"));
        Configuration config = (Configuration) transientVars.get("configuration");

        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);

            Session sendMailSession = Session.getInstance(props, null);
            Transport transport = sendMailSession.getTransport("smtp");
            Message message = new MimeMessage(sendMailSession);

            message.setFrom(new InternetAddress(from));

            Set toSet = new HashSet();
            VariableResolver variableResolver = config.getVariableResolver();
            StringTokenizer st = new StringTokenizer(parseVariables ? variableResolver.translateVariables(to, transientVars, ps).toString() : to, ", ");

            while (st.hasMoreTokens()) {
                String user = st.nextToken();
                toSet.add(new InternetAddress(user));
            }

            message.setRecipients(Message.RecipientType.TO, (InternetAddress[]) toSet.toArray(new InternetAddress[toSet.size()]));

            Set ccSet = null;

            if (cc != null) {
                ccSet = new HashSet();

                if (parseVariables) {
                    cc = variableResolver.translateVariables(cc, transientVars, ps).toString();
                }

                st = new StringTokenizer(cc, ", ");

                while (st.hasMoreTokens()) {
                    String user = st.nextToken();
                    ccSet.add(new InternetAddress(user));
                }
            }

            if ((ccSet != null) && (ccSet.size() > 0)) {
                message.setRecipients(Message.RecipientType.CC, (InternetAddress[]) ccSet.toArray(new InternetAddress[ccSet.size()]));
            }

            message.setSubject(parseVariables ? variableResolver.translateVariables(subject, transientVars, ps).toString() : subject);
            message.setSentDate(new Date());
            message.setText(parseVariables ? variableResolver.translateVariables(m, transientVars, ps).toString() : m);
            message.saveChanges();

            transport.connect();
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (MessagingException e) {
            log.error("Error sending email:", e);
        }
    }
}
