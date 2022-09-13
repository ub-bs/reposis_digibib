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

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Message.RecipientType;
import javax.mail.internet.MimeMessage;

import com.sun.mail.dsn.MultipartReport;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.mycore.common.processing.MCRProcessableStatus;
import org.mycore.mcr.cronjob.MCRCronjob;

public class ContactProcessFailedRecipientsCronjob extends MCRCronjob {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String REPORT_MIMETYPE = "multipart/report";

    @Override
    public void runJob() {
        getProcessable().setStatus(MCRProcessableStatus.processing);
        getProcessable().setProgress(0);
        final ContactMailService service = ContactMailService.getInstance();
        try {
            final Message[] unreadMessages = service.getUnreadMessages();
            LOGGER.info("Found {} unread messages", unreadMessages.length);
            for (Message message : unreadMessages) {
                if(message instanceof MimeMessage) {
                    final MimeMessage mime = (MimeMessage) message;
                    if (mime.isMimeType(REPORT_MIMETYPE)) {
                        try {
                            final MultipartReport dsn = (MultipartReport) mime.getContent();
                            MimeMessage m = dsn.getReturnedMessage();
                            if(m != null) {
                                final String requestId = m.getHeader(ContactConstants.REQUEST_HEADER_NAME, null);
                                if (requestId != null) {
                                    LOGGER.info(requestId);
                                    final Address[] recipients = m.getRecipients(Message.RecipientType.TO);
                                    if (recipients != null && recipients.length == 1) {
                                        LOGGER.info(recipients[0]);
                                        // TODO update recipient
                                        // TODO read message
                                    }
                                }
                            }
                        } catch (IOException e) {
                            LOGGER.warn(e);
                        }
                    }
                }
            }
        } catch (MessagingException e) {
            LOGGER.warn(e);
        }
        getProcessable().setProgress(100);
    }

    @Override
    public String getDescription() {
        return "Processes failed messages.";
    }
}
