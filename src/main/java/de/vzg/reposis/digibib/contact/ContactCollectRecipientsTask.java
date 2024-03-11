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
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.orcid2.MCRORCIDUtils;
import org.mycore.orcid2.client.MCRORCIDCredential;
import org.mycore.orcid2.user.MCRORCIDUser;
import org.mycore.orcid2.user.MCRORCIDUserUtils;
import org.mycore.orcid2.v3.client.MCRORCIDClientHelper;
import org.mycore.orcid2.v3.client.MCRORCIDSectionImpl;
import org.orcid.jaxb.model.v3.release.record.Email;
import org.orcid.jaxb.model.v3.release.record.Emails;

import de.vzg.reposis.digibib.contact.model.ContactRecipient;
import de.vzg.reposis.digibib.contact.model.ContactRecipientOrigin;

/**
 * Task that collects recipients for object id.
 */
public class ContactCollectRecipientsTask implements Callable<List<ContactRecipient>> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final MCRObjectID objectId;

    public ContactCollectRecipientsTask(MCRObjectID objectID) {
        objectId = objectID;
    }

    @Override
    public List<ContactRecipient> call() {
        return collectRecipients();
    }

    private List<ContactRecipient> collectRecipients() {
        final List<ContactRecipient> recipients = new ArrayList<ContactRecipient>();
        addOrcidRecipients(recipients);
        return recipients;
    }

    private void addOrcidRecipients(List<ContactRecipient> recipients) {
        final MCRObject object = MCRMetadataManager.retrieveMCRObject(objectId);
        final List<String> orcids = new ArrayList<String>(MCRORCIDUtils.getORCIDs(object));
        for (String orcid : orcids) {
            final Set<String> mails = new HashSet<String>();
            final MCRORCIDUser orcidUser = MCRORCIDUserUtils.getORCIDUserByORCID(orcid);
            if (orcidUser != null) {
                // TODO may check all credentials of user
                final MCRORCIDCredential credential = orcidUser.getCredentialByORCID(orcid);
                final Set<String> otherOrcids = orcidUser.getORCIDs();
                if (credential != null) {
                    try {
                        mails.addAll(fetchMailsFromMemberApi(orcid, credential));
                        otherOrcids.remove(orcid);
                    } catch (Exception e) {
                        LOGGER.warn(e);
                    }
                }
                for (String otherOrcid : otherOrcids) {
                    try {
                        mails.addAll(fetchMailsFromPublicApi(otherOrcid));
                    } catch (Exception e) {
                        LOGGER.warn(e);
                    }
                }
            } else {
                try {
                    mails.addAll(fetchMailsFromPublicApi(orcid));
                } catch (Exception e) {
                    LOGGER.warn(e);
                }
            }
            final String name = orcidUser.getUser().getRealName() != null ? orcidUser.getUser().getRealName()
                : orcidUser.getUser().getUserName();
            mails.stream()
                .map(m -> new ContactRecipient(name, ContactRecipientOrigin.ORCID, m))
                .forEach(r -> {
                    recipients.add(r);
                });
        }
    }

    private List<String> extractMails(Emails mails) {
        return mails.getEmails().stream().map(Email::getEmail).distinct().toList();
    }

    private List<String> fetchMailsFromMemberApi(String orcid, MCRORCIDCredential credential) {
        return extractMails(MCRORCIDClientHelper.getClientFactory().createUserClient(orcid, credential)
            .fetch(MCRORCIDSectionImpl.EMAIL, Emails.class));
    }

    private List<String> fetchMailsFromPublicApi(String orcid) {
        return extractMails(MCRORCIDClientHelper.getClientFactory().createReadClient().fetch(orcid,
            MCRORCIDSectionImpl.EMAIL, Emails.class));
    }
}
