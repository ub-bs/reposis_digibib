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

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.mycore.common.MCRMailer.EMail;
import org.mycore.common.MCRMailer.EMail.MessagePart;
import org.mycore.common.config.MCRConfiguration2;

import de.vzg.reposis.digibib.contact.exception.ContactException;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

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

    private static final Session mailSession = createSession();

    /**
     * Sends mail without headers.
     *
     * @param mail the mail
     * @param from the sender mail
     * @param to   the recipient mail
     * @throws ContactException if sending fails
     */
    public static void sendMail(EMail mail, String to) {
        sendMail(mail, to, Collections.emptyMap());
    }

    /**
     * Sends mail with given map over header elements.
     *
     * @param mail   mail
     * @param from   sender mail
     * @param to     recipient mail
     * @param headers map over header elements
     * @throws ContactException if sending fails
     */
    public static void sendMail(EMail mail, String to, Map<String, String> headers) {
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
