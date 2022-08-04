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
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestRecipient;
import de.vzg.reposis.digibib.contact.model.ContactRequestState; // TODO innerclass of model
import de.vzg.reposis.digibib.contact.model.RecipientSource; // TODO innerclass of model

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.processing.MCRProcessableStatus;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mcr.cronjob.MCRCronjob;
import org.mycore.util.concurrent.MCRFixedUserCallable;

public class ContactCollectCronjob extends MCRCronjob {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String FALLBACK_EMAIL = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "FallbackEmail");

    @Override
    public void runJob() {
        getProcessable().setStatus(MCRProcessableStatus.processing);
        getProcessable().setProgress(0);
        work();
        getProcessable().setProgress(100);
    }

    @Override
    public String getDescription() {
        return "Collects recipients for all new contact requests.";
    }

    public void work() {
        final ContactRequestService service = ContactRequestService.getInstance();
        LOGGER.info("Running contact collection cron...");
        final Map<MCRObjectID, List<ContactRequestRecipient>> recipientsCache = new HashMap();
        service.listContactRequestsByState(ContactRequestState.RECEIVED).stream().forEach((r) -> {
            LOGGER.info("Collecting recipients for {}", r.getId());
            final MCRObjectID objectID = r.getObjectID();
            final List<ContactRequestRecipient> cachedRecipients = recipientsCache.get(objectID);
            try {
                new MCRFixedUserCallable<>(() -> { // use own transaction for each request to isolate errors
                    if (cachedRecipients != null) {
                        cachedRecipients.forEach((v) -> r.addRecipient(v));
                    } else {
                        List<ContactRequestRecipient> recipients = new ContactCollectTask(objectID).call();
                        if (recipients.isEmpty()) {
                            addFallbackRecipient(recipients);
                        } else {
                            addRecipients(r, recipients);
                        }
                        recipientsCache.put(objectID, recipients);
                    }
                    r.setState(ContactRequestState.READY);
                    service.updateContactRequest(r);
                    return null;
                }, MCRSystemUserInformation.getJanitorInstance()).call();
            } catch (Exception e) {
                LOGGER.error(e);
            }
        });
    }

    private void addFallbackRecipient(List<ContactRequestRecipient> recipients) {
        ContactRequestRecipient fallback =
                new ContactRequestRecipient("FDM Team", RecipientSource.FALLBACK, FALLBACK_EMAIL);
        recipients.add(fallback);
    }

    private void addRecipients(ContactRequest contactRequest, List<ContactRequestRecipient> recipients) {
        recipients.forEach((r) -> {
            contactRequest.addRecipient(r);
        });
    }
}
