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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRMailer.EMail;
import org.mycore.common.MCRMailer.EMail.MessagePart;
import org.mycore.common.config.MCRConfiguration2;

public class ContactMailService {

    private static final Logger LOGGER = LogManager.getLogger();

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

    private Session session;

    private ContactMailService() {
        session = Session.getDefaultInstance(getProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USER, PASSWORD);
            }
        });
        session.setDebug(DEBUG);
    }

    private Properties getProperties() {
        final Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", HOST);
        properties.setProperty("mail.smtp.port", PORT);
        properties.setProperty("mail.store.protocol", "imaps"); // TODO property
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

    public static ContactMailService getInstance() {
        return Holder.INSTANCE;
    }
    public void sendMail(EMail mail) throws UnsupportedEncodingException, MessagingException, MalformedURLException {
        sendMail(mail, null);
    }

    public void flagMessageAsSeen(Message message) throws MessagingException {
        message.setFlag(Flags.Flag.SEEN, true);
    }

    public Message[] getUnreadMessages() throws MessagingException {
        final Store store = session.getStore();
        store.connect(HOST, USER, PASSWORD);

        final Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        final Message[] unreadMessages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

        return unreadMessages;
    }

    public void sendMail(EMail mail, Map<String, String> headers) throws UnsupportedEncodingException, MessagingException, MalformedURLException {
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(buildAddress(mail.from));
        if (mail.to != null) {
            List<InternetAddress> toList = buildAddressList(mail.to);
            msg.addRecipients(Message.RecipientType.TO, toList.toArray(new InternetAddress[toList.size()]));
        }
        if (mail.replyTo != null) {
            List<InternetAddress> replyToList = buildAddressList(mail.replyTo);
            msg.setReplyTo((replyToList.toArray(new InternetAddress[replyToList.size()])));
        }
        if (mail.bcc != null) {
            List<InternetAddress> bccList = buildAddressList(mail.bcc);
            msg.addRecipients(Message.RecipientType.BCC,
                bccList.toArray(new InternetAddress[bccList.size()]));
        }
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

    private static InternetAddress buildAddress(String s) throws AddressException, UnsupportedEncodingException {
        if (!s.endsWith(">")) {
            return new InternetAddress(s.trim());
        }
        String name = s.substring(0, s.lastIndexOf("<")).trim();
        String addr = s.substring(s.lastIndexOf("<") + 1, s.length() - 1).trim();
        if (name.startsWith("\"") && name.endsWith("\"")) {
            name = name.substring(1, name.length() - 1);
        }
        return new InternetAddress(addr, name);
    }

    private static List<InternetAddress> buildAddressList(List<String> addresses)
            throws AddressException, UnsupportedEncodingException {
        List<InternetAddress> internetAdresses = new ArrayList();
        for (String address : addresses) {
            internetAdresses.add(buildAddress(address));
        }
        return internetAdresses;
    }

    private static class Holder {
        static final ContactMailService INSTANCE = new ContactMailService();
    }
}
