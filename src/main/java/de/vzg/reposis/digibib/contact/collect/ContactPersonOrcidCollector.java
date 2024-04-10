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

package de.vzg.reposis.digibib.contact.collect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.orcid2.MCRORCIDUtils;
import org.mycore.orcid2.client.MCRORCIDCredential;
import org.mycore.orcid2.user.MCRORCIDUser;
import org.mycore.orcid2.user.MCRORCIDUserUtils;
import org.mycore.orcid2.v3.client.MCRORCIDClientHelper;
import org.mycore.orcid2.v3.client.MCRORCIDSectionImpl;
import org.orcid.jaxb.model.v3.release.record.Email;
import org.orcid.jaxb.model.v3.release.record.Emails;
import org.orcid.jaxb.model.v3.release.record.Name;
import org.orcid.jaxb.model.v3.release.record.Person;

import de.vzg.reposis.digibib.contact.model.ContactPerson;

/**
 * Seeks related contact persons on orcid.
 */
public class ContactPersonOrcidCollector implements ContactPersonCollector {

    private static final Logger LOGGER = LogManager.getLogger();

    // TODO only process corresponding author orcid ids.
    @Override
    public List<ContactPerson> collect(MCRObject object) {
        LOGGER.info("Collecting contact person for {} from orcid", object.getId());
        final List<String> orcids = new ArrayList<String>(MCRORCIDUtils.getORCIDs(object));
        for (String orcid : orcids) {
            final String name = fetchNameFromPublicApi(orcid);
            final MCRORCIDUser orcidUser = MCRORCIDUserUtils.getORCIDUserByORCID(orcid);
            if (orcidUser != null) {
                final Set<String> mails = new HashSet<String>();
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
                return mails.stream().map(m -> new ContactPerson(name, m, "orcid", orcid)).toList();
            } else {
                try {
                    return fetchMailsFromPublicApi(orcid).stream().map(m -> new ContactPerson(name, m, "orcid", orcid))
                        .toList();
                } catch (Exception e) {
                    LOGGER.warn(e);
                }
            }
        }
        return Collections.emptyList();
    }

    private List<String> fetchMailsFromMemberApi(String orcid, MCRORCIDCredential credential) {
        return extractMails(MCRORCIDClientHelper.getClientFactory().createUserClient(orcid, credential)
            .fetch(MCRORCIDSectionImpl.EMAIL, Emails.class));
    }

    private List<String> fetchMailsFromPublicApi(String orcid) {
        return extractMails(MCRORCIDClientHelper.getClientFactory().createReadClient().fetch(orcid,
            MCRORCIDSectionImpl.EMAIL, Emails.class));
    }

    private List<String> extractMails(Emails mails) {
        return mails.getEmails().stream().map(Email::getEmail).distinct().toList();
    }

    private String fetchNameFromPublicApi(String orcid) {
        try {
            final Name name = MCRORCIDClientHelper.getClientFactory().createReadClient()
                .fetch(orcid, MCRORCIDSectionImpl.PERSON, Person.class).getName();
            return name.getGivenNames().getContent() + " " + name.getFamilyName().getContent();
        } catch (Exception e) {
            return orcid;
        }
    }

}
