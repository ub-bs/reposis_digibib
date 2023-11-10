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
import java.util.Map;

import de.vzg.reposis.digibib.contact.exception.ContactException;
import de.vzg.reposis.digibib.contact.model.ContactRecipient;
import de.vzg.reposis.digibib.contact.model.ContactRequest;
import de.vzg.reposis.digibib.contact.util.ContactUtils;
import org.jdom2.Element;
import org.mycore.common.MCRMailer.EMail;
import org.mycore.common.config.MCRConfiguration2;

public class ContactForwardRequestHelper {

    /**
     * Name of stylesheet to transform mail.
     */
    private static final String MAIL_STYLESHEET
        = MCRConfiguration2.getStringOrThrow(ContactConstants.CONF_PREFIX + "RequestMail.Stylesheet");

    /**
     * Creates Email for recipient and sends it.
     *
     * @param recipient the recipient
     * @throws ContactException if mail cannot be send
     */
    public static void sendMail(ContactRecipient recipient) {
        if (recipient.getRequest() == null) {
            throw new ContactException("Request no set.");
        }
        final ContactRequest r = recipient.getRequest();
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put(ContactConstants.REQUEST_HEADER_NAME, r.getUUID().toString());
        final EMail mail = createMail(recipient);
        ContactMailService.sendMail(mail, recipient.getMail(), headers);
    }

    /**
     * Creates mail for recipient with given properties.
     * 
     * @param recipient the recipient
     * @return the mail
     * @throws ContactException if mail transformation fails
     */
    private static EMail createMail(ContactRecipient recipient) {
        final ContactRequest r = recipient.getRequest();
        final EMail baseMail = new EMail();
        final Map<String, String> properties = new HashMap<String, String>();
        properties.put("email", r.getFrom());
        properties.put("id", r.getObjectID().toString());
        properties.put("message", r.getMessage());
        properties.put("name", r.getName());
        properties.put("recipient", recipient.getName());
        properties.put("recipientID", recipient.getUUID().toString());
        properties.put("requestID", r.getUUID().toString());
        properties.put("title", r.getObjectID().toString());
        final String orcid = r.getORCID();
        if (orcid != null) {
            properties.put("orcid", orcid);
        }
        final String comment = r.getComment();
        if (comment != null) {
            properties.put("comment", comment);
        }
        final Element mailElement = ContactUtils.transform(baseMail.toXML(), MAIL_STYLESHEET, properties)
            .getRootElement();
        return EMail.parseXML(mailElement);
    }
}
