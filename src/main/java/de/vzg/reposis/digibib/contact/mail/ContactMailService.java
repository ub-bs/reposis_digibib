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

package de.vzg.reposis.digibib.contact.mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.MCRMailer.EMail;
import org.mycore.common.MCRMailer.EMail.MessagePart;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRXSL2XMLTransformer;
import org.mycore.common.xsl.MCRParameterCollector;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.xml.sax.SAXException;

import com.sun.mail.dsn.MultipartReport;

import de.vzg.reposis.digibib.contact.ContactConstants;
import de.vzg.reposis.digibib.contact.exception.ContactException;
import de.vzg.reposis.digibib.contact.model.ContactPerson;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestBody;
import jakarta.mail.Authenticator;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.FlagTerm;

/**
 * This class implements a service to send mails via SMTP.
 */
public class ContactMailService {

    private static final String CONF_PREFIX = ContactConstants.CONF_PREFIX + "Mail.";

    private static final String ENCODING = MCRConfiguration2.getStringOrThrow(CONF_PREFIX + "Encoding");

    private static final String HOST = MCRConfiguration2.getStringOrThrow(CONF_PREFIX + "Host");

    private static final String PORT = MCRConfiguration2.getStringOrThrow(CONF_PREFIX + "Port");

    private static final String PROTOCOL = MCRConfiguration2.getStringOrThrow(CONF_PREFIX + "Protocol");

    private static final String STARTTLS = MCRConfiguration2.getString(CONF_PREFIX + "STARTTLS").orElse("disabled");

    private static final String USER = MCRConfiguration2.getStringOrThrow(CONF_PREFIX + "Auth.User");

    private static final String PASSWORD = MCRConfiguration2.getStringOrThrow(CONF_PREFIX + "Auth.Password");

    private static final Boolean DEBUG = MCRConfiguration2.getBoolean(CONF_PREFIX + "Debug").orElse(false);

    private static final String SENDER_NAME = MCRConfiguration2.getStringOrThrow(CONF_PREFIX + "SenderName");

    private static final String REPORT_MIMETYPE = "multipart/report";

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Session mailSession = createSession();

    private static final String MAIL_STYLESHEET = MCRConfiguration2
        .getStringOrThrow(ContactConstants.CONF_PREFIX + "RequestMail.Stylesheet");

    private static final String FORWARDING_CONFIRMATION_STYLESHEET = MCRConfiguration2
        .getStringOrThrow(ContactConstants.CONF_PREFIX + "ForwardingConfirmationMail.Stylesheet");

    private static final String NEW_REQUEST_STYLESHEET = MCRConfiguration2
        .getStringOrThrow(ContactConstants.CONF_PREFIX + "NewRequestMail.Stylesheet");

    private static final String RECEIPT_CONFIRMATION_STYLESHEET = MCRConfiguration2
        .getStringOrThrow(ContactConstants.CONF_PREFIX + "ReceiptConfirmationMail.Stylesheet");

    private static final String FALLBACK_MAIL = MCRConfiguration2
        .getStringOrThrow(ContactConstants.CONF_PREFIX + "FallbackRecipient.Mail");

