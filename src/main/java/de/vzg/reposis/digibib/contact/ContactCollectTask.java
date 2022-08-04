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
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestRecipient;
import de.vzg.reposis.digibib.contact.model.ContactRequestState;
import de.vzg.reposis.digibib.contact.model.RecipientSource;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.util.concurrent.MCRFixedUserCallable;

public class ContactCollectTask implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String FALLBACK_EMAIL = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "FallbackEmail");

    private final ContactRequestService contactRequestService;

    public ContactCollectTask() {
        this.contactRequestService = ContactRequestService.getInstance();
    }

    @Override
    public void run() {
        LOGGER.info("Running contact collection cron...");
        final Map<MCRObjectID, List<ContactRequestRecipient>> recipientsCache = new HashMap();
        contactRequestService.listContactRequestsByState(ContactRequestState.RECEIVED).stream().forEach((r) -> {
            LOGGER.info("Collecting recipients for {}", r.getId());
            final List<ContactRequestRecipient> cachedRecipients = recipientsCache.get(r.getObjectID());
            if (cachedRecipients != null) {
                cachedRecipients.forEach((v) -> r.addRecipient(v));
            } else {
                try {
                    new MCRFixedUserCallable<>(() -> { // use own transaction for each request to isolate errors
                        collectRecipients(r);
                        recipientsCache.put(r.getObjectID(), r.getRecipients());
                        return null;
                    }, MCRSystemUserInformation.getJanitorInstance()).call();
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            }
        });
    }

    private void collectRecipients(ContactRequest contactRequest) {
        try {
            if (contactRequest.getRecipients().isEmpty()) {
                addFallbackRecipient(contactRequest);
            }
            contactRequest.setState(ContactRequestState.READY);
        } catch (ContactRequestNotFoundException e) {
            // nothing to do
        } finally {
            try {
                contactRequestService.updateContactRequest(contactRequest);
            } catch (ContactRequestNotFoundException e) {
                // nothing to do
            }
        }
    }

    private void addOrcidRecipients(ContactRequest contactRequest) {
        final MCRObjectID objectID = contactRequest.getObjectID();
        final MCRObject object = MCRMetadataManager.retrieveMCRObject(objectID);
        /* getAuthors(object).stream().forEach((a) -> {
            final String orcid = getORCID(a);
            if (orcid != null) {
                recipients.addAll(getEmails(orcid));
            }
        }); */
    }

    private void addFallbackRecipient(ContactRequest contactRequest) {
        ContactRequestRecipient fallback =
                new ContactRequestRecipient("FDM Team", RecipientSource.FALLBACK, FALLBACK_EMAIL);
        fallback.setContactRequest(contactRequest);
        contactRequest.addRecipient(fallback);
    }
}
