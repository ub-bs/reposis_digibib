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
import java.util.concurrent.Callable;

import de.vzg.reposis.digibib.contact.model.ContactRequestRecipient;
import de.vzg.reposis.digibib.contact.model.RecipientSource;

import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;

public class ContactCollectTask implements Callable<List<ContactRequestRecipient>> {

    private final MCRObjectID objectID;

    public ContactCollectTask(MCRObjectID objectID) {
        this.objectID = objectID;
    }

    @Override
    public List<ContactRequestRecipient> call() {
        return collectRecipients();
    }

    private List<ContactRequestRecipient> collectRecipients() {
        final List<ContactRequestRecipient> recipients = new ArrayList();
        // addOrcidRecipients
        return recipients;
    }

    private void addOrcidRecipients() {
        final MCRObject object = MCRMetadataManager.retrieveMCRObject(objectID);
        /* getAuthors(object).stream().forEach((a) -> {
            final String orcid = getORCID(a);
            if (orcid != null) {
                recipients.addAll(getEmails(orcid));
            }
        }); */
    }
}
