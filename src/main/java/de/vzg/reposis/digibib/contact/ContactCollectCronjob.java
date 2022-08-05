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

import de.vzg.reposis.digibib.contact.ContactRequestService;
import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;
import de.vzg.reposis.digibib.contact.model.ContactRecipientSource;
import de.vzg.reposis.digibib.contact.model.ContactRecipient;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestState;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.processing.MCRProcessableStatus;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mcr.cronjob.MCRCronjob;
import org.mycore.util.concurrent.MCRTransactionableCallable;

public class ContactCollectCronjob extends MCRCronjob {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String FALLBACK_EMAIL = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "FallbackEmail");

    @Override
    public void runJob() {
        getProcessable().setStatus(MCRProcessableStatus.processing);
        getProcessable().setProgress(0);
        try { // preventive, to prevent dying
            doWork();
        } catch (Exception e) {
            LOGGER.error("Job failed: ", e);
        }
        getProcessable().setProgress(100);
    }

    @Override
    public String getDescription() {
        return "Collects recipients for all new contact requests.";
    }

    private void updateContactRequest(ContactRequest contactRequest) throws Exception {
        new MCRTransactionableCallable<>(() -> { // use own transaction for each request to isolate errors
            LOGGER.info(MCRSessionMgr.getCurrentSession().getID());
            ContactRequestService.getInstance().updateContactRequest(contactRequest);
            return null;
        // }, MCRSystemUserInformation.getJanitorInstance()).call();
        }).call();
    }

    public void doWork() throws Exception {
        final ContactRequestService service = ContactRequestService.getInstance();
        LOGGER.info("Running contact collection cron...");
        final Map<MCRObjectID, List<ContactRecipient>> recipientsCache = new HashMap();
        service.listContactRequestsByState(ContactRequestState.RECEIVED).stream().forEach((r) -> {
            LOGGER.info("Collecting recipients for {}", r.getId());
            final MCRObjectID objectID = r.getObjectID();
            final List<ContactRecipient> cachedRecipients = recipientsCache.get(objectID);
            try {
                r.setState(ContactRequestState.PROCESSING);
                updateContactRequest(r);
                if (cachedRecipients != null) {
                    cachedRecipients.forEach((v) -> r.addRecipient(v)); // TODO
                } else {
                    List<ContactRecipient> recipients = new ContactCollectTask(objectID).call();
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
                r.setState(ContactRequestState.PROCESSING_FAILED);
                LOGGER.error(e);
            } finally {
                try {
                    updateContactRequest(r);
                } catch (ContactRequestNotFoundException e) {
                    // request seems to be deleted in meantime, nothing to do
                } catch (Exception e) {
                    LOGGER.warn("TODO");
                }
            }
        });
    }

    private void addFallbackRecipient(List<ContactRecipient> recipients) {
        final ContactRecipient fallback =
                new ContactRecipient("FDM Team", ContactRecipientSource.FALLBACK, FALLBACK_EMAIL);
        LOGGER.info("added fallback");
        recipients.add(fallback);
    }

    private void addRecipients(ContactRequest contactRequest, List<ContactRecipient> recipients) {
        recipients.forEach((r) -> contactRequest.addRecipient(r));
    }
}
