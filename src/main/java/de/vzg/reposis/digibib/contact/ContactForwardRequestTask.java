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

import de.vzg.reposis.digibib.contact.model.ContactRecipient;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestState;

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

    private static final String FORWARDING_CONFIRMATION_STYLESHEET = MCRConfiguration2.getStringOrThrow(ContactConstants.CONF_PREFIX + "ForwardingConfirmationMail.Stylesheet");

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
        try {
            MCRTransactionHelper.beginTransaction();
            for (ContactRecipient recipient : request.getRecipients().stream()
                    .filter(r -> r.isEnabled() && r.getSent() == null).toList()) {
                try {
                    ContactForwardRequestHelper.sendMail(recipient);
                    recipient.setFailed(null);
                } catch (Exception e){
                    recipient.setFailed(new Date());
                } finally {
                    recipient.setSent(new Date());
                    ContactService.getInstance().updateRecipient(recipient);
                }
            }
            request.setState(ContactRequestState.SENT);
            request.setForwarded(new Date());
            try {
                sendForwardingConfirmation();
            } catch (Exception e) {
                LOGGER.error("Cannot send forward confirmation.", e);
            }
        } catch (Exception e) {
            request.setDebug(e.getMessage());
            request.setState(ContactRequestState.SENDING_FAILED);
        } finally {
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

    private void sendForwardingConfirmation() throws Exception {
        final EMail forwardConfirmation = new EMail();
        final Map<String, String> properties = new HashMap<String, String>();
        properties.put("id", request.getObjectID().toString());
        properties.put("name", request.getName());
        final Element mailElement = ContactUtils.transform(forwardConfirmation.toXML(), FORWARDING_CONFIRMATION_STYLESHEET, properties).getRootElement();
        ContactMailService.sendMail(EMail.parseXML(mailElement), request.getFrom());
    }
}
