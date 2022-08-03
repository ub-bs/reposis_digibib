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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.AuthenticationFailedException;

import de.vzg.reposis.digibib.contact.ContactRequestService;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestRecipient;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jdom2.Document;
import org.mycore.common.MCRMailer;
import org.mycore.common.MCRMailer.EMail;
import org.mycore.common.config.MCRConfiguration2;

public class ContactRequestSendTask implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String SENDER_NAME = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "Email.SenderName");

    private static final String MAIL_STYLESHEET = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "Email.Stylesheet");

    private final ContactRequest contactRequest;

    private final ContactRequestService contactRequestService;

    public ContactRequestSendTask(ContactRequest contactRequest) {
        this.contactRequest = contactRequest;
        this.contactRequestService = ContactRequestService.getInstance();
    }

    @Override
    public void run() {
        LOGGER.info("Sending contact request: ", contactRequest.getId());
        final Document mail = createBaseEmail().toXML();
        final Map<String, String> properties = new HashMap();
        properties.put("email", contactRequest.getSender());
        properties.put("id", contactRequest.getObjectID().toString());
        properties.put("message", contactRequest.getMessage());
        properties.put("name", contactRequest.getName());
        properties.put("title", contactRequest.getObjectID().toString());
        final String orcid = contactRequest.getORCID();
        if (orcid != null) {
            properties.put("orcid", orcid);
        }
        try {
            MCRMailer.sendMail(mail, MAIL_STYLESHEET, properties);
        } catch (AuthenticationFailedException e) {
            LOGGER.error("Authentication failed", e);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        // TODO handle state
    }

    private EMail createBaseEmail() {
        final EMail mail = new EMail();
        final Set<String> recipients = contactRequest.getRecipients().stream().map(ContactRequestRecipient::getEmail)
                .collect(Collectors.toSet());
        mail.to = List.copyOf(recipients);
        mail.from = SENDER_NAME;
        return mail;
    }
}