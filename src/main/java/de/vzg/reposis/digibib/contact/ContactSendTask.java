/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vzg.reposis.digibib.contact;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import de.vzg.reposis.digibib.contact.ContactRequestService;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestState;
import de.vzg.reposis.digibib.contact.model.ContactRecipient;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.MCRMailer.EMail;
import org.mycore.common.MCRMailer.EMail.MessagePart;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.MCRTransactionHelper;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRXSL2XMLTransformer;
import org.mycore.common.xsl.MCRParameterCollector;
import org.xml.sax.SAXException;

public class ContactSendTask implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String SENDER_NAME = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "Email.SenderName");

    private static final String MAIL_STYLESHEET = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "Email.Stylesheet");

    private static final String ENCODING = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "SMTP.Encoding");

    private static final String HOST = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "SMTP.Host");

    private static final String PORT = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "SMTP.Port");

    private static final String PROTOCOL = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "SMTP.Protocol");

    private static final String STARTTLS = MCRConfiguration2
            .getString(ContactConstants.CONF_PREFIX + "SMTP.STARTTLS").orElse("disabled");

    private static final String USER = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "SMTP.Auth.User");

    private static final String PASSWORD = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "SMTP.Auth.Password");

    private static final Boolean DEBUG = MCRConfiguration2
            .getBoolean(ContactConstants.CONF_PREFIX + "SMTP.Debug").orElse(false);

    private static final Session session;

    static {
        session = Session.getDefaultInstance(getProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USER, PASSWORD);
            }
        });
        session.setDebug(DEBUG);
    }

    private final ContactRequest request;

    public ContactSendTask(ContactRequest request) {
        this.request = request;
    }

    @Override
    public void run() {
        LOGGER.info("Sending contact request: {}", request.getId());
        MCRSessionMgr.unlock();
        final MCRSession session = MCRSessionMgr.getCurrentSession();
        session.setUserInformation(MCRSystemUserInformation.getJanitorInstance());
        final EMail baseMail = new EMail();
        final Map<String, String> properties = new HashMap();
        properties.put("email", request.getSender());
        properties.put("id", request.getObjectID().toString());
        properties.put("message", request.getMessage());
        properties.put("name", request.getName());
        properties.put("title", request.getObjectID().toString());
        final String orcid = request.getORCID();
        if (orcid != null) {
            properties.put("orcid", orcid);
        }
        final Map<String, String> headers = new HashMap();
        headers.put(ContactConstants.REQUEST_HEADER_NAME, request.getUuid().toString());
        try {
            final Element mailElement = transform(baseMail.toXML(), MAIL_STYLESHEET, properties).getRootElement();
            final EMail mail = EMail.parseXML(mailElement);
            for (ContactRecipient recipient : request.getRecipients().stream().filter(r -> r.isEnabled() && !r.isSent()).collect(Collectors.toList())) {
                final String to = recipient.getEmail();
                sendMail(mail, SENDER_NAME, to, headers);
                recipient.setSent(true);
                MCRTransactionHelper.beginTransaction();
                try {
                    ContactRequestService.getInstance().updateRecipient(recipient);
                    MCRTransactionHelper.commitTransaction();
                } catch (Exception e) {
                    LOGGER.error(e);
                    try {
                        MCRTransactionHelper.rollbackTransaction();
                    } catch (Exception rollbackExc) {
                        LOGGER.error("Error while rollbacking transaction.", rollbackExc);
                    }
                }
            }
            if (request.isSendCopy()) {
                // TODO send copy
            }
            request.setState(ContactRequestState.SENT);
        } catch (Exception e) {
            request.setComment(e.getMessage());
            request.setState(ContactRequestState.SENDING_FAILED);
        } finally {
            MCRTransactionHelper.beginTransaction();
            try {
                ContactRequestService.getInstance().updateRequest(request);
                MCRTransactionHelper.commitTransaction();
            } catch (Exception e) {
                LOGGER.error(e);
                try {
                    MCRTransactionHelper.rollbackTransaction();
                } catch (Exception rollbackExc) {
                    LOGGER.error("Error while rollbacking transaction.", rollbackExc);
                }
            }
        }
        session.close();
    }

    private void sendMail(EMail mail, String from, String to, Map<String, String> headers)
            throws MessagingException {
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSentDate(new Date());
        msg.setSubject(mail.subject, ENCODING);
        Optional<MessagePart> plainMsg = mail.getTextMessage();
        if (plainMsg.isPresent()) {
            msg.setText(plainMsg.get().message, ENCODING);
        }
        if (headers != null) {
            for (var entry : headers.entrySet()) {
                msg.setHeader(entry.getKey(), entry.getValue());
            }
        }
        Transport.send(msg);
    }

    private static Document transform(Document input, String stylesheet, Map<String, String> parameters)
        throws IOException, JDOMException, SAXException {
        MCRJDOMContent source = new MCRJDOMContent(input);
        MCRXSL2XMLTransformer transformer = MCRXSL2XMLTransformer.getInstance("xsl/" + stylesheet + ".xsl");
        MCRParameterCollector parameterCollector = MCRParameterCollector.getInstanceFromUserSession();
        parameterCollector.setParameters(parameters);
        return transformer.transform(source, parameterCollector).asXML();
    }

    private static Properties getProperties() {
        final Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", HOST);
        properties.setProperty("mail.smtp.port", PORT);
        properties.setProperty("mail.transport.protocol", PROTOCOL);
        if ("enabled".equals(STARTTLS)) {
            properties.setProperty("mail.smtp.starttls.enabled", "true");
        } else if ("required".equals(STARTTLS)) {
            properties.setProperty("mail.smtp.starttls.enabled", "true");
            properties.setProperty("mail.smtp.starttls.required", "true");
        }
        properties.setProperty("mail.smtp.auth", "true");
        return properties;
    }
}
