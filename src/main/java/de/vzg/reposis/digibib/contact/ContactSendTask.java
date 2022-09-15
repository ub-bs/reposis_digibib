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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import de.vzg.reposis.digibib.contact.ContactRequestService;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestState;
import de.vzg.reposis.digibib.contact.model.ContactRecipient;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jdom2.Element;
import org.mycore.common.MCRMailer.EMail;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.MCRTransactionHelper;
import org.mycore.common.config.MCRConfiguration2;

public class ContactSendTask implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String SENDER_NAME = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "Email.SenderName");

    private static final String MAIL_STYLESHEET = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "Email.Stylesheet");

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
        final Map<String, String> headers = new HashMap();
        headers.put(ContactConstants.REQUEST_HEADER_NAME, request.getUuid().toString());
        try {
            for (ContactRecipient recipient : request.getRecipients().stream().filter(r -> r.isEnabled() && r.getSent() == null).collect(Collectors.toList())) {
                final EMail mail = createMail(recipient.getName(), null); // TODO
                final String to = recipient.getEmail();
                ContactMailService.sendMail(mail, SENDER_NAME, to, headers);
                recipient.setSent(new Date());
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

    private EMail createMail(String recipientName, String token) throws Exception {
        final EMail baseMail = new EMail();
        final Map<String, String> properties = new HashMap();
        properties.put("email", request.getSender());
        properties.put("id", request.getObjectID().toString());
        properties.put("message", request.getMessage());
        properties.put("name", request.getName());
        properties.put("recipient", recipientName);
        properties.put("title", request.getObjectID().toString());
        final String orcid = request.getORCID();
        if (orcid != null) {
            properties.put("orcid", orcid);
        }
        final Element mailElement = ContactUtils.transform(baseMail.toXML(), MAIL_STYLESHEET, properties).getRootElement();
        return EMail.parseXML(mailElement);
    }
}
