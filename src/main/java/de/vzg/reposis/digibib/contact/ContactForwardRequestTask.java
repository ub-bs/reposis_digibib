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

import de.vzg.reposis.digibib.contact.ContactService;
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

/**
 * This task forwards a contact request to all recipients.
 */
public class ContactForwardRequestTask implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Name of mail sender.
     */


    /**
     * Name of stylesheet to transform mail.
     */
    private static final String MAIL_STYLESHEET = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "RecipientMail.Stylesheet");

    /**
     * The contact request.
     */
    private final ContactRequest request;

    public ContactForwardRequestTask(ContactRequest request) {
        this.request = request;
    }

    @Override
    public void run() {
        LOGGER.info("Sending contact request: {}", request.getId());
        MCRSessionMgr.unlock();
        final MCRSession session = MCRSessionMgr.getCurrentSession();
        session.setUserInformation(MCRSystemUserInformation.getJanitorInstance());
        final Map<String, String> headers = new HashMap();
        headers.put(ContactConstants.REQUEST_HEADER_NAME, request.getUUID().toString());
        try {
            for (ContactRecipient recipient : request.getRecipients().stream().filter(r -> r.isEnabled() && r.getSent() == null).collect(Collectors.toList())) {
                final EMail mail = createMail(recipient);
                ContactMailService.sendMail(mail, recipient.getEmail(), headers);
                recipient.setSent(new Date());
                MCRTransactionHelper.beginTransaction();
                try {
                    ContactService.getInstance().updateRecipient(recipient);
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
                ContactService.getInstance().updateRequest(request);
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

    /**
     * Creates mail for recipient with given properties.
     * @param recipient the recipient
     * @return the mail
     * @throws Exception if mail transformation fails
     */
    private EMail createMail(ContactRecipient recipient) throws Exception {
        final EMail baseMail = new EMail();
        final Map<String, String> properties = new HashMap();
        properties.put("email", request.getSender());
        properties.put("id", request.getObjectID().toString());
        properties.put("message", request.getMessage());
        properties.put("name", request.getName());
        properties.put("recipient", recipient.getName());
        properties.put("recipientID", recipient.getUUID().toString());
        properties.put("requestID", request.getUUID().toString());
        properties.put("title", request.getObjectID().toString());
        final String orcid = request.getORCID();
        if (orcid != null) {
            properties.put("orcid", orcid);
        }
        final Element mailElement = ContactUtils.transform(baseMail.toXML(), MAIL_STYLESHEET, properties).getRootElement();
        return EMail.parseXML(mailElement);
    }
}
