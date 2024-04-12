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

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.MCRMailer.EMail;
import org.mycore.common.MCRMailer.EMail.MessagePart;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRXSL2XMLTransformer;
import org.mycore.common.xsl.MCRParameterCollector;
import org.xml.sax.SAXException;

import de.vzg.reposis.digibib.contact.ContactConstants;
import de.vzg.reposis.digibib.contact.exception.ContactEmailException;
import de.vzg.reposis.digibib.contact.model.Contact;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
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
public class ContactEmailService {

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

    private static final Session mailSession = createSession();

    private static final String EMAIL_STYLESHEET = MCRConfiguration2
        .getStringOrThrow(ContactConstants.CONF_PREFIX + "RequestMail.Stylesheet");

    private static final String NEW_REQUEST_STYLESHEET = MCRConfiguration2
        .getStringOrThrow(ContactConstants.CONF_PREFIX + "NewRequestMail.Stylesheet");

    private static final String RECEIPT_CONFIRMATION_STYLESHEET = MCRConfiguration2
        .getStringOrThrow(ContactConstants.CONF_PREFIX + "ReceiptConfirmationMail.Stylesheet");

    private static final String FALLBACK_MAIL = MCRConfiguration2
        .getStringOrThrow(ContactConstants.CONF_PREFIX + "FallbackRecipient.Mail");

    public static List<Message> fetchUnseenReportMessages(String folderName) throws MessagingException {
        final Store store = mailSession.getStore("imaps");
        store.connect(HOST, USER, PASSWORD);
        final Folder inbox = store.getFolder(folderName);
        inbox.open(Folder.READ_WRITE);
        final List<Message> unreadMessages
            = Arrays.asList(inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false)));
        final List<Message> reportMessages = new ArrayList<>();
        for (Message message : unreadMessages) {
            if (message.isMimeType(REPORT_MIMETYPE)) {
                reportMessages.add(message);
            }
        }
        return reportMessages;
    }

    /**
     * Creates email and sends it to contact.
     *
     * @param request request
     * @param contact recipient
     * @throws ContactEmailException if an email error occurs
     */
    public static void sendRequestEmail(ContactRequest request, Contact contact) {
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put(ContactConstants.REQUEST_HEADER_NAME, request.getId().toString());
        final EMail email = createEmail(request, contact);
        sendEmail(email, contact.getEmail(), headers);
    }

    /**
     * Creates confirmation email and sends it to from.
     *
     * @param request request
     * @throws ContactEmailException if an email error occurs
     */
    public static void sendConfirmationEmail(ContactRequest request) {
        final EMail email = createConfirmationEmail(request);
        sendMail(email, request.getBody().email());
    }

    /**
     * Creates new request email and sends to stuff.
     *
     * @param request request
     * @throws ContactEmailException if an email error occurs
     */
    public static void sendNewRequestEmail(ContactRequest request) {
        final EMail email = createNotificationEmail(request.getObjectId().toString());
        sendMail(email, FALLBACK_MAIL);
    }

    private static EMail createConfirmationEmail(ContactRequest request) {
        final EMail baseEmail = new EMail();
        final Map<String, String> properties = new HashMap<String, String>();
        properties.put("id", request.getObjectId().toString());
        properties.put("message", request.getBody().message());
        properties.put("name", request.getBody().name());
        properties.put("requestId", request.getId().toString());
        Optional.ofNullable(request.getBody().orcid()).ifPresent(o -> properties.put("orcid", o));
        final Element emailElement = transform(baseEmail.toXML(), RECEIPT_CONFIRMATION_STYLESHEET, properties)
            .getRootElement();
        return EMail.parseXML(emailElement);
    }

    private static EMail createEmail(ContactRequest request, Contact contactPerson) {
        final EMail baseEmail = new EMail();
        final Map<String, String> properties = new HashMap<String, String>();
        properties.put("email", request.getBody().email());
        properties.put("id", request.getObjectId().toString());
        properties.put("message", request.getBody().message());
        properties.put("name", request.getBody().name());
        properties.put("recipientName", contactPerson.getName());
        properties.put("recipientMail", contactPerson.getEmail());
        properties.put("requestId", request.getId().toString());
        properties.put("title", request.getObjectId().toString());
        Optional.ofNullable(request.getBody().orcid()).ifPresent(o -> properties.put("orcid", o));
        Optional.ofNullable(request.getComment()).ifPresent(c -> properties.put("comment", c));
        final Element emailElement = transform(baseEmail.toXML(), EMAIL_STYLESHEET, properties).getRootElement();
        return EMail.parseXML(emailElement);
    }

    private static Document transform(Document input, String stylesheet, Map<String, String> parameters) {
        MCRJDOMContent source = new MCRJDOMContent(input);
        MCRXSL2XMLTransformer transformer = MCRXSL2XMLTransformer.getInstance("xsl/" + stylesheet + ".xsl");
        MCRParameterCollector parameterCollector = MCRParameterCollector.getInstanceFromUserSession();
        parameterCollector.setParameters(parameters);
        try {
            return transformer.transform(source, parameterCollector).asXML();
        } catch (IOException | JDOMException | SAXException e) {
            throw new ContactEmailException("Cannot transform document", e);
        }
    }

    private static EMail createNotificationEmail(String id) {
        final EMail baseEmail = new EMail();
        final Map<String, String> properties = new HashMap<String, String>();
        properties.put("id", id);
        final Element emailElement = transform(baseEmail.toXML(), NEW_REQUEST_STYLESHEET, properties).getRootElement();
        return EMail.parseXML(emailElement);
    }

    private static void sendMail(EMail email, String to) {
        sendEmail(email, to, Collections.emptyMap());
    }

    private static void sendEmail(EMail email, String to, Map<String, String> headers) {
        final MimeMessage msg = new MimeMessage(mailSession);
        try {
            msg.setFrom(new InternetAddress(SENDER_NAME));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSentDate(new Date());
            msg.setSubject(email.subject, ENCODING);
            final Optional<MessagePart> plainMsg = email.getTextMessage();
            if (plainMsg.isPresent()) {
                msg.setText(plainMsg.get().message, ENCODING);
            }
            for (var entry : headers.entrySet()) {
                msg.setHeader(entry.getKey(), entry.getValue());
            }
            Transport.send(msg);
        } catch (MessagingException e) {
            throw new ContactEmailException("Cannot send mail", e);
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