    public static List<MimeMessage> fetchUnseenDsnMessages(String folderName) throws MessagingException {
        final Store store = mailSession.getStore("imaps");
        store.connect(HOST, USER, PASSWORD);
        final Folder inbox = store.getFolder(folderName);
        inbox.open(Folder.READ_WRITE);
        final List<Message> unreadMessages
            = Arrays.asList(inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false)));

        final List<MimeMessage> dsnMessages = new ArrayList<>();
        for (Message message : unreadMessages) {
            if (message.isMimeType(REPORT_MIMETYPE)) {
                MultipartReport report = null;
                try {
                    report = (MultipartReport) ((MimeMessage) message).getContent();
                } catch (IOException | MessagingException e) {
                    LOGGER.error("Error while retrieving report");
                }
                final MimeMessage dsnMessage = report.getReturnedMessage();
                if (dsnMessage != null) {
                    dsnMessages.add(dsnMessage);
                }
            }
        }
        return dsnMessages;
    }

    /**
     * Creates Email for recipient and sends it.
     *
     * @param request request
     * @param contactPerson recipient
     */
    public static void sendRequestMailToRecipient(ContactRequest request, ContactPerson contactPerson) {
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put(ContactConstants.REQUEST_HEADER_NAME, request.getId().toString());
        final EMail mail = createMail(request, contactPerson);
        sendMail(mail, contactPerson.getMail(), headers);
    }

    public static void sendRequestForwardedMail(ContactRequest request) {
        final EMail mail = createForwardConfirmationMail(request.getObjectId(), request.getBody());
        sendMail(mail, request.getBody().fromMail());
    }

    public static void sendConfirmationMail(ContactRequest request) {
        final EMail mail = createConfirmationMail(request.getObjectId(), request.getBody());
        sendMail(mail, request.getBody().fromMail());
    }

    public static void sendNewRequestMail(ContactRequest request) {
        final EMail mail = createNotificationMail(request.getObjectId().toString());
        sendMail(mail, FALLBACK_MAIL);
    }

    private static EMail createConfirmationMail(MCRObjectID objectId, ContactRequestBody requestBody) {
        final EMail baseMail = new EMail();
        final Map<String, String> properties = new HashMap<String, String>();
        properties.put("id", objectId.toString());
        properties.put("message", requestBody.message());
        properties.put("name", requestBody.fromName());
        Optional.ofNullable(requestBody.fromOrcid()).ifPresent(o -> properties.put("orcid", o));
        final Element mailElement = transform(baseMail.toXML(), RECEIPT_CONFIRMATION_STYLESHEET, properties)
            .getRootElement();
        return EMail.parseXML(mailElement);
    }

    private static EMail createMail(ContactRequest request, ContactPerson contactPerson) {
        final EMail baseMail = new EMail();
        final Map<String, String> properties = new HashMap<String, String>();
        properties.put("email", request.getBody().fromMail());
        properties.put("id", request.getObjectId().toString());
        properties.put("message", request.getBody().message());
        properties.put("name", request.getBody().fromName());
        properties.put("recipientName", contactPerson.getName());
        properties.put("recipientMail", contactPerson.getMail());
        properties.put("requestID", request.getId().toString());
        properties.put("title", request.getObjectId().toString());
        Optional.ofNullable(request.getBody().fromOrcid()).ifPresent(o -> properties.put("orcid", o));
        Optional.ofNullable(request.getComment()).ifPresent(c -> properties.put("comment", c));

        final Element mailElement = transform(baseMail.toXML(), MAIL_STYLESHEET, properties).getRootElement();
        return EMail.parseXML(mailElement);
    }

    private static EMail createForwardConfirmationMail(MCRObjectID objectId, ContactRequestBody request) {
        final EMail forwardConfirmation = new EMail();
        final Map<String, String> properties = new HashMap<String, String>();
        properties.put("id", objectId.toString());
        properties.put("name", request.fromName());
        final Element mailElement = transform(forwardConfirmation.toXML(), FORWARDING_CONFIRMATION_STYLESHEET,
            properties).getRootElement();
        return EMail.parseXML(mailElement);
    }

    private static Document transform(Document input, String stylesheet, Map<String, String> parameters) {
        MCRJDOMContent source = new MCRJDOMContent(input);
        MCRXSL2XMLTransformer transformer = MCRXSL2XMLTransformer.getInstance("xsl/" + stylesheet + ".xsl");
        MCRParameterCollector parameterCollector = MCRParameterCollector.getInstanceFromUserSession();
        parameterCollector.setParameters(parameters);
        try {
            return transformer.transform(source, parameterCollector).asXML();
        } catch (IOException | JDOMException | SAXException e) {
            throw new ContactException("Cannot transform document", e);
        }
    }

    private static EMail createNotificationMail(String id) {
        final EMail baseMail = new EMail();
        final Map<String, String> properties = new HashMap<String, String>();
        properties.put("id", id);
        final Element mailElement = transform(baseMail.toXML(), NEW_REQUEST_STYLESHEET, properties).getRootElement();
        return EMail.parseXML(mailElement);
    }

    private static void sendMail(EMail mail, String to) {
        sendMail(mail, to, Collections.emptyMap());
    }

    private static void sendMail(EMail mail, String to, Map<String, String> headers) {
        final MimeMessage msg = new MimeMessage(mailSession);
        try {
            msg.setFrom(new InternetAddress(SENDER_NAME));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSentDate(new Date());
            msg.setSubject(mail.subject, ENCODING);
            final Optional<MessagePart> plainMsg = mail.getTextMessage();
            if (plainMsg.isPresent()) {
                msg.setText(plainMsg.get().message, ENCODING);
            }
            for (var entry : headers.entrySet()) {
                msg.setHeader(entry.getKey(), entry.getValue());
            }
            Transport.send(msg);
        } catch (MessagingException e) {
            throw new ContactException("Cannot send mail", e);
        }
    }

    private static Session createSession() {
        final Session session = Session.getDefaultInstance(getProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USER, PASSWORD);
            }
        });
        session.setDebug(DEBUG);
        return session;
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
