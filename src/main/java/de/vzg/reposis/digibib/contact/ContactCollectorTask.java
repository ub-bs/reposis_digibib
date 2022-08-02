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

import de.vzg.reposis.digibib.contact.ContactRequestService;
import de.vzg.reposis.digibib.contact.exception.ContactRequestNotFoundException;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestRecipient;
import de.vzg.reposis.digibib.contact.model.ContactRequestState;
import de.vzg.reposis.digibib.contact.model.RecipientSource;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jdom2.Element;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.util.concurrent.MCRFixedUserCallable;

public class ContactCollectorTask implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String FALLBACK_EMAIL = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "FallbackEmail");

    private ContactRequestService contactRequestService;

    public ContactCollectorTask() {
        this.contactRequestService = ContactRequestService.getInstance();
    }

    @Override
    public void run() {
        LOGGER.info("Running contact collection cron...");
        contactRequestService.listContactRequestsByState(ContactRequestState.RECEIVED).stream().forEach((r) -> {
            compute(r);
        });
    }

    private void updateContactRequest(ContactRequest contactRequest) throws Exception {
        new MCRFixedUserCallable<>(() -> {
            contactRequestService.updateContactRequest(contactRequest);
            return null;
        }, MCRSystemUserInformation.getJanitorInstance()).call();
    }

    private void compute(ContactRequest contactRequest) {
        try {
            LOGGER.info("Started collection task for {}.", contactRequest.getObjectID().toString());
            contactRequest.setState(ContactRequestState.PROCESSING);
            updateContactRequest(contactRequest);
            if (contactRequest.getRecipients().isEmpty()) {
                addFallbackRecipient(contactRequest);
            }
            contactRequest.setState(ContactRequestState.READY);
            updateContactRequest(contactRequest);
        } catch (ContactRequestNotFoundException e) {
            LOGGER.info("Request seems deleted. skipping: {}...", contactRequest.getId());
        } catch (Exception e) {
            LOGGER.error("Error while computing contact request: {}.", contactRequest.getId(), e);
            // TODO set error state
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

    private List<Element> getAuthors(MCRObject object) {
        return new MCRMODSWrapper(object).getElements("mods:name[@type='personal']");
    }
}
