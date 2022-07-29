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
import java.util.List;

import de.vzg.reposis.digibib.contact.dao.ContactRequestDAO;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.model.ContactRequestRecipient;
import de.vzg.reposis.digibib.contact.model.ContactRequestState;
import de.vzg.reposis.digibib.contact.model.RecipientSource;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jdom2.Element;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;

public class ContactCollectorTask implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String FALLBACK_EMAIL = MCRConfiguration2
            .getStringOrThrow(ContactConstants.CONF_PREFIX + "FallbackEmail");

    private ContactRequestDAO contactRequestDAO;

    private ContactRequest contactRequest;

    public ContactCollectorTask(ContactRequestDAO contactRequestDAO, ContactRequest contactRequest) {
        this.contactRequestDAO = contactRequestDAO;
        this.contactRequest = contactRequest;
    }

    @Override
    public void run() {
        final MCRObjectID objectID = contactRequest.getObjectID();
        LOGGER.info("Started new collection task for {}.", objectID.toString());
        contactRequest.setState(ContactRequestState.PROCESSING);
        contactRequestDAO.update(contactRequest);
        final MCRObject object = MCRMetadataManager.retrieveMCRObject(objectID);
        final List<Element> authors = getAuthors(object);
        /* for (Element author : authors) {
            final String orcid = getORCID(author);
            if (orcid != null) {
                recipients.addAll(getEmails(orcid));
            }
                // TODO extract affiliations
        } */
        if (contactRequest.getRecipients().isEmpty()) {
            ContactRequestRecipient fallback =
                    new ContactRequestRecipient("FDM Team", RecipientSource.FALLBACK, FALLBACK_EMAIL);
            fallback.setContactRequest(contactRequest);
            contactRequest.addRecipient(fallback);
        }
        // contactRequest.setState(ContactRequestState.READY);
        // contactRequestDAO.update(contactRequest);
    }

    private static List<Element> getAuthors(MCRObject object) {
        return new MCRMODSWrapper(object).getElements("mods:name[@type='personal']");
    }
}
