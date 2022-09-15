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

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.mycore.common.MCRMailer.EMail;
import org.mycore.common.MCRMailer.EMail.MessagePart;

import org.mycore.common.config.MCRConfiguration2;

public class ContactMailService {

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


    public static void sendMail(EMail mail, String from, String to) throws MessagingException {
        sendMail(mail, from, to, null);
    }

    public static void sendMail(EMail mail, String from, String to, Map<String, String> headers) throws MessagingException {
        final MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSentDate(new Date());
        msg.setSubject(mail.subject, ENCODING);
        final Optional<MessagePart> plainMsg = mail.getTextMessage();
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
}
