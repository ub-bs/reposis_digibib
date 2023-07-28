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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;
import de.vzg.reposis.digibib.contact.model.ContactRecipientOrigin;
import de.vzg.reposis.digibib.contact.model.ContactRecipient;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestState;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jdom2.Element;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.MCRTransactionHelper;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.processing.MCRProcessableStatus;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mcr.cronjob.MCRCronjob;
import org.mycore.mods.MCRMODSWrapper;

/**
 * This class implements a cronjob that collects mails for contact requests.
 */
public class ContactCollectRecipientsCronjob extends MCRCronjob {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Mail of fallback recipient.
     */
    private static final String FALLBACK_MAIL
        = MCRConfiguration2.getStringOrThrow(ContactConstants.CONF_PREFIX + "FallbackRecipient.Mail");

    /**
     * Name of fallback recipient.
     */
    private static final String FALLBACK_NAME
        = MCRConfiguration2.getStringOrThrow(ContactConstants.CONF_PREFIX + "FallbackRecipient.Name");

    @Override
    public void runJob() {
        getProcessable().setStatus(MCRProcessableStatus.processing);
        getProcessable().setProgress(0);
        MCRSessionMgr.unlock();
        final MCRSession session = MCRSessionMgr.getCurrentSession();
        session.setUserInformation(MCRSystemUserInformation.getJanitorInstance());
        try { // preventive, to prevent dying
            doWork();
        } catch (Exception e) {
            LOGGER.error("Job failed: ", e);
        }
        session.close();
        getProcessable().setProgress(100);
    }

    @Override
    public String getDescription() {
        return "Collects recipients for all new requests.";
    }

    /**
     * Collects mails for contact request.
     * 
     * @throws Exception if job fails
     */
    private void doWork() throws Exception {
        final ContactService service = ContactService.getInstance();
        final List<ContactRequest> requests = service.listRequestsByState(ContactRequestState.RECEIVED);
        requests.addAll(service.listRequestsByState(ContactRequestState.PROCESSING));
        requests.addAll(service.listRequestsByState(ContactRequestState.PROCESSING_FAILED));
        final Map<MCRObjectID, List<ContactRecipient>> recipientsCache
            = new HashMap<MCRObjectID, List<ContactRecipient>>();
        requests.forEach((r) -> {
            LOGGER.info("Collecting recipients for {}", r.getId());
            final MCRObjectID objectID = r.getObjectID();
            final List<ContactRecipient> cachedRecipients = recipientsCache.get(objectID);
            MCRTransactionHelper.beginTransaction();
            try {
                r.setState(ContactRequestState.PROCESSING);
                service.updateRequest(r);
                MCRTransactionHelper.commitTransaction();
                if (cachedRecipients != null) {
                    addRecipients(r, cachedRecipients);
                } else {
                    final List<ContactRecipient> recipients = new ArrayList();
                    try {
                        final List<Element> correspondingAuthors = getCorrespondingAuthors(objectID);
                        for (Element correspondingAuthor : correspondingAuthors) {
                            final NameWrapper wrapper = new NameWrapper(correspondingAuthor);
                            if (wrapper.hasORCID()) {
                                final String name = wrapper.getName();
                                final Set<String> mails = ContactORCIDService.getMails(wrapper.getORCID());
                                mails.forEach(m -> recipients.add(new ContactRecipient(name, ContactRecipientOrigin.ORCID, m)));
                            }
                        }
                    } catch (Exception e) {
                        //
                    }
                    if (recipients.isEmpty()) {
                        addFallbackRecipient(recipients);
                    }
                    addRecipients(r, recipients);
                    recipientsCache.put(objectID, recipients);
                }
                r.setState(ContactRequestState.PROCESSED);
            } catch (ContactRequestNotFoundException e) {
                // request seems to be deleted in meantime, nothing to do
            } catch (Exception e) {
                try {
                    MCRTransactionHelper.rollbackTransaction();
                } catch (Exception rollbackExc) {
                    LOGGER.error("Error while rollbacking transaction.", rollbackExc);
                }
                r.setState(ContactRequestState.PROCESSING_FAILED);
                r.setDebug(e.toString());
                LOGGER.error(e);
            } finally {
                MCRTransactionHelper.beginTransaction();
                try {
                    service.updateRequest(r);
                    MCRTransactionHelper.commitTransaction();
                } catch (ContactRequestNotFoundException e) {
                    // request seems to be deleted in meantime, nothing to do
                } catch (Exception e) {
                    LOGGER.error(e);
                    try {
                        MCRTransactionHelper.rollbackTransaction();
                    } catch (Exception rollbackExc) {
                        LOGGER.error("Error while rollbacking transaction.", rollbackExc);
                    }
                }
            }
        });
    }

    /**
     * Adds fallback recipient to recipients
     * 
     * @param recipients the recipients
     */
    private void addFallbackRecipient(List<ContactRecipient> recipients) {
        final ContactRecipient fallback
            = new ContactRecipient(FALLBACK_NAME, ContactRecipientOrigin.FALLBACK, FALLBACK_MAIL);
        recipients.add(fallback);
    }

    /**
     * Adds list of recipients to recipients to contact request.
     * 
     * @param request    the contact request
     * @param recipients the recipients
     */
    private void addRecipients(ContactRequest request, List<ContactRecipient> recipients) {
        recipients.forEach((r) -> request.addRecipient(r));
    }

    // TODO
    private List<Element> getCorrespondingAuthors(MCRObjectID objectID) {
        final MCRObject object = MCRMetadataManager.retrieveMCRObject(objectID);
        return new MCRMODSWrapper(object).getElements("mods:name/mods:nameIdentifier[mods:role/mods]");
    }
}
