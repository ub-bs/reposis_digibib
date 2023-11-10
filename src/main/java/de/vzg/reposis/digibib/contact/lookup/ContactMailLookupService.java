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

package de.vzg.reposis.digibib.contact.lookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.vzg.reposis.digibib.contact.ContactConstants;
import de.vzg.reposis.digibib.contact.model.ContactRecipient;
import de.vzg.reposis.digibib.contact.model.ContactRecipientOrigin;
import de.vzg.reposis.digibib.contact.util.NameWrapper;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.mycore.common.config.MCRConfiguration2;

/**
 * Service to look up recipients for name.
 */
public class ContactMailLookupService {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final List<ContactMailLookup> LOOKUPS = MCRConfiguration2
        .getOrThrow(ContactConstants.CONF_PREFIX + "MailLookupService.Lookups", MCRConfiguration2::splitValue)
        .map(MCRConfiguration2::<ContactMailLookup>instantiateClass)
        .collect(Collectors.toList());

    /**
     * Collects from recipients from lookup providers.
     * 
     * @param nameWrapper the NameWrapper for name
     * @return a List of ContactRecipients
     */
    public static List<ContactRecipient> getRecipients(NameWrapper nameWrapper) {
        final List<ContactRecipient> recipients = new ArrayList();
        for (ContactMailLookup lookup : LOOKUPS) {
            LOGGER.debug("Looking up {}...", lookup.getName());
            try {
                final Set<String> mails = lookup.getMails(nameWrapper);
                mails.forEach(m -> recipients.add(new ContactRecipient(nameWrapper.getName(), lookup.getName(), m)));
                if (!mails.isEmpty()) {
                    LOGGER.debug("Found {} mails.", mails.size());
                }
            } catch (Exception e) {
                LOGGER.warn(e);
            }
        }
        return recipients;
    }
}
