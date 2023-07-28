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

import de.vzg.reposis.digibib.contact.model.ContactRecipient;
import de.vzg.reposis.digibib.contact.model.ContactRecipientOrigin;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.mycore.orcid2.client.MCRORCIDClientFactory;
import org.mycore.orcid2.client.MCRORCIDCredential;
import org.mycore.orcid2.v3.client.MCRORCIDClientHelper;
import org.mycore.orcid2.v3.client.MCRORCIDSectionImpl;
import org.mycore.orcid2.user.MCRORCIDUser;
import org.mycore.orcid2.user.MCRORCIDUserUtils;
import org.orcid.jaxb.model.v3.release.record.Emails;
import org.orcid.jaxb.model.v3.release.record.Email;

/**
 * Provides ORCID Service.
 */
public class ContactORCIDService {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Fetches mail for given ORCID iD.
     * 
     * @param orcid the ORCID iD
     * @return List of mails
     */
    public static Set<String> getMails(String orcid) {
        final Set<String> mails = new HashSet<String>();
        final MCRORCIDUser orcidUser = MCRORCIDUserUtils.getORCIDUserByORCID(orcid);
        if (orcidUser != null) {
            final Set<String> otherOrcids = orcidUser.getORCIDs();
            for (String o : otherOrcids) {
                final MCRORCIDCredential credential = orcidUser.getCredentialByORCID(o);
                if (credential != null) {
                    try {
                        mails.addAll(fetchMailsFromMemberAPI(o, credential));
                        otherOrcids.remove(o);
                    } catch (Exception e) {
                        LOGGER.warn(e);
                    }
                }
            }
            for (String otherOrcid : otherOrcids) {
                try {
                    mails.addAll(fetchMailsFromPublicAPI(otherOrcid));
                } catch (Exception e) {
                    LOGGER.warn(e);
                }
            }
        } else {
            try {
                mails.addAll(fetchMailsFromPublicAPI(orcid));
            } catch (Exception e) {
                LOGGER.warn(e);
            }
        }
        return mails;
    }

    private static List<String> fetchMailsFromPublicAPI(String orcid) {
        return extractMails(MCRORCIDClientHelper.getClientFactory().createReadClient().fetch(orcid, MCRORCIDSectionImpl.EMAIL, Emails.class));
    }

    private static List<String> fetchMailsFromMemberAPI(String orcid, MCRORCIDCredential credential) {
        return extractMails(MCRORCIDClientHelper.getClientFactory().createUserClient(orcid, credential).fetch(MCRORCIDSectionImpl.EMAIL, Emails.class));
    }

    private static List<String> extractMails(Emails mails) {
        return mails.getEmails().stream().map(Email::getEmail).distinct().toList();
    }
}
