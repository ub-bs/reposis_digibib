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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

import de.vzg.reposis.digibib.contact.model.ContactRecipient;
import de.vzg.reposis.digibib.contact.model.ContactRecipientOrigin;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.orcid2.client.MCRORCIDReadClient;
import org.mycore.orcid2.client.exception.MCRORCIDRequestException;
import org.mycore.orcid2.user.MCRORCIDCredentials;
import org.mycore.orcid2.user.MCRORCIDUser;
import org.mycore.orcid2.v3.MCRORCIDAPIClientFactoryImpl;
import org.mycore.orcid2.v3.MCRORCIDSectionImpl;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;
import org.orcid.jaxb.model.v3.release.record.Emails;
import org.orcid.jaxb.model.v3.release.record.Email;

/**
 * Task that collects recipients for object id.
 */
public class ContactCollectRecipientsTask implements Callable<List<ContactRecipient>> {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * The object id.
     */
    private final MCRObjectID objectID;

    public ContactCollectRecipientsTask(MCRObjectID objectID) {
        this.objectID = objectID;
    }

    @Override
    public List<ContactRecipient> call() {
        return collectRecipients();
    }

    private List<ContactRecipient> collectRecipients() {
        final List<ContactRecipient> recipients = new ArrayList();
        addOrcidRecipients(recipients);
        return recipients;
    }

    /**
     * Collect recipients from orcid.
     */
    private void addOrcidRecipients(List<ContactRecipient> recipients) {
        final MCRObject object = MCRMetadataManager.retrieveMCRObject(objectID);
        final List<MCRUser> users = getAuthors(object).stream()
                .flatMap(a -> a.getChildren("nameIdentifier", MCRConstants.MODS_NAMESPACE).stream())
                .filter(c -> "orcid".equalsIgnoreCase(c.getAttributeValue("type")))
                .map(e -> MCRUserManager.getUsers(MCRORCIDUser.ATTR_ID_PREFIX + "orcid", e.getText()).findFirst())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct()
                .toList();
        for (MCRUser user : users) {
            final Set<String> mails = new HashSet();
            final MCRORCIDUser orcidUser = new MCRORCIDUser(user);
            final MCRORCIDCredentials credentials = orcidUser.getCredentials();
            final Set<String> otherOrcids = orcidUser.getORCIDs();
            if (credentials != null) {
                try {
                    mails.addAll(fetchMailsFromMemberAPI(credentials));
                    otherOrcids.remove(credentials.getORCID());
                } catch (Exception e) {
                    LOGGER.warn(e);
                }
            }
            for (String otherOrcid : otherOrcids) {
                try {
                    mails.addAll(fetchMailsFromPublicAPI(otherOrcid));
                } catch (Exception e) {
                    LOGGER.warn(e);
                }
            }
            final String name = user.getRealName() != null ? user.getRealName() : user.getUserName();
            mails.stream()
                .map(m -> new ContactRecipient(name, ContactRecipientOrigin.ORCID, m))
                .forEach(r -> {
                    recipients.add(r);
                });
        }
    }

    private List<String> fetchMailsFromPublicAPI(String orcid) throws MCRORCIDRequestException {
        return MCRORCIDAPIClientFactoryImpl.getInstance().createPublicClient()
                .fetch(orcid, MCRORCIDSectionImpl.EMAIL, Emails.class).getEmails().stream()
                .map(Email::getEmail)
                .distinct()
                .toList();
    }

    private List<String> fetchMailsFromMemberAPI(MCRORCIDCredentials credentials) throws MCRORCIDRequestException {
        Emails mails = MCRORCIDAPIClientFactoryImpl.getInstance().createMemberClient(credentials)
                .fetch(MCRORCIDSectionImpl.EMAIL, Emails.class);
        LOGGER.info(mails.getEmails().size());
        return mails.getEmails().stream()
                .map(Email::getEmail)
                .distinct()
                .toList();
    }

    /**
     * Returns author elements for object 
     * 
     * @param object the object
     * @return list of author elements
     */
    private List<Element> getAuthors(MCRObject object) {
        return new MCRMODSWrapper(object)
                .getElements("mods:name[@type='personal']");
    }
}
