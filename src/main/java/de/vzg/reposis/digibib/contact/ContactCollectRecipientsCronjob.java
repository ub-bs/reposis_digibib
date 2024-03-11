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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.MCRTransactionHelper;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.processing.MCRProcessableStatus;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mcr.cronjob.MCRCronjob;

import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;
import de.vzg.reposis.digibib.contact.model.ContactRecipient;
import de.vzg.reposis.digibib.contact.model.ContactRecipientOrigin;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestState;

/**
 * This class implements a cronjob that collects mails for contact requests.
 */
public class ContactCollectRecipientsCronjob extends MCRCronjob {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String FALLBACK_MAIL = MCRConfiguration2
        .getStringOrThrow(ContactConstants.CONF_PREFIX + "FallbackRecipient.Mail");

    private static final String FALLBACK_NAME = MCRConfiguration2
        .getStringOrThrow(ContactConstants.CONF_PREFIX + "FallbackRecipient.Name");

    @Override
    public String getDescription() {
        return "Collects recipients for all new requests.";
    }

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

    private List<ContactRequest> listRequestsByState(ContactService service, ContactRequestState state) {
        return service.listAllRequests().stream().filter(r -> Objects.equals(r.getState(), state))
            .collect(Collectors.toList());
    }

    private void doWork() throws Exception {
        final ContactService service = ContactServiceImpl.getInstance();
        final List<ContactRequest> requests = listRequestsByState(service, ContactRequestState.RECEIVED);
        requests.addAll(listRequestsByState(service, ContactRequestState.PROCESSING));
        requests.addAll(listRequestsByState(service, ContactRequestState.PROCESSING_FAILED));
        final Map<MCRObjectID, List<ContactRecipient>> recipientsCache = new HashMap<MCRObjectID, List<ContactRecipient>>();
        requests.forEach((r) -> {
            LOGGER.info("Collecting recipients for {}", r.getId());
            final MCRObjectID objectId = r.getObjectId();
            final List<ContactRecipient> cachedRecipients = recipientsCache.get(objectId);
            MCRTransactionHelper.beginTransaction();
            try {
                r.setState(ContactRequestState.PROCESSING);
                service.updateRequest(r);
                MCRTransactionHelper.commitTransaction();
                if (cachedRecipients != null) {
                    addRecipients(r, cachedRecipients);
                } else {
                    final List<ContactRecipient> recipients = new ContactCollectRecipientsTask(objectId).call();
                    if (recipients.isEmpty()) {
                        addFallbackRecipient(recipients);
                    }
                    addRecipients(r, recipients);
                    recipientsCache.put(objectId, recipients);
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

    private void addFallbackRecipient(List<ContactRecipient> recipients) {
        final ContactRecipient fallback = new ContactRecipient(FALLBACK_NAME, ContactRecipientOrigin.FALLBACK,
            FALLBACK_MAIL);
        recipients.add(fallback);
    }

    private void addRecipients(ContactRequest request, List<ContactRecipient> recipients) {
        recipients.forEach((r) -> request.addRecipient(r));
    }
}
