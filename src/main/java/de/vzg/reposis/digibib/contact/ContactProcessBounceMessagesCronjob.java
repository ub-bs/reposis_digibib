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
import java.util.Properties;
import java.util.UUID;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;

import com.sun.mail.dsn.MultipartReport;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.processing.MCRProcessableStatus;
import org.mycore.mcr.cronjob.MCRCronjob;
import org.mycore.util.concurrent.MCRFixedUserCallable;

/**
 * This class implements a cronjob that proccess bounces mails.
 */
public class ContactProcessBounceMessagesCronjob extends MCRCronjob {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String HOST = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "Mail.Host");

    private static final String USER = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "Mail.Auth.User");

    private static final String PASSWORD = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "Mail.Auth.Password");

    private static final String REPORT_MIMETYPE = "multipart/report";

    /**
     * Mail inbox.
     */
    private Folder inbox = null;

    @Override
    public void runJob() {
        getProcessable().setStatus(MCRProcessableStatus.processing);
        getProcessable().setProgress(0);
        final Session mailSession = Session.getInstance(new Properties());
        Store store = null;
        try {
            store = mailSession.getStore("imaps");
            store.connect(HOST, USER, PASSWORD);
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            final Message[] unreadMessages = getUnreadMessages();
            LOGGER.debug("Found {} unread messages.", unreadMessages.length);
            for (Message message : unreadMessages) {
                if(message instanceof MimeMessage) {
                    final MimeMessage mime = (MimeMessage) message;
                    if (mime.isMimeType(REPORT_MIMETYPE)) {
                        try {
                            final MultipartReport dsn = (MultipartReport) mime.getContent();
                            final MimeMessage m = dsn.getReturnedMessage();
                            if(m != null) {
                                final String requestId = m.getHeader(ContactConstants.REQUEST_HEADER_NAME, null);
                                if (requestId != null) {
                                    final Address[] recipients = m.getRecipients(Message.RecipientType.TO);
                                    if (recipients != null && recipients.length == 1) {
                                        try {
                                            new MCRFixedUserCallable<>(() -> {
                                                ContactService.getInstance()
                                                        .setRecipientFailed(UUID.fromString(requestId), recipients[0].toString(), true);
                                                return null;
                                            }, MCRSystemUserInformation.getJanitorInstance()).call();
                                            flagMessageAsSeen(message);
                                        } catch (MessagingException e) {
                                            LOGGER.error(e);
                                        } catch (Exception e) {
                                            LOGGER.error(e);
                                        }
                                    }
                                }
                            }
                        } catch (IOException e) {
                            LOGGER.error(e);
                        }
                    }
                }
            }
        } catch (NoSuchProviderException e) {
            LOGGER.error(e);
        } catch (MessagingException e) {
            LOGGER.error(e);
        } finally {
            try {
                if (store != null) {
                    if (inbox != null) {
                        inbox.close(false);
                    }
                    store.close();
                }
            } catch (MessagingException e) {
                LOGGER.warn(e);
            }
        }
        getProcessable().setProgress(100);
    }

    @Override
    public String getDescription() {
        return "Processes bounce messages.";
    }

    /**
     * Flags a message instance as seen.
     * @param message the message instance
     * @throws MessagingException if flagging failed
     */
    private void flagMessageAsSeen(Message message) throws MessagingException {
        message.setFlag(Flags.Flag.SEEN, true);
    }

    /**
     * Returns all unreaded messages.
     * @return the unreaded messages as array
     * @throws MessagingException if inbox cannot be readed
     */
    private Message[] getUnreadMessages() throws MessagingException {
        return inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
    }
}
